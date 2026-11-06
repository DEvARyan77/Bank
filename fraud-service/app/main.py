from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.model import MODEL_VERSION, FraudModel
from app.schemas import RiskAssessment, TransferFeatures

models: dict[str, FraudModel] = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    models["fraud"] = FraudModel.load_or_train()
    yield
    models.clear()


app = FastAPI(
    title="MiniBank Fraud Detection",
    description="Scores transfers for fraud risk using an Isolation Forest model and explainable rules.",
    version="1.0.0",
    lifespan=lifespan,
)


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "modelVersion": MODEL_VERSION}


@app.post("/score", response_model=RiskAssessment)
def score(features: TransferFeatures) -> RiskAssessment:
    return models["fraud"].assess(features)
