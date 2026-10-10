
import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ReferenceLine,
} from "recharts";
import api from "../api";
import "./Dashboard.css";

const emptyAnalytics = {
  summary: {
    totalRevenue: 0,
    totalProfit: 0,
    totalCost: 0,
    totalUnitsSold: 0,
    totalSalesRecords: 0,
    averageRevenuePerRecord: 0,
  },
  monthlyRevenue: [],
  productRevenue: [],
  regionComparison: [],
};

const currency = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(Number(value) || 0);

const number = (value) =>
  new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 2,
  }).format(Number(value) || 0);

const compactCurrency = (value) => {
  const amount = Number(value) || 0;
  const absolute = Math.abs(amount);

  if (absolute >= 10000000) {
    return `₹${(amount / 10000000).toFixed(1)}Cr`;
  }
  if (absolute >= 100000) {
    return `₹${(amount / 100000).toFixed(1)}L`;
  }
  if (absolute >= 1000) {
    return `₹${(amount / 1000).toFixed(1)}K`;
  }
  return `₹${amount.toFixed(0)}`;
};

function ChartCard({ title, subtitle, children, className = "" }) {
  return (
    <article className={`chart-card ${className}`}>
      <div className="chart-card-heading">
        <h2>{title}</h2>
        <p>{subtitle}</p>
      </div>
      {children}
    </article>
  );
}

