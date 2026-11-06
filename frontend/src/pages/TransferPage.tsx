import { FormEvent, useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Account, api, ApiError, TransferResult } from "../api";
import { Stamp } from "../components/Stamp";
import { formatMoney, parseAmount } from "../format";

type Outcome =
  | { kind: "done"; result: TransferResult; amount: number; from: string; to: string }
  | { kind: "blocked"; reasons: string[]; amount: number; from: string; to: string };

export default function TransferPage() {
  const [params] = useSearchParams();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [from, setFrom] = useState(params.get("from") ?? "");
  const [to, setTo] = useState("");
  const [amount, setAmount] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [outcome, setOutcome] = useState<Outcome | null>(null);

  useEffect(() => {
    api.listAccounts().then(setAccounts).catch((e: ApiError) => setError(e.message));
  }, []);

  const nameOf = (no: string) => accounts.find((a) => a.accountNumber === no)?.accountHolder ?? no;

  async function submit(event: FormEvent) {
    event.preventDefault();
    const value = parseAmount(amount);
    if (value === null) {
      setError("Enter an amount greater than zero, with up to 2 decimal places.");
      return;
    }
    if (from === to) {
      setError("Choose two different accounts.");
      return;
    }
    setBusy(true);
    setError(null);
    setOutcome(null);
    try {
      const result = await api.transfer(from, to, value);
      setOutcome({ kind: "done", result, amount: value, from, to });
      setAmount("");
      setAccounts(await api.listAccounts());
    } catch (e) {
      const err = e as ApiError;
      if (err.status === 403) {
        setOutcome({ kind: "blocked", reasons: err.reasons, amount: value, from, to });
      } else {
        setError(err.message);
      }
    } finally {
      setBusy(false);
    }
  }

  const source = accounts.find((a) => a.accountNumber === from);

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Transfer money</h1>
          <p className="lede">
            Before any money moves, an AI model scores the transfer against the sender's usual behaviour. High-risk
            transfers are blocked; unusual ones go through and are flagged for review.
          </p>
        </div>
      </div>

      <div className="slip-layout">
        <form className="slip" onSubmit={submit}>
          <div className="field">
            <label htmlFor="from">From</label>
            <select id="from" value={from} onChange={(e) => setFrom(e.target.value)} required>
              <option value="">Choose an account</option>
              {accounts.map((a) => (
                <option key={a.accountNumber} value={a.accountNumber}>
                  {a.accountHolder} ({a.accountNumber})
                </option>
              ))}
            </select>
            {source && <span className="hint">Balance {formatMoney(Number(source.balance))}</span>}
          </div>

          <div className="field">
            <label htmlFor="to">To</label>
            <select id="to" value={to} onChange={(e) => setTo(e.target.value)} required>
              <option value="">Choose an account</option>
              {accounts
                .filter((a) => a.accountNumber !== from)
                .map((a) => (
                  <option key={a.accountNumber} value={a.accountNumber}>
                    {a.accountHolder} ({a.accountNumber})
                  </option>
                ))}
            </select>
          </div>

          <div className="field">
            <label htmlFor="amount">Amount</label>
            <input
              id="amount"
              inputMode="decimal"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="₹ 0.00"
              required
            />
            <span className="hint">Try ₹60,000 or ₹2,50,000 to see the fraud check react.</span>
          </div>

          <button type="submit" disabled={busy || !from || !to}>
            {busy ? "Checking and sending…" : "Send money"}
          </button>
          {error && <p className="note note--error">{error}</p>}
        </form>

        <section className="verdict" aria-live="polite" aria-label="Fraud check result">
          {!outcome && (
            <p className="muted verdict__placeholder">
              The fraud check's decision and its reasons will appear here after you send money.
            </p>
          )}

          {outcome && (
            <div className="verdict__card" key={`${outcome.from}-${outcome.to}-${outcome.amount}-${outcome.kind}`}>
              <Stamp
                verdict={outcome.kind === "blocked" ? "BLOCKED" : outcome.result.riskLevel}
                score={outcome.kind === "done" ? outcome.result.riskScore : undefined}
              />
              <p className="verdict__summary">
                <span className="num">{formatMoney(outcome.amount)}</span> from {nameOf(outcome.from)} to{" "}
                {nameOf(outcome.to)}{" "}
                {outcome.kind === "blocked"
                  ? "was not sent. No money left the account."
                  : outcome.result.riskLevel === "MEDIUM"
                    ? "was sent and flagged for review."
                    : "was sent."}
              </p>

              {(() => {
                const reasons = outcome.kind === "blocked" ? outcome.reasons : outcome.result.reasons;
                if (reasons.length === 0) return <p className="muted">Nothing unusual about this transfer.</p>;
                return (
                  <>
                    <h2 className="verdict__why">Why</h2>
                    <ul className="reasons">
                      {reasons.map((r) => (
                        <li key={r}>{r}</li>
                      ))}
                    </ul>
                  </>
                );
              })()}

              {outcome.kind === "blocked" || outcome.result.riskLevel === "MEDIUM" ? (
                <Link to="/alerts">See all fraud alerts</Link>
              ) : (
                <Link to={`/accounts/${outcome.from}`}>View sender's passbook</Link>
              )}
            </div>
          )}
        </section>
      </div>
    </>
  );
}
