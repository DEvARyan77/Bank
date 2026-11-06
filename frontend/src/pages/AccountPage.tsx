import { FormEvent, useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Account, api, ApiError, Page, Transaction } from "../api";
import { formatDateTime, formatMoney, parseAmount } from "../format";

function particulars(t: Transaction): string {
  switch (t.type) {
    case "DEPOSIT":
      return "Cash deposit";
    case "WITHDRAWAL":
      return "Cash withdrawal";
    case "TRANSFER_IN":
      return `Transfer from ${t.counterpartyAccountNumber}`;
    case "TRANSFER_OUT":
      return `Transfer to ${t.counterpartyAccountNumber}`;
  }
}

const isCredit = (t: Transaction) => t.type === "DEPOSIT" || t.type === "TRANSFER_IN";

export default function AccountPage() {
  const { accountNumber = "" } = useParams();
  const [account, setAccount] = useState<Account | null>(null);
  const [history, setHistory] = useState<Page<Transaction> | null>(null);
  const [page, setPage] = useState(0);
  const [amount, setAmount] = useState("");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<{ ok: boolean; text: string } | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const [a, h] = await Promise.all([api.getAccount(accountNumber), api.transactions(accountNumber, page)]);
      setAccount(a);
      setHistory(h);
    } catch (e) {
      setLoadError((e as ApiError).message);
    }
  }, [accountNumber, page]);

  useEffect(() => {
    load();
  }, [load]);

  async function move(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const action = ((event.nativeEvent as SubmitEvent).submitter as HTMLButtonElement | null)?.value;
    const value = parseAmount(amount);
    if (value === null) {
      setMessage({ ok: false, text: "Enter an amount greater than zero, with up to 2 decimal places." });
      return;
    }
    setBusy(true);
    setMessage(null);
    try {
      if (action === "withdraw") {
        await api.withdraw(accountNumber, value);
        setMessage({ ok: true, text: `Withdrew ${formatMoney(value)}.` });
      } else {
        await api.deposit(accountNumber, value);
        setMessage({ ok: true, text: `Deposited ${formatMoney(value)}.` });
      }
      setAmount("");
      setPage(0);
      await load();
    } catch (e) {
      setMessage({ ok: false, text: (e as ApiError).message });
    } finally {
      setBusy(false);
    }
  }

  if (loadError) {
    return (
      <>
        <p className="note note--error">{loadError}</p>
        <Link to="/">Back to accounts</Link>
      </>
    );
  }
  if (!account) return <p className="muted">Loading account…</p>;

  return (
    <>
      <Link to="/" className="back">All accounts</Link>

      <section className="passbook-cover" aria-label="Account summary">
        <div>
          <h1>{account.accountHolder}</h1>
          <p className="muted">Account {account.accountNumber}</p>
        </div>
        <div className="passbook-cover__balance">
          <span className="muted">Available balance</span>
          <span className="big-num num">{formatMoney(Number(account.balance))}</span>
        </div>
      </section>

      <form className="inline-form" onSubmit={move}>
        <label htmlFor="amount">Deposit or withdraw cash</label>
        <div className="inline-form__row">
          <input
            id="amount"
            inputMode="decimal"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Amount in ₹"
            required
          />
          <button type="submit" value="deposit" disabled={busy}>Deposit</button>
          <button type="submit" value="withdraw" className="secondary" disabled={busy}>Withdraw</button>
          <Link to={`/transfer?from=${account.accountNumber}`} className="button-link">Transfer</Link>
        </div>
        {message && <p className={`note ${message.ok ? "note--ok" : "note--error"}`}>{message.text}</p>}
      </form>

      <section aria-labelledby="passbook-title">
        <h2 id="passbook-title">Passbook</h2>
        {history && history.totalElements === 0 ? (
          <p className="empty">No transactions yet. Deposit money above to make the first entry.</p>
        ) : (
          <div className="table-wrap">
            <table className="passbook">
              <thead>
                <tr>
                  <th scope="col">Date</th>
                  <th scope="col">Particulars</th>
                  <th scope="col" className="r">Withdrawal</th>
                  <th scope="col" className="r">Deposit</th>
                  <th scope="col" className="r">Balance</th>
                </tr>
              </thead>
              <tbody>
                {history?.content.map((t) => (
                  <tr key={t.id}>
                    <td className="nowrap">{formatDateTime(t.createdAt)}</td>
                    <td>{particulars(t)}</td>
                    <td className="r num debit">{isCredit(t) ? "" : formatMoney(Number(t.amount))}</td>
                    <td className="r num credit">{isCredit(t) ? formatMoney(Number(t.amount)) : ""}</td>
                    <td className="r num">{formatMoney(Number(t.balanceAfter))}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {history && history.totalPages > 1 && (
          <div className="pager">
            <button className="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Newer</button>
            <span className="muted">Page {page + 1} of {history.totalPages}</span>
            <button className="secondary" disabled={page + 1 >= history.totalPages} onClick={() => setPage(page + 1)}>
              Older
            </button>
          </div>
        )}
      </section>
    </>
  );
}
