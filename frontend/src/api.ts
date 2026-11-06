const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? "http://localhost:8080";

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH" | "UNCHECKED";
export type TransactionType = "DEPOSIT" | "WITHDRAWAL" | "TRANSFER_IN" | "TRANSFER_OUT";

export interface Account {
  accountNumber: string;
  accountHolder: string;
  balance: number;
  createdAt: string;
}

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  balanceAfter: number;
  counterpartyAccountNumber: string | null;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface TransferResult {
  status: string;
  riskLevel: RiskLevel;
  riskScore: number;
  reasons: string[];
}

export interface FraudAlert {
  id: number;
  fromAccountNumber: string;
  toAccountNumber: string;
  amount: number;
  riskScore: number;
  riskLevel: RiskLevel;
  decision: "BLOCKED" | "FLAGGED";
  reasons: string[];
  createdAt: string;
}

export class ApiError extends Error {
  constructor(message: string, readonly status: number, readonly reasons: string[] = []) {
    super(message);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(BASE_URL + path, {
      ...init,
      headers: { "Content-Type": "application/json", ...init?.headers },
    });
  } catch {
    throw new ApiError(`Can't reach the MiniBank API at ${BASE_URL}. Check that the backend is running.`, 0);
  }

  const text = await response.text();
  const body = text ? JSON.parse(text) : null;
  if (!response.ok) {
    // Errors come back as RFC 9457 problem details: { title, status, detail, reasons? }
    throw new ApiError(body?.detail ?? `Request failed (${response.status})`, response.status, body?.reasons ?? []);
  }
  return body as T;
}

const post = (body: unknown): RequestInit => ({ method: "POST", body: JSON.stringify(body) });

export const api = {
  listAccounts: () => request<Account[]>("/api/accounts"),
  getAccount: (no: string) => request<Account>(`/api/accounts/${no}`),
  createAccount: (accountHolder: string) => request<Account>("/api/accounts", post({ accountHolder })),
  deposit: (no: string, amount: number) => request<Account>(`/api/accounts/${no}/deposit`, post({ amount })),
  withdraw: (no: string, amount: number) => request<Account>(`/api/accounts/${no}/withdraw`, post({ amount })),
  transactions: (no: string, page: number) =>
    request<Page<Transaction>>(`/api/accounts/${no}/transactions?page=${page}&size=10`),
  transfer: (fromAccount: string, toAccount: string, amount: number) =>
    request<TransferResult>("/api/transfers", post({ fromAccount, toAccount, amount })),
  fraudAlerts: (page: number) => request<Page<FraudAlert>>(`/api/fraud-alerts?page=${page}&size=20`),
};
