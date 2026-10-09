
import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api";

export default function Upload() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [checkingSession, setCheckingSession] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let active = true;

    async function checkSession() {
      try {
        const response = await api("/check-session");
        const loggedIn = response.ok && await response.json();

        if (active && !loggedIn) {
          navigate("/login", { replace: true });
        }
      } catch {
        if (active) {
          setMessage("Unable to verify your session. Check the backend.");
        }
      } finally {
        if (active) setCheckingSession(false);
      }
    }

    checkSession();

    return () => {
      active = false;
    };
  }, [navigate]);

  async function upload() {
    if (!file) {
      setMessage("Please select an Excel file first.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    setLoading(true);
    setMessage("");

    try {
      const response = await api("/upload", {
        method: "POST",
        body: formData,
      });

      if (response.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      const result = await response.text();
      setMessage(result);

      if (response.ok) {
        setFile(null);
        const input = document.getElementById("fileInput");
        if (input) input.value = "";
      }
    } catch {
      setMessage("Upload failed. Check your backend connection.");
    } finally {
      setLoading(false);
    }
  }

  async function logout() {
    try {
      await api("/logout");
    } finally {
      navigate("/login", { replace: true });
    }
  }

  if (checkingSession) {
    return <main className="page-shell"><p>Checking session...</p></main>;
  }

  return (
    <main className="page-shell">
      <section className="panel upload-panel">
        <header className="topbar">
          <div>
            <div className="eyebrow">EXCEL ANALYTICS</div>
            <h1>Upload Excel File</h1>
          </div>

          <button className="secondary-button logout-button" onClick={logout}>
            Logout
          </button>
        </header>

        <p className="subtitle">
          Upload your API usage spreadsheet to analyse calls, successes,
          and failures.
        </p>

        <div className="upload-area">
          <div className="upload-symbol">↑</div>
          <h2>Select your Excel file</h2>
          <p className="subtitle">Choose an .xlsx or .xls spreadsheet.</p>

          <input
            id="fileInput"
            type="file"
            accept=".xlsx,.xls"
            onChange={(event) => setFile(event.target.files?.[0] || null)}
          />

          {file && (
            <p className="file-name">Selected: {file.name}</p>
          )}

          <button
            className="primary-button upload-button"
            onClick={upload}
            disabled={loading}
          >
            {loading ? "Uploading..." : "Upload file"}
          </button>
        </div>

        {message && (
          <p className="status-message" role="status">
            {message}
          </p>
        )}

        <div className="bottom-link">
          <Link to="/dashboard">Go to Dashboard →</Link>
        </div>
      </section>
    </main>
  );
}