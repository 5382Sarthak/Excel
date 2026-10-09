
import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api";

const sortOptions = [
  ["mostUsed", "Most used APIs"],
  ["leastUsed", "Least used APIs"],
  ["mostFailed", "Most failed calls"],
  ["leastFailed", "Least failed calls"],
  ["successRateHigh", "Highest success rate"],
  ["successRateLow", "Lowest success rate"],
  ["failureRateHigh", "Highest failure rate"],
  ["failureRateLow", "Lowest failure rate"],
];

export default function Dashboard() {
  const navigate = useNavigate();
  const [data, setData] = useState([]);
  const [sortBy, setSortBy] = useState("mostUsed");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  const loadData = useCallback(async () => {
    setLoading(true);
    setMessage("");

    try {
      const sessionResponse = await api("/check-session");
      const loggedIn =
        sessionResponse.ok && await sessionResponse.json();

      if (!loggedIn) {
        navigate("/login", { replace: true });
        return;
      }

      const response = await api(
        `/view?sortBy=${encodeURIComponent(sortBy)}`
      );

      if (response.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      if (!response.ok) {
        throw new Error("Could not retrieve dashboard data.");
      }

      const result = await response.json();
      setData(Array.isArray(result) ? result : []);
    } catch (error) {
      setMessage(error.message || "Unable to load dashboard data.");
    } finally {
      setLoading(false);
    }
  }, [navigate, sortBy]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  async function logout() {
    try {
      await api("/logout");
    } finally {
      navigate("/login", { replace: true });
    }
  }

  const totalCalls = data.reduce(
    (sum, item) => sum + (Number(item.totalCalls) || 0), 0
  );

  const totalSuccess = data.reduce(
    (sum, item) => sum + (Number(item.successCalls) || 0), 0
  );

  const totalFailed = data.reduce(
    (sum, item) => sum + (Number(item.failedCalls) || 0), 0
  );

  return (
    <main className="page-shell dashboard-shell">
      <section className="panel dashboard-panel">
        <header className="topbar">
          <div>
            <div className="eyebrow">EXCEL ANALYTICS</div>
            <h1>API Usage Dashboard</h1>
            <p className="subtitle">
              Review usage, successful calls, and failed calls.
            </p>
          </div>

          <button className="secondary-button logout-button" onClick={logout}>
            Logout
          </button>
        </header>

        <div className="dashboard-actions">
          <Link className="text-link" to="/upload">
            ← Back to upload
          </Link>

          <div className="sort-control">
            <label htmlFor="sortBy">Sort data</label>
            <select
              id="sortBy"
              value={sortBy}
              onChange={(event) => setSortBy(event.target.value)}
            >
              {sortOptions.map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <button
            className="secondary-button"
            onClick={loadData}
            disabled={loading}
          >
            Refresh
          </button>
        </div>

        <div className="stats-grid">
          <article className="stat-card">
            <span>Total API calls</span>
            <strong>{totalCalls.toLocaleString()}</strong>
          </article>

          <article className="stat-card">
            <span>Successful calls</span>
            <strong>{totalSuccess.toLocaleString()}</strong>
          </article>

          <article className="stat-card">
            <span>Failed calls</span>
            <strong>{totalFailed.toLocaleString()}</strong>
          </article>

          <article className="stat-card">
            <span>API keys</span>
            <strong>{data.length.toLocaleString()}</strong>
          </article>
        </div>

        {message && (
          <p className="status-message" role="status">{message}</p>
        )}

        <div className="table-heading">
          <h2>API usage records</h2>
          <span>{data.length} records</span>
        </div>

        {loading ? (
          <p className="empty-state">Loading dashboard data...</p>
        ) : data.length === 0 ? (
          <p className="empty-state">
            No records found. Upload an Excel file to get started.
          </p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>API Key</th>
                  <th>Total Calls</th>
                  <th>Successful</th>
                  <th>Failed</th>
                  <th>Success %</th>
                  <th>Failure %</th>
                </tr>
              </thead>

              <tbody>
                {data.map((item, index) => (
                  <tr key={item.id || item.apiKey || index}>
                    <td className="api-key-cell">{item.apiKey ?? "—"}</td>
                    <td>{item.totalCalls ?? 0}</td>
                    <td>{item.successCalls ?? 0}</td>
                    <td>{item.failedCalls ?? 0}</td>
                    <td>{Number(item.successPercent ?? 0).toFixed(2)}%</td>
                    <td>{Number(item.failedPercent ?? 0).toFixed(2)}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}