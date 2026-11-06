import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Account, api, ApiError } from "../api";
import { formatDate, formatMoney } from "../format";

export default function AccountsPage() {
  const [accounts, setAccounts] = useState<Account[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [creating, setCreating] = useState(false);
  const [created, setCreated] = useState<string | null>(null);

  const load = () =>
    api.listAccounts().then(setAccounts).catch((e: ApiError) => setError(e.message));

  useEffect(() => {
    load();
  }, []);

  async function createAccount(event: FormEvent) {
    event.preventDefault();
    setCreating(true);
    setError(null);
    try {
      const account = await api.createAccount(name.trim());
      setCreated(account.accountNumber);
      setName("");
      await load();
    } catch (e) {
      setError((e as ApiError).message);
    } finally {
      setCreating(false);
    }
  }

  const total = accounts?.reduce((sum, a) => sum + Number(a.balance), 0) ?? 0;

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Accounts</h1>
          {accounts && (
            <p className="lede">
              {accounts.length} {accounts.length === 1 ? "account" : "accounts"} holding{" "}
              <strong className="num">{formatMoney(total)}</strong>
            </p>
          )}
        </div>
      </div>

      <form className="inline-form" onSubmit={createAccount}>
        <label htmlFor="holder">Open a new account</label>
        <div className="inline-form__row">
          <input
            id="holder"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Account holder's full name"
            maxLength={100}
            required
          />
          <button type="submit" disabled={creating || !name.trim()}>
            {creating ? "Opening…" : "Open account"}
          </button>
        </div>
        {created && <p className="note note--ok">Opened account {created}.</p>}
      </form>

      {error && <p className="note note--error">{error}</p>}

      {accounts === null && !error && <p className="muted">Loading accounts…</p>}

      {accounts?.length === 0 && (
        <p className="empty">No accounts yet. Open one above to start depositing and transferring money.</p>
      )}

      {accounts && accounts.length > 0 && (
        <ul className="account-list">
          {accounts.map((account) => (
            <li key={account.accountNumber}>
              <Link to={`/accounts/${account.accountNumber}`} className="account-row">
                <span className="account-row__who">
                  <span className="account-row__name">{account.accountHolder}</span>
                  <span className="account-row__meta">
                    {account.accountNumber}, opened {formatDate(account.createdAt)}
                  </span>
                </span>
                <span className="account-row__balance num">{formatMoney(Number(account.balance))}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
