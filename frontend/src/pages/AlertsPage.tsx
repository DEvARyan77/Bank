import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError, FraudAlert, Page } from "../api";
import { Stamp } from "../components/Stamp";
import { formatDateTime, formatMoney } from "../format";

export default function AlertsPage() {
  const [alerts, setAlerts] = useState<Page<FraudAlert> | null>(null);
  const [page, setPage] = useState(0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.fraudAlerts(page).then(setAlerts).catch((e: ApiError) => setError(e.message));
  }, [page]);

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Fraud alerts</h1>
          <p className="lede">Transfers the AI model blocked or flagged, newest first.</p>
        </div>
      </div>

      {error && <p className="note note--error">{error}</p>}
      {!alerts && !error && <p className="muted">Loading alerts…</p>}

      {alerts?.totalElements === 0 && (
        <p className="empty">
          No alerts yet. Transfers that look unusual will be listed here. <Link to="/transfer">Make a transfer</Link>
        </p>
      )}

      {alerts && alerts.totalElements > 0 && (
        <ul className="alert-list">
          {alerts.content.map((a) => (
            <li key={a.id} className="alert">
              <Stamp verdict={a.decision === "BLOCKED" ? "BLOCKED" : a.riskLevel} score={a.riskScore} small />
              <div className="alert__body">
                <p className="alert__title">
                  <span className="num">{formatMoney(Number(a.amount))}</span> from{" "}
                  <Link to={`/accounts/${a.fromAccountNumber}`}>{a.fromAccountNumber}</Link> to{" "}
                  <Link to={`/accounts/${a.toAccountNumber}`}>{a.toAccountNumber}</Link>
                </p>
                <p className="muted">{formatDateTime(a.createdAt)}</p>
                <ul className="reasons">
                  {a.reasons.map((r) => (
                    <li key={r}>{r}</li>
                  ))}
                </ul>
              </div>
            </li>
          ))}
        </ul>
      )}

      {alerts && alerts.totalPages > 1 && (
        <div className="pager">
          <button className="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Newer</button>
          <span className="muted">Page {page + 1} of {alerts.totalPages}</span>
          <button className="secondary" disabled={page + 1 >= alerts.totalPages} onClick={() => setPage(page + 1)}>
            Older
          </button>
        </div>
      )}
    </>
  );
}
