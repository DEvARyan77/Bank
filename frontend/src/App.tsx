import { NavLink, Route, Routes } from "react-router-dom";
import AccountsPage from "./pages/AccountsPage";
import AccountPage from "./pages/AccountPage";
import TransferPage from "./pages/TransferPage";
import AlertsPage from "./pages/AlertsPage";

export default function App() {
  return (
    <div className="shell">
      <header className="rail">
        <NavLink to="/" className="brand" end>
          <span className="brand__mark" aria-hidden="true">M</span>
          MiniBank
        </NavLink>
        <nav className="rail__nav" aria-label="Main">
          <NavLink to="/" end>Accounts</NavLink>
          <NavLink to="/transfer">Transfer</NavLink>
          <NavLink to="/alerts">Fraud alerts</NavLink>
        </nav>
        <p className="rail__note">Every transfer is scored by an AI fraud model before money moves.</p>
      </header>

      <main className="main">
        <Routes>
          <Route path="/" element={<AccountsPage />} />
          <Route path="/accounts/:accountNumber" element={<AccountPage />} />
          <Route path="/transfer" element={<TransferPage />} />
          <Route path="/alerts" element={<AlertsPage />} />
          <Route path="*" element={<p className="empty">This page doesn't exist. Use the menu to go back.</p>} />
        </Routes>
      </main>
    </div>
  );
}
