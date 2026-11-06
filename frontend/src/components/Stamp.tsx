import type { RiskLevel } from "../api";

const LABELS: Record<RiskLevel | "BLOCKED", { text: string; tone: string }> = {
  LOW: { text: "Cleared", tone: "cleared" },
  MEDIUM: { text: "Flagged", tone: "flagged" },
  HIGH: { text: "Blocked", tone: "blocked" },
  BLOCKED: { text: "Blocked", tone: "blocked" },
  UNCHECKED: { text: "Not checked", tone: "unchecked" },
};

/** The AI fraud verdict, drawn like a bank's rubber stamp. */
export function Stamp({ verdict, score, small = false }: { verdict: RiskLevel | "BLOCKED"; score?: number; small?: boolean }) {
  const { text, tone } = LABELS[verdict];
  return (
    <span className={`stamp stamp--${tone}${small ? " stamp--small" : ""}`} role="status">
      <span className="stamp__text">{text}</span>
      {score !== undefined && verdict !== "UNCHECKED" && (
        <span className="stamp__score">Risk {Math.round(score * 100)}%</span>
      )}
    </span>
  );
}
