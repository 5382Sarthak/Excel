
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

export default function Login() {
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [otp, setOtp] = useState("");
  const [otpMode, setOtpMode] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  async function login() {
    if (!username.trim() || !password) {
      setMessage("Enter your email and password.");
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      const data = new URLSearchParams();
      data.append("username", username);
      data.append("password", password);

      const response = await api("/login", {
        method: "POST",
        body: data,
      });

      const result = (await response.text()).trim();

      if (result === "OTP_SENT") {
        setOtpMode(true);
        setMessage("OTP sent to your email.");
      } else {
        setMessage("Login failed. Check your email and password.");
      }
    } catch {
      setMessage("Cannot connect to the server. Check your backend.");
    } finally {
      setLoading(false);
    }
  }

  async function register() {
    if (!username.trim() || !password) {
      setMessage("Enter an email and password before registering.");
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      const data = new URLSearchParams();
      data.append("username", username);
      data.append("password", password);

      const response = await api("/register", {
        method: "POST",
        body: data,
      });

      const result = (await response.text()).trim();

      if (result === "REGISTERED") {
        setMessage("Registration successful! You can now log in.");
      } else {
        setMessage("User already exists.");
      }
    } catch {
      setMessage("Cannot connect to the server.");
    } finally {
      setLoading(false);
    }
  }

  async function verifyOtp() {
    if (!otp.trim()) {
      setMessage("Enter the OTP sent to your email.");
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      const data = new URLSearchParams();
      data.append("otp", otp.trim());

      const response = await api("/verify-otp", {
        method: "POST",
        body: data,
      });

      const result = (await response.text()).trim();

      if (result === "SUCCESS") {
        navigate("/upload", { replace: true });
      } else {
        setMessage(result || "OTP verification failed.");
      }
    } catch {
      setMessage("Cannot verify OTP. Check your server connection.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="page-shell">
      <section className="panel login-panel">
        <div className="eyebrow">EXCEL ANALYTICS</div>

        <h1>{otpMode ? "Verify your email" : "Welcome back"}</h1>

        <p className="subtitle">
          {otpMode
            ? "Enter the six-digit verification code sent to your email."
            : "Sign in to upload Excel files and analyse API usage."}
        </p>

        {!otpMode ? (
          <form
            onSubmit={(event) => {
              event.preventDefault();
              login();
            }}
          >
            <label htmlFor="username">Email / Username</label>
            <input
              id="username"
              type="text"
              placeholder="Enter your email"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              required
            />

            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />

            <button className="primary-button" disabled={loading}>
              {loading ? "Please wait..." : "Login"}
            </button>

            <button
              type="button"
              className="secondary-button"
              disabled={loading}
              onClick={register}
            >
              Register
            </button>
          </form>
        ) : (
          <form
            onSubmit={(event) => {
              event.preventDefault();
              verifyOtp();
            }}
          >
            <label htmlFor="otp">Verification code</label>
            <input
              id="otp"
              type="text"
              inputMode="numeric"
              autoComplete="one-time-code"
              placeholder="Enter OTP"
              maxLength={6}
              value={otp}
              onChange={(event) =>
                setOtp(event.target.value.replace(/\D/g, "").slice(0, 6))
              }
              required
            />

            <button className="primary-button" disabled={loading}>
              {loading ? "Verifying..." : "Verify OTP"}
            </button>

            <button
              type="button"
              className="secondary-button"
              disabled={loading}
              onClick={() => {
                setOtpMode(false);
                setOtp("");
                setMessage("");
              }}
            >
              Back to login
            </button>
          </form>
        )}

        {message && (
          <p className="status-message" role="status">
            {message}
          </p>
        )}
      </section>
    </main>
  );
}