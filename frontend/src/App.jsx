import { useEffect, useMemo, useState } from "react";
import { authApi, stockApi } from "./api.js";

export default function App() {
  const [user, setUser] = useState(null);
  const [items, setItems] = useState([]);
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ username: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    authApi.currentUser()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!user) return;
    stockApi.list()
      .then(setItems)
      .catch((requestError) => setError(requestError.message));
  }, [user]);

  const totalUnits = useMemo(
    () => items.reduce((total, item) => total + item.quantity, 0),
    [items],
  );

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const signedInUser = mode === "register"
        ? await authApi.register(form)
        : await authApi.login({ email: form.email, password: form.password });
      setUser(signedInUser);
      setForm({ username: "", email: "", password: "" });
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function logout() {
    setError("");
    try {
      await authApi.logout();
      setUser(null);
      setItems([]);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  if (loading) {
    return <main className="loading">Loading Stockroom...</main>;
  }

  if (!user) {
    const registering = mode === "register";
    return (
      <main className="auth-layout">
        <section className="welcome-panel">
          <div className="brand"><span className="brand-icon">S</span> Stockroom</div>
          <div className="welcome-copy">
            <p className="eyebrow">RWANDA POLYTECHNIC · DEVOPS DEMO</p>
            <h1>Know what you have.<br /><span>Plan what’s next.</span></h1>
            <p>A simple stock dashboard built to demonstrate a complete GitHub Actions and Jira workflow.</p>
          </div>
          <div className="welcome-foot">Stock Management System <span>·</span> CI-ready</div>
        </section>

        <section className="form-panel">
          <div className="auth-card">
            <div className="mobile-brand"><span className="brand-icon">S</span> Stockroom</div>
            <p className="eyebrow">{registering ? "CREATE YOUR ACCOUNT" : "WELCOME BACK"}</p>
            <h2>{registering ? "Get started" : "Sign in"}</h2>
            <p className="form-subtitle">
              {registering ? "Create an account to view your stock dashboard." : "Enter your details to continue to your dashboard."}
            </p>

            <form onSubmit={submit}>
              {registering && (
                <label>
                  Username
                  <input name="username" value={form.username} onChange={updateField} placeholder="e.g. Alex" maxLength="40" required />
                </label>
              )}
              <label>
                Email address
                <input name="email" type="email" value={form.email} onChange={updateField} placeholder="you@example.com" autoComplete="email" required />
              </label>
              <label>
                Password
                <input name="password" type="password" value={form.password} onChange={updateField} placeholder="At least 8 characters" minLength="8" maxLength="72" autoComplete={registering ? "new-password" : "current-password"} required />
              </label>
              {error && <p className="error-message" role="alert">{error}</p>}
              <button className="primary-button" type="submit" disabled={submitting}>
                {submitting ? "Please wait..." : registering ? "Create account" : "Sign in"}
                {!submitting && <span aria-hidden="true">→</span>}
              </button>
            </form>

            <p className="switch-mode">
              {registering ? "Already have an account?" : "New to Stockroom?"}{" "}
              <button type="button" onClick={() => { setError(""); setMode(registering ? "login" : "register"); }}>
                {registering ? "Sign in" : "Create account"}
              </button>
            </p>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="dashboard">
      <header className="topbar">
        <a className="brand" href="#" aria-label="Stockroom home"><span className="brand-icon">S</span> Stockroom</a>
        <div className="topbar-right">
          <span className="user-avatar">{user.username.slice(0, 1).toUpperCase()}</span>
          <span className="user-name">{user.username}</span>
          <button className="logout-button" onClick={logout}>Log out</button>
        </div>
      </header>

      <section className="dashboard-content">
        <div className="page-heading">
          <div>
            <p className="eyebrow">OVERVIEW</p>
            <h1>Your stock at a glance</h1>
            <p className="form-subtitle">Welcome back, {user.username}. Here’s your current inventory snapshot.</p>
          </div>
          <span className="status-pill"><span /> All systems operational</span>
        </div>

        {error && <p className="error-message dashboard-error" role="alert">{error}</p>}

        <section className="summary-grid" aria-label="Stock summary">
          <article className="summary-card">
            <div className="summary-icon lavender">▤</div>
            <p className="summary-label">Total products</p>
            <p className="summary-value">{items.length.toString().padStart(2, "0")}</p>
            <p className="summary-note">Across your inventory</p>
          </article>
          <article className="summary-card">
            <div className="summary-icon mint">◫</div>
            <p className="summary-label">Units in stock</p>
            <p className="summary-value">{totalUnits}</p>
            <p className="summary-note">Available units</p>
          </article>
          <article className="summary-card">
            <div className="summary-icon peach">⌂</div>
            <p className="summary-label">Categories</p>
            <p className="summary-value">{new Set(items.map((item) => item.category)).size.toString().padStart(2, "0")}</p>
            <p className="summary-note">Product groups</p>
          </article>
        </section>

        <section className="inventory-card">
          <div className="inventory-heading">
            <div><p className="eyebrow">INVENTORY</p><h2>Stock overview</h2></div>
            <span className="item-count">{items.length} items</span>
          </div>
          <div className="table-scroll">
            <table>
              <thead><tr><th>Product</th><th>Category</th><th>Quantity</th><th>Status</th></tr></thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id}>
                    <td><span className="product-mark">{item.name.slice(0, 1)}</span>{item.name}</td>
                    <td><span className="category-tag">{item.category}</span></td>
                    <td className="quantity">{item.quantity} units</td>
                    <td><span className="stock-status"><span /> In stock</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="demo-note">Sample inventory for the assignment demo. This data resets when the API restarts.</p>
        </section>
        <footer className="dashboard-footer">Stockroom <span>·</span> Stock Management System demo</footer>
      </section>
    </main>
  );
}
