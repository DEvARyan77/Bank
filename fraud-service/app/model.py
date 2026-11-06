"""Fraud scoring: an Isolation Forest (unsupervised ML) plus explainable rules.

The Isolation Forest learns what *normal* transfers look like and gives a high
anomaly score to transfers that are easy to isolate from the rest. Rules add
human-readable reasons, which banks need to explain why a payment was flagged.
"""

import math
from pathlib import Path

import joblib
import numpy as np
from sklearn.ensemble import IsolationForest

from app.schemas import RiskAssessment, RiskLevel, TransferFeatures

MODEL_VERSION = "isoforest-v1"
MODEL_PATH = Path(__file__).resolve().parent.parent / "model" / "fraud_model.joblib"

MEDIUM_THRESHOLD = 0.5
HIGH_THRESHOLD = 0.8


def to_vector(f: TransferFeatures) -> list[float]:
    """Turns features into numbers the model can use (logs tame very large values)."""
    ratio = f.amount / f.avg_transfer_amount if f.avg_transfer_amount > 0 else 1.0
    return [
        math.log1p(f.amount),
        math.sin(2 * math.pi * f.hour_of_day / 24),  # hour as a circle: 23:00 is close to 00:00
        math.cos(2 * math.pi * f.hour_of_day / 24),
        math.log1p(f.account_age_days),
        float(f.transfers_last_day),
        math.log1p(ratio),
        1.0 if f.new_recipient else 0.0,
    ]


def generate_normal_transfers(n: int, seed: int = 42) -> np.ndarray:
    """Synthetic 'normal' customer behaviour used to train the model."""
    rng = np.random.default_rng(seed)
    amounts = rng.lognormal(mean=math.log(2000), sigma=0.9, size=n)
    hours = np.clip(rng.normal(loc=15, scale=3.5, size=n).round(), 6, 23).astype(int)
    # Most customers are established, but some accounts are brand new
    ages = np.where(rng.random(n) < 0.15, rng.uniform(0, 7, size=n), rng.uniform(7, 2000, size=n))
    counts = rng.poisson(lam=1.2, size=n)
    avgs = amounts / rng.lognormal(mean=0, sigma=0.4, size=n)
    avgs = np.where(rng.random(n) < 0.1, 0.0, avgs)  # first-ever transfer: no history yet
    new_recipient = rng.random(n) < 0.3

    rows = [
        to_vector(TransferFeatures(
            amount=float(a), hour_of_day=int(h), account_age_days=float(age),
            transfers_last_day=int(c), avg_transfer_amount=float(avg), new_recipient=bool(nr),
        ))
        for a, h, age, c, avg, nr in zip(amounts, hours, ages, counts, avgs, new_recipient)
    ]
    return np.array(rows)


class FraudModel:
    def __init__(self, forest: IsolationForest, score_low: float, score_high: float):
        self.forest = forest
        # Raw scores seen in training, used to map any raw score onto 0..1
        self.score_low = score_low
        self.score_high = score_high

    @classmethod
    def train(cls, n: int = 5000, seed: int = 42) -> "FraudModel":
        data = generate_normal_transfers(n, seed)
        forest = IsolationForest(n_estimators=200, contamination="auto", random_state=seed)
        forest.fit(data)
        raw = -forest.score_samples(data)  # higher = more anomalous
        return cls(forest, float(np.percentile(raw, 50)), float(np.percentile(raw, 99.9)))

    @classmethod
    def load_or_train(cls, path: Path = MODEL_PATH) -> "FraudModel":
        if path.exists():
            return joblib.load(path)
        model = cls.train()
        path.parent.mkdir(parents=True, exist_ok=True)
        joblib.dump(model, path)
        return model

    def ml_score(self, f: TransferFeatures) -> float:
        raw = -self.forest.score_samples(np.array([to_vector(f)]))[0]
        # Median normal transfer -> 0, the rarest 0.1% of normal transfers -> ~0.6
        scaled = (raw - self.score_low) / (self.score_high - self.score_low) * 0.6
        return float(min(max(scaled, 0.0), 1.0))

    def assess(self, f: TransferFeatures) -> RiskAssessment:
        score = self.ml_score(f)
        reasons, rule_score = explain(f)
        final = max(score, rule_score)

        if final >= HIGH_THRESHOLD:
            level = RiskLevel.HIGH
        elif final >= MEDIUM_THRESHOLD:
            level = RiskLevel.MEDIUM
        else:
            level = RiskLevel.LOW

        if level != RiskLevel.LOW and score >= MEDIUM_THRESHOLD:
            reasons.insert(0, "Unusual pattern compared with normal customer behaviour (ML model)")

        return RiskAssessment(risk_score=round(final, 3), risk_level=level,
                              reasons=reasons, model_version=MODEL_VERSION)


def explain(f: TransferFeatures) -> tuple[list[str], float]:
    """Explainable rules. Each one adds weight; the total is capped at 1."""
    reasons: list[str] = []
    weight = 0.0

    if f.amount >= 200_000:
        reasons.append("Very large amount (₹2,00,000 or more)")
        weight += 0.45
    elif f.amount >= 50_000:
        reasons.append("Large amount (₹50,000 or more)")
        weight += 0.2

    if f.avg_transfer_amount > 0 and f.amount >= 10 * f.avg_transfer_amount and f.amount >= 10_000:
        reasons.append("Amount is over 10x this account's usual transfer")
        weight += 0.3

    if f.transfers_last_day >= 10:
        reasons.append("Many transfers in the last 24 hours")
        weight += 0.3

    if f.hour_of_day < 5:
        reasons.append("Made at an unusual time (midnight to 5 AM)")
        weight += 0.15

    if f.account_age_days < 1 and f.amount >= 20_000:
        reasons.append("Large transfer from an account opened today")
        weight += 0.3

    if f.new_recipient and f.amount >= 50_000:
        reasons.append("Large amount to a new recipient")
        weight += 0.15

    return reasons, min(weight, 1.0)
