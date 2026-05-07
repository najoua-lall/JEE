import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import OffreService from "../services/offre.service";
import MultiSelectCompetences from "../components/MultiSelectCompetences";

const SECTEURS = ["IT", "Finance", "Marketing", "RH", "Industrie", "Santé", "Autre"];

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  :root {
    --bg: #eef2ff; --surface: #ffffff; --primary: #6366f1; --accent: #8b5cf6;
    --text: #1e1b4b; --text-muted: #6b7280; --error: #ef4444;
    --font: 'Sora', sans-serif; --mono: 'JetBrains Mono', monospace;
  }
  body { font-family: var(--font); background: var(--bg); color: var(--text); }

  .form-root {
    position: relative; width: 100vw; min-height: 100vh;
    display: flex; align-items: center; justify-content: center;
    background: #eef2ff; overflow: hidden; padding: 2rem;
  }
  .bg-grid {
    position: fixed; inset: 0;
    background-image: linear-gradient(rgba(99,102,241,0.07) 1px, transparent 1px),
      linear-gradient(90deg, rgba(99,102,241,0.07) 1px, transparent 1px);
    background-size: 52px 52px; animation: gridShift 28s linear infinite; pointer-events: none;
  }
  @keyframes gridShift { 0%{transform:translate(0,0)} 100%{transform:translate(52px,52px)} }
  .orb { position:fixed;border-radius:50%;filter:blur(90px);pointer-events:none;animation:orbFloat 9s ease-in-out infinite alternate; }
  .orb-1 { width:600px;height:600px;background:radial-gradient(circle,rgba(99,102,241,0.18),transparent 70%);top:-250px;left:-200px; }
  .orb-2 { width:500px;height:500px;background:radial-gradient(circle,rgba(139,92,246,0.14),transparent 70%);bottom:-150px;right:-150px;animation-delay:-4s; }
  @keyframes orbFloat { 0%{transform:translate(0,0) scale(1)} 100%{transform:translate(35px,25px) scale(1.07)} }

  .card {
    position: relative; z-index: 10; width: 100%; max-width: 900px;
    background: rgba(255,255,255,0.92); border: 1.5px solid rgba(99,102,241,0.18);
    border-radius: 28px; padding: 56px 64px 48px; backdrop-filter: blur(24px);
    box-shadow: 0 0 0 1px rgba(99,102,241,0.08), 0 32px 80px rgba(99,102,241,0.12),
      0 8px 24px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.9);
    animation: cardIn 0.7s cubic-bezier(0.22,1,0.36,1) both;
  }
  @keyframes cardIn { from{opacity:0;transform:translateY(32px) scale(0.96)} to{opacity:1;transform:translateY(0) scale(1)} }

  .logo-row { display:flex;align-items:center;gap:14px;margin-bottom:32px; }
  .logo-icon { width:48px;height:48px;background:linear-gradient(135deg,#6366f1,#8b5cf6);border-radius:14px;display:flex;align-items:center;justify-content:center;font-size:22px;box-shadow:0 6px 20px rgba(99,102,241,0.35); }
  .logo-name { font-family:var(--mono);font-size:17px;font-weight:600;color:#1e1b4b;letter-spacing:0.04em; }
  .logo-tag { font-size:10px;color:var(--primary);letter-spacing:0.14em;text-transform:uppercase;font-family:var(--mono);display:block;opacity:0.75;margin-top:2px; }

  .card-title { font-size:28px;font-weight:700;color:#1e1b4b;margin-bottom:6px;letter-spacing:-0.03em; }
  .card-sub { font-size:14px;color:var(--text-muted);margin-bottom:32px;line-height:1.6; }
  .divider { border:none;border-top:1.5px solid rgba(99,102,241,0.10);margin:0 0 28px; }

  label { display:block;font-size:12px;font-weight:600;color:#3730a3;letter-spacing:0.09em;text-transform:uppercase;margin-bottom:8px;font-family:var(--mono); }
  .field { margin-bottom: 20px; }
  .grid2 { display:grid;grid-template-columns:1fr 1fr;gap:18px;margin-bottom:20px; }
  .grid3 { display:grid;grid-template-columns:1fr 1fr 1fr;gap:18px;margin-bottom:20px; }

  input, textarea, select {
    width:100%;background:rgba(238,242,255,0.6);
    border:2px solid rgba(99,102,241,0.18);border-radius:12px;
    padding:13px 16px;color:#1e1b4b;font-family:var(--font);font-size:15px;
    outline:none;transition:border-color 0.2s,box-shadow 0.2s,background 0.2s;
  }
  input::placeholder, textarea::placeholder { color:#9ca3af;opacity:0.8; }
  input:focus, textarea:focus, select:focus {
    border-color:rgba(99,102,241,0.55);background:rgba(255,255,255,0.95);
    box-shadow:0 0 0 4px rgba(99,102,241,0.10);
  }
  select { cursor:pointer; }
  textarea { resize:vertical; }
  input[type="date"] { cursor:pointer; }

  .section-label {
    font-size:13px;font-weight:700;color:#3730a3;letter-spacing:0.06em;
    text-transform:uppercase;font-family:var(--mono);
    margin-bottom:12px;margin-top:4px;
    display:flex;align-items:center;gap:8px;
  }
  .section-label::after { content:'';flex:1;height:1px;background:rgba(99,102,241,0.12); }

  .competences-wrap {
    background:rgba(238,242,255,0.4);border:2px solid rgba(99,102,241,0.15);
    border-radius:12px;padding:16px;margin-bottom:20px;
  }

  .btn-actions { display:flex;gap:14px;margin-top:8px; }
  .btn-cancel {
    flex:1;padding:14px;border-radius:12px;
    border:2px solid rgba(99,102,241,0.2);background:rgba(238,242,255,0.7);
    color:#3730a3;font-size:15px;font-weight:600;cursor:pointer;
    font-family:var(--font);transition:background 0.2s;
  }
  .btn-cancel:hover { background:rgba(99,102,241,0.10); }
  .btn-submit {
    flex:2;padding:14px;border-radius:12px;border:none;
    background:linear-gradient(135deg,#6366f1,#8b5cf6);
    color:#fff;font-size:15px;font-weight:700;cursor:pointer;
    font-family:var(--font);
    box-shadow:0 6px 24px rgba(99,102,241,0.38);
    transition:opacity 0.2s,transform 0.15s;
  }
  .btn-submit:hover:not(:disabled) { opacity:0.92;transform:translateY(-1px); }
  .btn-submit:disabled { opacity:0.5;cursor:not-allowed; }
  .spinner {
    display:inline-block;width:14px;height:14px;
    border:2px solid rgba(255,255,255,0.3);border-top-color:#fff;
    border-radius:50%;animation:spin 0.7s linear infinite;
    vertical-align:middle;margin-right:8px;
  }
  @keyframes spin { to{transform:rotate(360deg)} }
  .error-msg {
    display:flex;align-items:center;gap:10px;
    background:rgba(239,68,68,0.07);border:1.5px solid rgba(239,68,68,0.25);
    border-radius:12px;padding:14px 18px;font-size:14px;
    color:var(--error);margin-top:16px;
  }
`;

const emptyForm = {
  titre:        "",
  entreprise:   "",
  localisation: "",
  secteur:      "",
  description:  "",
  dateDebut:    "",
  dateFin:      "",
};

const OffreForm = () => {
  const { id }      = useParams();
  const isEdit      = Boolean(id);
  const navigate    = useNavigate();

  const [form, setForm]             = useState(emptyForm);
  const [competences, setCompetences] = useState([]);
  const [loading, setLoading]       = useState(isEdit);
  const [submitting, setSub]        = useState(false);
  const [error, setError]           = useState("");

  useEffect(() => {
    if (isEdit) {
      OffreService.getOffre(id)
        .then(({ data }) => {
          setForm({
            titre:        data.titre        || "",
            entreprise:   data.entreprise   || "",
            localisation: data.localisation || "",
            secteur:      data.secteur      || "",
            description:  data.description  || "",
            dateDebut:    data.dateDebut    || "",
            dateFin:      data.dateFin      || "",
          });
          if (data.competences) {
            setCompetences(data.competences);
          }
        })
        .catch(() => setError("Impossible de charger l'offre."))
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleChange = (e) =>
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSub(true);
    setError("");
    try {
      const payload = {
        ...form,
        competenceIds: competences.map(c => c.id),
      };

      if (isEdit) {
        await OffreService.updateOffre(id, payload);
      } else {
        await OffreService.createOffre(payload);
      }
      navigate("/offres");
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Une erreur est survenue. Vérifiez vos données."
      );
    } finally {
      setSub(false);
    }
  };

  if (loading) return (
    <div style={{
      padding: "3rem", textAlign: "center",
      color: "#6b7280", fontFamily: "'Sora',sans-serif"
    }}>
      Chargement…
    </div>
  );

  return (
    <>
      <style>{styles}</style>
      <div className="form-root">
        <div className="bg-grid" />
        <div className="orb orb-1" />
        <div className="orb orb-2" />

        <div className="card">

          {/* Logo */}
          <div className="logo-row">
            <div className="logo-icon"></div>
            <div>
              <span className="logo-name">ENSAM Stages</span>
              <span className="logo-tag">Gestion de stages</span>
            </div>
          </div>

          <h1 className="card-title">
            {isEdit ? "Modifier l'offre " : "Publier une offre "}
          </h1>
          <p className="card-sub">
            {isEdit
              ? "Mettez à jour les informations de votre offre de stage."
              : "Remplissez les informations de votre offre de stage."}
          </p>
          <hr className="divider" />

          <form onSubmit={handleSubmit} noValidate>

            {/* Ligne 1 : titre + entreprise */}
            <div className="grid2">
              <div>
                <label>Titre du poste</label>
                <input
                  name="titre" required
                  value={form.titre}
                  onChange={handleChange}
                  placeholder="ex: Développeur Full-Stack"
                />
              </div>
              <div>
                <label>Entreprise</label>
                <input
                  name="entreprise" required
                  value={form.entreprise}
                  onChange={handleChange}
                  placeholder="ex: Accenture Maroc"
                />
              </div>
            </div>

            {/* Ligne 2 : localisation + secteur */}
            <div className="grid2">
              <div>
                <label>Localisation</label>
                <input
                  name="localisation" required
                  value={form.localisation}
                  onChange={handleChange}
                  placeholder="ex: Casablanca"
                />
              </div>
              <div>
                <label>Secteur</label>
                <select
                  name="secteur"
                  value={form.secteur}
                  onChange={handleChange}
                >
                  <option value="">-- Choisir un secteur --</option>
                  {SECTEURS.map(s => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>

            {/* Ligne 3 : dates */}
            <div className="grid2">
              <div>
                <label>Date de début</label>
                <input
                  type="date" name="dateDebut" required
                  value={form.dateDebut}
                  onChange={handleChange}
                />
              </div>
              <div>
                <label>Date de fin</label>
                <input
                  type="date" name="dateFin" required
                  value={form.dateFin}
                  onChange={handleChange}
                />
              </div>
            </div>

            {/* Description */}
            <div className="field">
              <label>Description</label>
              <textarea
                name="description" required rows={5}
                value={form.description}
                onChange={handleChange}
                placeholder="Décrivez les missions, technologies, profil recherché…"
              />
            </div>

            {/* Compétences */}
            <p className="section-label"> Compétences requises</p>
            <div className="competences-wrap">
              <MultiSelectCompetences
                selected={competences}
                onChange={setCompetences}
              />
            </div>

            {/* Boutons */}
            <div className="btn-actions">
              <button
                type="button" className="btn-cancel"
                onClick={() => navigate("/offres")}
              >
                Annuler
              </button>
              <button
                type="submit" className="btn-submit"
                disabled={submitting}
              >
                {submitting && <span className="spinner" />}
                {submitting
                  ? "Enregistrement…"
                  : isEdit ? "Mettre à jour →" : "Publier l'offre "}
              </button>
            </div>

          </form>

          {error && (
            <div className="error-msg"><span>⚠</span>{error}</div>
          )}

        </div>
      </div>
    </>
  );
};

export default OffreForm;