from enum import Enum

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    """Accepts and returns camelCase JSON so it matches the Java service."""

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class TransferFeatures(CamelModel):
    amount: float = Field(gt=0, description="Transfer amount in rupees")
    hour_of_day: int = Field(ge=0, le=23, description="Local hour when the transfer was made")
    account_age_days: float = Field(ge=0, description="How old the sending account is, in days")
    transfers_last_day: int = Field(ge=0, description="Transfers sent by this account in the last 24 hours")
    avg_transfer_amount: float = Field(ge=0, description="Average past transfer amount (0 if none)")
    new_recipient: bool = Field(description="True if this account has never sent money to the recipient")


class RiskLevel(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class RiskAssessment(CamelModel):
    risk_score: float = Field(ge=0, le=1, description="0 = normal, 1 = highly suspicious")
    risk_level: RiskLevel
    reasons: list[str]
    model_version: str
