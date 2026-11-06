import pytest
from fastapi.testclient import TestClient

from app.main import app


@pytest.fixture(scope="module")
def client():
    with TestClient(app) as c:  # runs startup, so the model is loaded
        yield c


def score(client, **overrides):
    body = {
        "amount": 1500,
        "hourOfDay": 14,
        "accountAgeDays": 400,
        "transfersLastDay": 1,
        "avgTransferAmount": 1800,
        "newRecipient": False,
    }
    body.update(overrides)
    response = client.post("/score", json=body)
    assert response.status_code == 200, response.text
    return response.json()


def test_health(client):
    assert client.get("/health").json()["status"] == "UP"


def test_normal_transfer_is_low_risk(client):
    result = score(client)
    assert result["riskLevel"] == "LOW"
    assert result["reasons"] == []


def test_obvious_fraud_is_high_risk(client):
    result = score(client, amount=250_000, hourOfDay=3, accountAgeDays=0.1,
                   transfersLastDay=12, avgTransferAmount=1000, newRecipient=True)
    assert result["riskLevel"] == "HIGH"
    assert result["riskScore"] >= 0.8
    assert any("large" in reason.lower() for reason in result["reasons"])


def test_unusually_large_transfer_is_at_least_medium(client):
    result = score(client, amount=60_000, avgTransferAmount=2000, newRecipient=True)
    assert result["riskLevel"] in ("MEDIUM", "HIGH")
    assert result["reasons"]


def test_first_ever_transfer_has_no_average(client):
    result = score(client, amount=1000, avgTransferAmount=0, transfersLastDay=0)
    assert result["riskLevel"] == "LOW"


def test_invalid_input_is_rejected(client):
    response = client.post("/score", json={"amount": -5, "hourOfDay": 30})
    assert response.status_code == 422