function EmptyChart({ message }) {
  return <div className="chart-empty">{message}</div>;
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [analytics, setAnalytics] = useState(emptyAnalytics);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  const loadData = useCallback(async () => {
    setLoading(true);
    setMessage("");

    try {
      const sessionResponse = await api("/check-session");
      const loggedIn =
        sessionResponse.ok && (await sessionResponse.json());

      if (!loggedIn) {
        navigate("/login", { replace: true });
        return;
      }

      const response = await api("/view");

      if (response.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      if (!response.ok) {
        throw new Error("Could not retrieve sales analytics.");
      }

      const result = await response.json();

      setAnalytics({
        ...emptyAnalytics,
        ...result,
        summary: {
          ...emptyAnalytics.summary,
          ...(result.summary || {}),
        },
        monthlyRevenue: Array.isArray(result.monthlyRevenue)
          ? result.monthlyRevenue
          : [],
        productRevenue: Array.isArray(result.productRevenue)
          ? result.productRevenue
          : [],
        regionComparison: Array.isArray(result.regionComparison)
          ? result.regionComparison
          : [],
      });
    } catch (error) {
      setMessage(error.message || "Unable to load sales analytics.");
    } finally {
      setLoading(false);
    }
  }, [navigate]);

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

  const summary = analytics.summary;
  const hasData = Number(summary.totalSalesRecords) > 0;

  // Display revenue on the left and profit on the right.
  // Negative revenue values create the mirrored butterfly layout.
  const regionalData = analytics.regionComparison.map((item) => ({
    ...item,
    revenueMirror: -Math.abs(Number(item.revenue) || 0),
    profit: Number(item.profit) || 0,
  }));

  const maxRegionValue = Math.max(
    1,
    ...regionalData.map((item) =>
      Math.max(
        Math.abs(item.revenueMirror),
        Math.abs(item.profit)
      )
    )
  );

  return (
    <main className="page-shell sales-dashboard-shell">
      <section className="panel sales-dashboard-panel" >
        <header className="sales-topbar">
          <div>
            <div className="eyebrow">BUSINESS INTELLIGENCE</div>
            <h1>Sales &amp; Revenue Analytics</h1>
            <p className="sales-subtitle">
              Understand revenue, profit, products, and regional performance.
            </p>
          </div>

          <div className="sales-header-actions">
            <button
              className="secondary-button"
              onClick={loadData}
              disabled={loading}
            >
              {loading ? "Refreshing..." : "↻ Refresh"}
            </button>

            <button
              className="secondary-button logout-button"
              onClick={logout}
            >
              Logout
            </button>
          </div>
        </header>

        <nav className="sales-nav">
          <Link to="/dashboard" className="sales-nav-active">
            Overview
          </Link>
          <Link to="/upload">Import sales data ↗</Link>
        </nav>

        {message && (
          <p className="status-message" role="alert">
            {message}
          </p>
        )}

        {loading && !hasData ? (
          <div className="sales-loading">Loading sales analytics...</div>
        ) : !hasData ? (
          <div className="sales-empty">
            <div className="sales-empty-icon">↗</div>
            <h2>Your sales dashboard is ready</h2>
            <p>
              Import an Excel spreadsheet containing sales data to see
              revenue trends, product performance, and regional comparisons.
            </p>
            <Link className="primary-button sales-import-button" to="/upload">
              Import sales spreadsheet
            </Link>
          </div>
        ) : (
          <>
            <section className="sales-stats-grid">
              <article className="sales-stat-card revenue-stat">
                <span className="sales-stat-label">Total revenue</span>
                <strong>{currency(summary.totalRevenue)}</strong>
                <span className="sales-stat-note">Across imported records</span>
              </article>

              <article className="sales-stat-card profit-stat">
                <span className="sales-stat-label">Total profit</span>
                <strong>{currency(summary.totalProfit)}</strong>
                <span className="sales-stat-note">
                  Revenue minus recorded costs
                </span>
              </article>

              <article className="sales-stat-card cost-stat">
                <span className="sales-stat-label">Total cost</span>
                <strong>{currency(summary.totalCost)}</strong>
                <span className="sales-stat-note">Calculated from unit costs</span>
              </article>

              <article className="sales-stat-card units-stat">
                <span className="sales-stat-label">Units sold</span>
                <strong>{number(summary.totalUnitsSold)}</strong>
                <span className="sales-stat-note">
                  {number(summary.totalSalesRecords)} sales records
                </span>
              </article>
            </section>

            <section className="sales-charts-grid">
              <ChartCard
                title="Revenue & profit trends"
                subtitle="Monthly performance across imported sales data"
                className="chart-wide"
              >
                {analytics.monthlyRevenue.length ? (
                  <ResponsiveContainer width="100%" height={310}>
                    <LineChart
                      data={analytics.monthlyRevenue}
                      margin={{ top: 12, right: 18, left: 8, bottom: 4 }}
                    >
                      <CartesianGrid
                        stroke="#e8edf0"
                        strokeDasharray="3 3"
                        vertical={false}
                      />
                      <XAxis
                        dataKey="month"
                        tick={{ fill: "#64748b", fontSize: 12 }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <YAxis
                        tickFormatter={compactCurrency}
                        tick={{ fill: "#64748b", fontSize: 12 }}
                        axisLine={false}
                        tickLine={false}
                        width={72}
                      />
                      <Tooltip
                        formatter={(value, name) => [
                          currency(value),
                          name === "revenue" ? "Revenue" : "Profit",
                        ]}
                        contentStyle={{
                          borderRadius: 10,
                          border: "1px solid #e2e8f0",
                        }}
                      />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="revenue"
                        name="Revenue"
                        stroke="#287d8e"
                        strokeWidth={3}
                        dot={{ r: 3 }}
                        activeDot={{ r: 6 }}
                      />
                      <Line
                        type="monotone"
                        dataKey="profit"
                        name="Profit"
                        stroke="#72a88a"
                        strokeWidth={3}
                        dot={{ r: 3 }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <EmptyChart message="No valid sales dates available for the monthly chart." />
                )}
              </ChartCard>

              <ChartCard
                title="Revenue by product"
                subtitle="Products ranked by total revenue"
              >
                {analytics.productRevenue.length ? (
                  <ResponsiveContainer
                    width="100%"
                    height={Math.max(
                      300,
                      Math.min(460, analytics.productRevenue.length * 44)
                    )}
                  >
                    <BarChart
                      data={analytics.productRevenue.slice(0, 10)}
                      layout="vertical"
                      margin={{ top: 4, right: 20, left: 8, bottom: 4 }}
                    >
                      <CartesianGrid
                        stroke="#e8edf0"
                        strokeDasharray="3 3"
                        horizontal={false}
                      />
                      <XAxis
                        type="number"
                        tickFormatter={compactCurrency}
                        tick={{ fill: "#64748b", fontSize: 11 }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <YAxis
                        type="category"
                        dataKey="product"
                        width={125}
                        tick={{ fill: "#475569", fontSize: 11 }}
                        axisLine={false}
                        tickLine={false}
                      />
                      <Tooltip
                        formatter={(value) => [currency(value), "Revenue"]}
                      />
                      <Bar
                        dataKey="revenue"
                        name="Revenue"
                        fill="#287d8e"
                        radius={[0, 5, 5, 0]}
                        barSize={22}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <EmptyChart message="No product data available." />
                )}
              </ChartCard>

              <ChartCard
                title="Regional performance"
                subtitle="Revenue on the left · Profit on the right"
                className="chart-wide"
              >
                {regionalData.length ? (
                  <>
                    <div className="butterfly-legend">
                      <span><i className="legend-dot revenue-dot" /> Revenue</span>
                      <span><i className="legend-dot profit-dot" /> Profit</span>
                    </div>

                    <ResponsiveContainer
                      width="100%"
                      height={Math.max(
                        280,
                        Math.min(430, regionalData.length * 55)
                      )}
                    >
                      <BarChart
                        data={regionalData}
                        layout="vertical"
                        margin={{ top: 8, right: 20, left: 12, bottom: 8 }}
                      >
                        <CartesianGrid
                          stroke="#e8edf0"
                          strokeDasharray="3 3"
                          horizontal={false}
                        />
                        <XAxis
                          type="number"
                          domain={[-maxRegionValue, maxRegionValue]}
                          tickFormatter={(value) =>
                            compactCurrency(Math.abs(value))
                          }
                          tick={{ fill: "#64748b", fontSize: 11 }}
                          axisLine={false}
                          tickLine={false}
                        />
                        <YAxis
                          type="category"
                          dataKey="region"
                          width={90}
                          tick={{ fill: "#475569", fontSize: 12 }}
                          axisLine={false}
                          tickLine={false}
                        />
                        <ReferenceLine x={0} stroke="#94a3b8" />
                        <Tooltip
                          formatter={(value, name) => [
                            currency(Math.abs(Number(value) || 0)),
                            name === "revenueMirror" ? "Revenue" : "Profit",
                          ]}
                        />
                        <Bar
                          dataKey="revenueMirror"
                          name="Revenue"
                          fill="#287d8e"
                          radius={[5, 0, 0, 5]}
                          barSize={19}
                        />
                        <Bar
                          dataKey="profit"
                          name="Profit"
                          fill="#72a88a"
                          radius={[0, 5, 5, 0]}
                          barSize={19}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </>
                ) : (
                  <EmptyChart message="No regional data available." />
                )}
              </ChartCard>

              <ChartCard
                title="At a glance"
                subtitle="Key figures from your imported records"
              >
                <div className="insights-list">
                  <div className="insight-row">
                    <span>Average revenue per record</span>
                    <strong>{currency(summary.averageRevenuePerRecord)}</strong>
                  </div>
                  <div className="insight-row">
                    <span>Profit margin</span>
                    <strong>
                      {Number(summary.totalRevenue) > 0
                        ? `${(
                            (Number(summary.totalProfit) /
                              Number(summary.totalRevenue)) *
                            100
                          ).toFixed(1)}%`
                        : "—"}
                    </strong>
                  </div>
                  <div className="insight-row">
                    <span>Products tracked</span>
                    <strong>{analytics.productRevenue.length}</strong>
                  </div>
                  <div className="insight-row">
                    <span>Regions tracked</span>
                    <strong>{analytics.regionComparison.length}</strong>
                  </div>
                </div>

                <p className="insights-footnote">
                  Figures are calculated from the sales records stored in
                  your database. They are not growth comparisons.
                </p>
              </ChartCard>
            </section>

            <footer className="sales-footer">
              <span>
                Analytics are based on {number(summary.totalSalesRecords)} stored
                sales records.
              </span>
              <Link to="/upload">Import more sales data →</Link>
            </footer>
          </>
        )}
      </section>
    </main>
  );
}
