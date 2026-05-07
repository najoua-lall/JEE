import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthService from "../services/auth.service";

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  :root {
    --bg: #f0f4ff;
    --surface: #ffffff;
    --border: rgba(99,102,241,0.15);
    --primary: #6366f1;
    --accent: #8b5cf6;
    --text: #1e1b4b;
    --text-muted: #6b7280;
    --error: #ef4444;
    --font: 'Sora', sans-serif;
    --mono: 'JetBrains Mono', monospace;
  }
  body { font-family: var(--font); background: var(--bg); color: var(--text); }

  .login-root {
    position: relative; width: 100vw; min-height: 100vh;
    display: flex; align-items: center; justify-content: center;
    background: #eef2ff; overflow: hidden;
  }
  .bg-grid {
    position: fixed; inset: 0;
    background-image: linear-gradient(rgba(99,102,241,0.07) 1px, transparent 1px), linear-gradient(90deg, rgba(99,102,241,0.07) 1px, transparent 1px);
    background-size: 52px 52px; animation: gridShift 28s linear infinite; pointer-events: none;
  }
  @keyframes gridShift { 0%{transform:translate(0,0)} 100%{transform:translate(52px,52px)} }
  .orb { position:fixed;border-radius:50%;filter:blur(90px);pointer-events:none;animation:orbFloat 9s ease-in-out infinite alternate; }
  .orb-1 { width:600px;height:600px;background:radial-gradient(circle,rgba(99,102,241,0.18),transparent 70%);top:-250px;left:-200px;animation-duration:10s; }
  .orb-2 { width:500px;height:500px;background:radial-gradient(circle,rgba(139,92,246,0.14),transparent 70%);bottom:-150px;right:-150px;animation-duration:8s;animation-delay:-4s; }
  .orb-3 { width:350px;height:350px;background:radial-gradient(circle,rgba(14,165,233,0.10),transparent 70%);top:40%;left:55%;animation-duration:12s;animation-delay:-6s; }
  @keyframes orbFloat { 0%{transform:translate(0,0) scale(1)} 100%{transform:translate(35px,25px) scale(1.07)} }

  .card {
    position:relative;z-index:10;width:100%;max-width:780px;margin:32px;
    background:rgba(255,255,255,0.92);border:1.5px solid rgba(99,102,241,0.18);
    border-radius:28px;padding:64px 72px 56px;backdrop-filter:blur(24px);
    box-shadow:0 0 0 1px rgba(99,102,241,0.08),0 32px 80px rgba(99,102,241,0.12),0 8px 24px rgba(0,0,0,0.06),inset 0 1px 0 rgba(255,255,255,0.9);
    animation:cardIn 0.7s cubic-bezier(0.22,1,0.36,1) both;
  }
  @keyframes cardIn { from{opacity:0;transform:translateY(32px) scale(0.96)} to{opacity:1;transform:translateY(0) scale(1)} }

  .logo-row { display:flex;align-items:center;gap:14px;margin-bottom:40px; }
  .logo-icon { width:52px;height:52px;background:linear-gradient(135deg,#6366f1,#8b5cf6);border-radius:16px;display:flex;align-items:center;justify-content:center;font-size:26px;flex-shrink:0;box-shadow:0 6px 20px rgba(99,102,241,0.35); }
  .logo-name { font-family:var(--mono);font-size:18px;font-weight:600;color:#1e1b4b;letter-spacing:0.04em; }
  .logo-tag { font-size:11px;color:var(--primary);letter-spacing:0.14em;text-transform:uppercase;font-family:var(--mono);display:block;opacity:0.75;line-height:1;margin-top:3px; }

  .card-title { font-size:32px;font-weight:700;color:#1e1b4b;margin-bottom:10px;letter-spacing:-0.03em; }
  .card-sub { font-size:16px;color:var(--text-muted);margin-bottom:44px;line-height:1.6; }

  .field { margin-bottom:28px;animation:fieldIn 0.5s cubic-bezier(0.22,1,0.36,1) both; }
  .field:nth-child(1){animation-delay:0.1s;} .field:nth-child(2){animation-delay:0.18s;}
  @keyframes fieldIn { from{opacity:0;transform:translateX(-16px)} to{opacity:1;transform:translateX(0)} }

  label { display:block;font-size:13px;font-weight:600;color:#3730a3;letter-spacing:0.08em;text-transform:uppercase;margin-bottom:10px;font-family:var(--mono); }
  .input-wrap { position:relative; }
  .input-icon { position:absolute;left:18px;top:50%;transform:translateY(-50%);color:#a5b4fc;font-size:16px;pointer-events:none; }
  input {
    width:100%;background:rgba(238,242,255,0.6);border:2px solid rgba(99,102,241,0.18);
    border-radius:14px;padding:18px 18px 18px 52px;color:#1e1b4b;font-family:var(--font);font-size:16px;
    outline:none;transition:border-color 0.2s,box-shadow 0.2s,background 0.2s;
  }
  input::placeholder { color:#9ca3af;opacity:0.8; }
  input:focus { border-color:rgba(99,102,241,0.55);background:rgba(255,255,255,0.95);box-shadow:0 0 0 4px rgba(99,102,241,0.10); }
  .input-focus-line { position:absolute;bottom:-2px;left:10%;right:10%;height:2px;background:linear-gradient(90deg,#6366f1,#8b5cf6);border-radius:2px;transform:scaleX(0);transition:transform 0.3s cubic-bezier(0.22,1,0.36,1); }
  input:focus ~ .input-focus-line { transform:scaleX(1); }
  .eye-btn { position:absolute;right:16px;top:50%;transform:translateY(-50%);background:none;border:none;color:#a5b4fc;cursor:pointer;padding:6px;display:flex;align-items:center;transition:color 0.2s; }
  .eye-btn:hover { color:var(--primary); }

  .forgot { text-align:right;margin-top:8px;margin-bottom:36px; }
  .forgot a { font-size:14px;color:var(--primary);text-decoration:none;font-family:var(--mono);font-weight:500;opacity:0.85;transition:opacity 0.2s; }
  .forgot a:hover { opacity:1;text-decoration:underline; }

  .error-msg { display:flex;align-items:center;gap:10px;background:rgba(239,68,68,0.07);border:1.5px solid rgba(239,68,68,0.25);border-radius:12px;padding:14px 18px;font-size:15px;color:var(--error);margin-bottom:24px;animation:shake 0.4s ease; }
  @keyframes shake { 0%,100%{transform:translateX(0)} 20%,60%{transform:translateX(-5px)} 40%,80%{transform:translateX(5px)} }

  .btn-submit {
    width:100%;padding:18px;background:linear-gradient(135deg,#6366f1,#8b5cf6);border:none;border-radius:14px;color:#ffffff;
    font-family:var(--font);font-size:17px;font-weight:700;cursor:pointer;position:relative;overflow:hidden;
    transition:opacity 0.2s,transform 0.15s,box-shadow 0.2s;box-shadow:0 6px 28px rgba(99,102,241,0.40);letter-spacing:0.04em;
  }
  .btn-submit:hover:not(:disabled) { opacity:0.92;transform:translateY(-2px);box-shadow:0 10px 36px rgba(99,102,241,0.55); }
  .btn-submit:active:not(:disabled) { transform:translateY(0); }
  .btn-submit:disabled { opacity:0.5;cursor:not-allowed; }
  .btn-submit::after { content:'';position:absolute;inset:0;background:linear-gradient(to right,transparent,rgba(255,255,255,0.15),transparent);transform:translateX(-100%);transition:transform 0.5s; }
  .btn-submit:hover::after { transform:translateX(100%); }
  .spinner { display:inline-block;width:17px;height:17px;border:2.5px solid rgba(255,255,255,0.3);border-top-color:#fff;border-radius:50%;animation:spin 0.7s linear infinite;vertical-align:middle;margin-right:10px; }
  @keyframes spin { to{transform:rotate(360deg)} }

  .bottom-link { text-align:center;margin-top:32px;padding-top:28px;border-top:1.5px solid rgba(99,102,241,0.10);font-size:15px;color:var(--text-muted); }
  .bottom-link a { color:var(--primary);text-decoration:none;font-weight:700;transition:opacity 0.2s; }
  .bottom-link a:hover { opacity:0.75;text-decoration:underline; }
`;

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Logique originale (doc 5) : navigate("/") + window.location.reload()
  const handleLogin = async (e) => {
    e.preventDefault();
    setError(""); setLoading(true);
    try {
      await AuthService.login(username, password);
      navigate("/");
      window.location.reload();
    } catch (err) {
      setError("Nom d'utilisateur ou mot de passe incorrect.");
    } finally { setLoading(false); }
  };

  return (
    <>
      <style>{styles}</style>
      <div className="login-root">
        <div className="bg-grid" />
        <div className="orb orb-1" />
        <div className="orb orb-2" />
        <div className="orb orb-3" />

        <div className="card">
          <div className="logo-row">
            <div className="logo-icon"></div>
            <div>
              <span className="logo-name">ENSAM Stages</span>
              <span className="logo-tag">Gestion de stages</span>
            </div>
          </div>

          <h1 className="card-title">Bon retour </h1>
          <p className="card-sub">Connectez-vous à votre espace personnel</p>

          {error && <div className="error-msg"><span>⚠</span>{error}</div>}

          <form onSubmit={handleLogin} noValidate>
            <div className="field">
              <label>Nom d'utilisateur</label>
              <div className="input-wrap">
                <span className="input-icon">@</span>
                <input type="text" required placeholder="ex: mohamed.allami"
                  value={username} onChange={(e) => setUsername(e.target.value)}
                  disabled={loading} />
                <div className="input-focus-line" />
              </div>
            </div>

            <div className="field">
              <label>Mot de passe</label>
              <div className="input-wrap">
                <span className="input-icon"></span>
                <input type={showPw ? "text" : "password"} placeholder="••••••••"
                  value={password} onChange={(e) => setPassword(e.target.value)}
                  disabled={loading} style={{ paddingRight: "52px" }} />
                <button type="button" className="eye-btn" onClick={() => setShowPw(v => !v)}>
                  {showPw ? (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                    </svg>
                  ) : (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  )}
                </button>
                <div className="input-focus-line" />
              </div>
            </div>

            <div className="forgot"><a href="#">Mot de passe oublié ?</a></div>

            <button className="btn-submit" type="submit" disabled={loading}>
              {loading && <span className="spinner" />}
              {loading ? "Connexion en cours…" : "Se connecter"}
            </button>
          </form>

          <div className="bottom-link">
            Pas encore de compte ?{" "}
            <a href="/register">Créer un compte</a>
          </div>
        </div>
      </div>
    </>
  );
};

export default Login;
