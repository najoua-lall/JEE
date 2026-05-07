import { useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import CandidatureService from "../services/candidature.service";

const NIVEAUX = ["Bac+2", "Bac+3", "Bac+4", "Bac+5", "Doctorat"];

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  :root {
    --bg: #eef2ff; --primary: #6366f1; --accent: #8b5cf6;
    --text: #1e1b4b; --text-muted: #6b7280; --error: #ef4444;
    --font: 'Sora', sans-serif; --mono: 'JetBrains Mono', monospace;
  }

  .form-root { position:relative;width:100vw;min-height:100vh;display:flex;align-items:center;justify-content:center;background:#eef2ff;overflow:hidden;padding:2rem; }
  .bg-grid { position:fixed;inset:0;background-image:linear-gradient(rgba(99,102,241,0.07) 1px,transparent 1px),linear-gradient(90deg,rgba(99,102,241,0.07) 1px,transparent 1px);background-size:52px 52px;pointer-events:none; }
  .orb { position:fixed;border-radius:50%;filter:blur(90px);pointer-events:none; }
  .orb-1 { width:600px;height:600px;background:radial-gradient(circle,rgba(99,102,241,0.18),transparent 70%);top:-250px;left:-200px; }
  .orb-2 { width:500px;height:500px;background:radial-gradient(circle,rgba(139,92,246,0.14),transparent 70%);bottom:-150px;right:-150px; }

  .card { position:relative;z-index:10;width:100%;max-width:800px;background:rgba(255,255,255,0.92);border:1.5px solid rgba(99,102,241,0.18);border-radius:28px;padding:48px 56px;backdrop-filter:blur(24px);box-shadow:0 32px 80px rgba(99,102,241,0.12);animation:cardIn 0.7s cubic-bezier(0.22,1,0.36,1) both; }
  @keyframes cardIn { from{opacity:0;transform:translateY(32px)} to{opacity:1;transform:translateY(0)} }

  .offre-banner { background:linear-gradient(135deg,rgba(99,102,241,0.1),rgba(139,92,246,0.08));border:1.5px solid rgba(99,102,241,0.2);border-radius:16px;padding:16px 20px;margin-bottom:32px; }
  .offre-titre { font-size:18px;font-weight:700;color:#1e1b4b; }
  .offre-entreprise { font-size:14px;color:#6366f1;font-weight:600;margin-top:4px; }

  .card-title { font-size:26px;font-weight:700;color:#1e1b4b;margin-bottom:6px;letter-spacing:-0.02em; }
  .card-sub { font-size:14px;color:var(--text-muted);margin-bottom:28px; }
  .divider { border:none;border-top:1.5px solid rgba(99,102,241,0.10);margin:0 0 24px; }

  .section-title { font-size:12px;font-weight:700;color:#3730a3;letter-spacing:0.1em;text-transform:uppercase;font-family:var(--mono);margin-bottom:16px;margin-top:8px;display:flex;align-items:center;gap:8px; }
  .section-title::after { content:'';flex:1;height:1px;background:rgba(99,102,241,0.12); }

  label { display:block;font-size:12px;font-weight:600;color:#3730a3;letter-spacing:0.08em;text-transform:uppercase;margin-bottom:7px;font-family:var(--mono); }
  .field { margin-bottom:18px; }
  .grid2 { display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:18px; }

  input, textarea, select {
    width:100%;background:rgba(238,242,255,0.6);border:2px solid rgba(99,102,241,0.18);
    border-radius:12px;padding:12px 16px;color:#1e1b4b;font-family:var(--font);font-size:14px;
    outline:none;transition:border-color 0.2s,box-shadow 0.2s,background 0.2s;
  }
  input::placeholder, textarea::placeholder { color:#9ca3af; }
  input:focus, textarea:focus, select:focus { border-color:rgba(99,102,241,0.55);background:#fff;box-shadow:0 0 0 4px rgba(99,102,241,0.08); }
  select { cursor:pointer; }
  textarea { resize:vertical; }

  .cv-upload-zone {
    border:2px dashed rgba(99,102,241,0.35);border-radius:14px;padding:28px;text-align:center;
    background:rgba(238,242,255,0.4);cursor:pointer;transition:all 0.2s;margin-bottom:18px;
    position:relative;
  }
  .cv-upload-zone:hover { border-color:rgba(99,102,241,0.6);background:rgba(238,242,255,0.7); }
  .cv-upload-zone.has-file { border-color:rgba(34,197,94,0.5);background:rgba(34,197,94,0.05); }
  .cv-icon { font-size:36px;margin-bottom:10px; }
  .cv-text { font-size:14px;color:#6b7280;margin-bottom:6px; }
  .cv-hint { font-size:12px;color:#9ca3af;font-family:var(--mono); }
  .cv-name { font-size:14px;font-weight:600;color:#16a34a;margin-top:8px; }
  .cv-input { position:absolute;inset:0;opacity:0;cursor:pointer;width:100%;height:100%; }

  .required { color:#ef4444;margin-left:2px; }

  .btn-actions { display:flex;gap:12px;margin-top:8px; }
  .btn-cancel { flex:1;padding:14px;border-radius:12px;border:2px solid rgba(99,102,241,0.2);background:rgba(238,242,255,0.7);color:#3730a3;font-size:14px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-cancel:hover { background:rgba(99,102,241,0.1); }
  .btn-submit { flex:2;padding:14px;border-radius:12px;border:none;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#fff;font-size:15px;font-weight:700;cursor:pointer;font-family:var(--font);box-shadow:0 6px 24px rgba(99,102,241,0.35);transition:opacity 0.2s,transform 0.15s; }
  .btn-submit:hover:not(:disabled) { opacity:0.9;transform:translateY(-1px); }
  .btn-submit:disabled { opacity:0.5;cursor:not-allowed; }
  .spinner { display:inline-block;width:14px;height:14px;border:2px solid rgba(255,255,255,0.3);border-top-color:#fff;border-radius:50%;animation:spin 0.7s linear infinite;vertical-align:middle;margin-right:8px; }
  @keyframes spin { to{transform:rotate(360deg)} }

  .error-msg { display:flex;align-items:center;gap:10px;background:rgba(239,68,68,0.07);border:1.5px solid rgba(239,68,68,0.25);border-radius:12px;padding:14px 18px;font-size:14px;color:var(--error);margin-top:16px; }
  .success-msg { display:flex;align-items:center;gap:10px;background:rgba(34,197,94,0.08);border:1.5px solid rgba(34,197,94,0.3);border-radius:12px;padding:14px 18px;font-size:14px;color:#16a34a;margin-top:16px; }
`;

const PostulerForm = () => {
  const { offreId }   = useParams();
  const location      = useLocation();
  const navigate      = useNavigate();
  const offre         = location.state?.offre;

  const [form, setForm] = useState({
    lettreMotivation: "",
    disponibilite:    "",
    telephone:        "",
    niveauEtudes:     "",
    etablissement:    "",
    linkedinUrl:      "",
    portfolioUrl:     "",
  });
  const [cvFile, setCvFile]       = useState(null);
  const [submitting, setSub]      = useState(false);
  const [error, setError]         = useState("");
  const [success, setSuccess]     = useState(false);

  const handleChange = (e) =>
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleCvChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.type !== "application/pdf") {
        setError("Seuls les fichiers PDF sont acceptés.");
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        setError("Le fichier ne doit pas dépasser 10 MB.");
        return;
      }
      setCvFile(file);
      setError("");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    // Validations
    if (!cvFile) { setError("Le CV est obligatoire."); return; }
    if (!form.lettreMotivation.trim()) { setError("La lettre de motivation est obligatoire."); return; }
    if (!form.telephone.trim()) { setError("Le téléphone est obligatoire."); return; }
    if (!form.niveauEtudes) { setError("Le niveau d'études est obligatoire."); return; }
    if (!form.etablissement.trim()) { setError("L'établissement est obligatoire."); return; }
    if (!form.disponibilite) { setError("La date de disponibilité est obligatoire."); return; }

    setSub(true);
    try {
      const formData = new FormData();
      formData.append("cv", cvFile);
      formData.append("lettreMotivation", form.lettreMotivation);
      formData.append("disponibilite", form.disponibilite);
      formData.append("telephone", form.telephone);
      formData.append("niveauEtudes", form.niveauEtudes);
      formData.append("etablissement", form.etablissement);
      if (form.linkedinUrl) formData.append("linkedinUrl", form.linkedinUrl);
      if (form.portfolioUrl) formData.append("portfolioUrl", form.portfolioUrl);

      await CandidatureService.postuler(offreId, formData);
      setSuccess(true);

      setTimeout(() => navigate("/mes-candidatures"), 2000);
    } catch (err) {
            
        if (err.response?.status === 409) {
          setError("Vous avez déjà postulé à cette offre.");
        } else if (err.response?.status === 403) {
          setError("Accès refusé. Vérifiez que vous êtes connecté en tant qu'étudiant.");
        } else {
          setError(err.response?.data?.message || "Erreur lors de la candidature.");
        }
      
    } finally {
      setSub(false);
    }
  };

  return (
    <>
      <style>{styles}</style>
      <div className="form-root">
        <div className="bg-grid" />
        <div className="orb orb-1" />
        <div className="orb orb-2" />

        <div className="card">

          {/* Info offre */}
          {offre && (
            <div className="offre-banner">
              <p className="offre-titre"> {offre.titre}</p>
              <p className="offre-entreprise"> {offre.entreprise} ·  {offre.localisation}</p>
            </div>
          )}

          <h1 className="card-title">Postuler à cette offre 🚀</h1>
          <p className="card-sub">Remplissez le formulaire ci-dessous pour soumettre votre candidature.</p>
          <hr className="divider" />

          <form onSubmit={handleSubmit} noValidate>

            {/* ── CV ── */}
            <p className="section-title"> Curriculum Vitae</p>
            <div
              className={`cv-upload-zone ${cvFile ? "has-file" : ""}`}
              onClick={() => document.getElementById("cv-input").click()}
            >
              <input
                id="cv-input"
                type="file"
                accept=".pdf"
                onChange={handleCvChange}
                style={{ display: "none" }}
              />
              {cvFile ? (
                <>
                  <div className="cv-icon"></div>
                  <p className="cv-name"> {cvFile.name}</p>
                  <p className="cv-hint">
                    {(cvFile.size / 1024 / 1024).toFixed(2)} MB · Cliquez pour changer
                  </p>
                </>
              ) : (
                <>
                  <div className="cv-icon">📎</div>
                  <p className="cv-text">Glissez votre CV ici ou cliquez pour choisir</p>
                  <p className="cv-hint">PDF uniquement · Max 10 MB</p>
                </>
              )}
            </div>

            {/* ── Infos personnelles ── */}
            <p className="section-title"> Informations personnelles</p>
            <div className="grid2">
              <div>
                <label>Téléphone <span className="required">*</span></label>
                <input
                  name="telephone" type="tel"
                  placeholder="ex: +212 6XX XXX XXX"
                  value={form.telephone}
                  onChange={handleChange}
                />
              </div>
              <div>
                <label>Disponibilité <span className="required">*</span></label>
                <input
                  type="date" name="disponibilite"
                  value={form.disponibilite}
                  onChange={handleChange}
                />
              </div>
            </div>

            {/* ── Parcours ── */}
            <p className="section-title"> Parcours académique</p>
            <div className="grid2">
              <div>
                <label>Niveau d'études <span className="required">*</span></label>
                <select name="niveauEtudes" value={form.niveauEtudes} onChange={handleChange}>
                  <option value="">-- Choisir --</option>
                  {NIVEAUX.map(n => <option key={n} value={n}>{n}</option>)}
                </select>
              </div>
              <div>
                <label>Établissement <span className="required">*</span></label>
                <input
                  name="etablissement"
                  placeholder="ex: ENSAM Meknès"
                  value={form.etablissement}
                  onChange={handleChange}
                />
              </div>
            </div>

            {/* ── Lettre de motivation ── */}
            <p className="section-title"> Lettre de motivation</p>
            <div className="field">
              <label>Lettre de motivation <span className="required">*</span></label>
              <textarea
                name="lettreMotivation" rows={7}
                placeholder="Expliquez pourquoi vous êtes le candidat idéal pour ce stage. Parlez de vos motivations, compétences et ce que vous apporterez à l'entreprise..."
                value={form.lettreMotivation}
                onChange={handleChange}
              />
              <p style={{ fontSize: "12px", color: "#9ca3af", marginTop: "4px", fontFamily: "var(--mono)" }}>
                {form.lettreMotivation.length} caractères
              </p>
            </div>

            {/* ── Liens optionnels ── */}
            <p className="section-title"> Liens (optionnels)</p>
            <div className="grid2">
              <div>
                <label>LinkedIn</label>
                <input
                  name="linkedinUrl" type="url"
                  placeholder="https://linkedin.com/in/..."
                  value={form.linkedinUrl}
                  onChange={handleChange}
                />
              </div>
              <div>
                <label>Portfolio / GitHub</label>
                <input
                  name="portfolioUrl" type="url"
                  placeholder="https://github.com/..."
                  value={form.portfolioUrl}
                  onChange={handleChange}
                />
              </div>
            </div>

            {/* ── Boutons ── */}
            <div className="btn-actions">
              <button type="button" className="btn-cancel"
                onClick={() => navigate(-1)}>
                Annuler
              </button>
              <button type="submit" className="btn-submit" disabled={submitting}>
                {submitting && <span className="spinner" />}
                {submitting ? "Envoi en cours…" : "Envoyer ma candidature "}
              </button>
            </div>

          </form>

          {error && <div className="error-msg">⚠ {error}</div>}
          {success && (
            <div className="success-msg">
               Candidature envoyée ! Redirection vers mes candidatures…
            </div>
          )}

        </div>
      </div>
    </>
  );
};

export default PostulerForm;