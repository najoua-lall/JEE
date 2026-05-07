import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import CandidatureService from "../services/candidature.service";

const STATUT_STYLE = {
  EN_ATTENTE: { bg: "rgba(251,191,36,0.15)", color: "#d97706", label: " ⏳ En attente" },
  ACCEPTEE:   { bg: "rgba(34,197,94,0.15)",  color: "#16a34a", label: " ✅ Acceptée" },
  REFUSEE:    { bg: "rgba(239,68,68,0.15)",  color: "#dc2626", label: " ❌ Refusée" },
};

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  :root { --primary: #6366f1; --font: 'Sora', sans-serif; --mono: 'JetBrains Mono', monospace; }
  body { font-family: var(--font); }

  .page-root { min-height:100vh;background:#eef2ff;padding:2.5rem;position:relative;overflow-x:hidden; }
  .bg-grid { position:fixed;inset:0;background-image:linear-gradient(rgba(99,102,241,0.06) 1px,transparent 1px),linear-gradient(90deg,rgba(99,102,241,0.06) 1px,transparent 1px);background-size:52px 52px;pointer-events:none;z-index:0; }
  .content { position:relative;z-index:1;max-width:1100px;margin:0 auto; }

  .page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:2rem; }
  .page-title { font-size:26px;font-weight:700;color:#1e1b4b; }
  .page-title span { color:var(--primary); }
  .count-badge { display:inline-block;background:rgba(99,102,241,0.12);color:#6366f1;border-radius:20px;padding:3px 12px;font-size:13px;font-weight:600;font-family:var(--mono);margin-left:10px; }

  .btn-back { padding:10px 20px;border-radius:10px;border:1.5px solid rgba(99,102,241,0.25);background:rgba(255,255,255,0.8);color:#3730a3;font-size:14px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-back:hover { background:rgba(99,102,241,0.08); }

  .card { background:rgba(255,255,255,0.92);border:1.5px solid rgba(99,102,241,0.12);border-radius:18px;padding:24px;margin-bottom:16px;box-shadow:0 4px 16px rgba(99,102,241,0.07);transition:transform 0.2s; }
  .card:hover { transform:translateY(-2px); }

  .card-header { display:flex;justify-content:space-between;align-items:flex-start;gap:16px;flex-wrap:wrap;margin-bottom:16px; }
  .etudiant-name { font-size:17px;font-weight:700;color:#1e1b4b; }
  .etudiant-email { font-size:13px;color:#6b7280;margin-top:3px; }
  .etudiant-date { font-size:12px;color:#9ca3af;font-family:var(--mono);margin-top:4px; }
  .statut-badge { padding:6px 14px;border-radius:20px;font-size:13px;font-weight:700;font-family:var(--mono);white-space:nowrap; }

  .info-grid { display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:12px;margin-bottom:16px;background:rgba(238,242,255,0.5);border-radius:12px;padding:14px; }
  .info-item { font-size:13px;color:#374151; }
  .info-label { font-size:11px;font-weight:600;color:#6366f1;text-transform:uppercase;letter-spacing:0.08em;font-family:var(--mono);margin-bottom:3px; }

  .lettre-box { background:rgba(238,242,255,0.4);border:1.5px solid rgba(99,102,241,0.12);border-radius:12px;padding:14px;margin-bottom:16px; }
  .lettre-label { font-size:11px;font-weight:600;color:#6366f1;text-transform:uppercase;letter-spacing:0.08em;font-family:var(--mono);margin-bottom:8px; }
  .lettre-text { font-size:13.5px;color:#374151;line-height:1.7;white-space:pre-wrap; }

  .links-row { display:flex;gap:10px;flex-wrap:wrap;margin-bottom:16px; }
  .link-btn { padding:6px 14px;border-radius:8px;border:1.5px solid rgba(99,102,241,0.2);background:rgba(238,242,255,0.7);color:#6366f1;font-size:12px;font-weight:600;cursor:pointer;font-family:var(--mono);text-decoration:none;transition:background 0.2s; }
  .link-btn:hover { background:rgba(99,102,241,0.12); }

  .actions { display:flex;gap:8px;flex-wrap:wrap;padding-top:12px;border-top:1px solid rgba(99,102,241,0.1); }
  .btn-accepter { padding:9px 18px;border-radius:10px;border:none;background:rgba(34,197,94,0.15);color:#16a34a;font-size:13px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-accepter:hover { background:rgba(34,197,94,0.25); }
  .btn-refuser { padding:9px 18px;border-radius:10px;border:none;background:rgba(239,68,68,0.1);color:#dc2626;font-size:13px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-refuser:hover { background:rgba(239,68,68,0.2); }
  .btn-cv { padding:9px 18px;border-radius:10px;border:1.5px solid rgba(99,102,241,0.25);background:rgba(238,242,255,0.8);color:#6366f1;font-size:13px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-cv:hover { background:rgba(99,102,241,0.1); }

  .empty { text-align:center;padding:4rem;color:#9ca3af;font-size:16px; }
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.5);
    backdrop-filter: blur(6px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }
  .modal {
    background: white;
    border-radius: 24px;
    padding: 28px;
    width: 90%;
    max-width: 550px;
    max-height: 85vh;
    overflow-y: auto;
    box-shadow: 0 20px 40px rgba(0,0,0,0.2);
  }
  .modal h3 {
    font-size: 22px;
    margin-bottom: 20px;
    color: #1e1b4b;
  }
  .modal label {
    display: block;
    margin-top: 15px;
    font-weight: 600;
    font-size: 13px;
    color: #4b5563;
  }
  .modal input, .modal select, .modal textarea {
    width: 100%;
    padding: 10px;
    margin-top: 6px;
    border: 1.5px solid #e2e8f0;
    border-radius: 12px;
    font-family: inherit;
    font-size: 14px;
  }
  .modal-buttons {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 28px;
  }
  .btn-modal-cancel, .btn-modal-submit {
    padding: 10px 20px;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    border: none;
  }
  .btn-modal-cancel {
    background: #e2e8f0;
    color: #1f2937;
  }
  .btn-modal-submit {
    background: #16a34a;
    color: white;
  }
`;

const GererCandidatures = () => {
  const { offreId } = useParams();
  const [candidatures, setCandidatures] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState({});
  const navigate = useNavigate();

  // États pour le modal d'acceptation
  const [showModal, setShowModal] = useState(false);
  const [selectedCandidature, setSelectedCandidature] = useState(null);
  const [form, setForm] = useState({
    societeAccueil: "",
    dateDebut: "",
    dateFin: "",
    mode: "PRESENTIEL",
    remunere: false,
    descriptionSujet: "",
    conditionsComplementaires: ""
  });

  useEffect(() => {
    CandidatureService.getCandidaturesParOffre(offreId)
      .then(r => setCandidatures(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [offreId]);

  const handleStatut = async (id, statut) => {
    try {
      const res = await CandidatureService.changerStatut(id, statut);
      setCandidatures(prev =>
        prev.map(c => c.id === id ? { ...c, statut: res.data.statut } : c)
      );
    } catch {
      alert("Erreur lors du changement de statut.");
    }
  };

  const handleDownloadCv = async (candidatureId, nomEtudiant) => {
    try {
      const response = await CandidatureService.downloadCv(candidatureId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `CV_${nomEtudiant}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      alert("Erreur lors du téléchargement du CV.");
    }
  };

  const openAcceptModal = (candidature) => {
    setSelectedCandidature(candidature);
    setForm({
      societeAccueil: candidature.offreEntreprise || "",
      dateDebut: "",
      dateFin: "",
      mode: "PRESENTIEL",
      remunere: false,
      descriptionSujet: "",
      conditionsComplementaires: ""
    });
    setShowModal(true);
  };

  const handleAcceptSubmit = async () => {
    if (!selectedCandidature) return;
    try {
      await CandidatureService.accepterDetail(selectedCandidature.id, form);
      setCandidatures(prev =>
        prev.map(c => c.id === selectedCandidature.id ? { ...c, statut: "ACCEPTEE" } : c)
      );
      setShowModal(false);
      setSelectedCandidature(null);
    } catch (err) {
      console.error(err);
      alert("Erreur lors de l'acceptation détaillée.");
    }
  };

  const toggleExpand = (id) =>
    setExpanded(prev => ({ ...prev, [id]: !prev[id] }));

  return (
    <>
      <style>{styles}</style>
      <div className="page-root">
        <div className="bg-grid" />
        <div className="content">

          <div className="page-header">
            <h1 className="page-title">
              Candidatures reçues
              <span className="count-badge">{candidatures.length}</span>
            </h1>
            <button className="btn-back" onClick={() => navigate("/offres")}>
              ← Retour
            </button>
          </div>

          {loading && (
            <p style={{ textAlign: "center", padding: "3rem", color: "#6b7280" }}>
              Chargement…
            </p>
          )}

          {!loading && candidatures.length === 0 && (
            <div className="empty">
              <p style={{ fontSize: "3rem", marginBottom: "1rem" }}>📭</p>
              <p>Aucune candidature reçue pour cette offre.</p>
            </div>
          )}

          {candidatures.map(c => {
            const s = STATUT_STYLE[c.statut];
            const open = expanded[c.id];

            return (
              <div key={c.id} className="card">
                <div className="card-header">
                  <div>
                    <p className="etudiant-name">👤 {c.etudiantUsername}</p>
                    <p className="etudiant-email">📧 {c.etudiantEmail}</p>
                    <p className="etudiant-date">
                      📅 Postulé le {new Date(c.datePostulation).toLocaleDateString("fr-FR")}
                    </p>
                  </div>
                  <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                    <span className="statut-badge" style={{ background: s.bg, color: s.color }}>
                      {s.label}
                    </span>
                    <button
                      onClick={() => toggleExpand(c.id)}
                      style={{
                        padding: "6px 14px", borderRadius: "8px",
                        border: "1.5px solid rgba(99,102,241,0.2)",
                        background: "rgba(238,242,255,0.7)",
                        color: "#6366f1", fontSize: "12px",
                        fontWeight: 600, cursor: "pointer",
                        fontFamily: "var(--mono)"
                      }}
                    >
                      {open ? "▲ Réduire" : "▼ Voir détails"}
                    </button>
                  </div>
                </div>

                {open && (
                  <>
                    <div className="info-grid">
                      <div className="info-item">
                        <p className="info-label">Téléphone</p>
                        <p>{c.telephone || "—"}</p>
                      </div>
                      <div className="info-item">
                        <p className="info-label">Disponible le</p>
                        <p>{c.disponibilite || "—"}</p>
                      </div>
                      <div className="info-item">
                        <p className="info-label">Niveau</p>
                        <p>{c.niveauEtudes || "—"}</p>
                      </div>
                      <div className="info-item">
                        <p className="info-label">Établissement</p>
                        <p>{c.etablissement || "—"}</p>
                      </div>
                    </div>

                    {c.lettreMotivation && (
                      <div className="lettre-box">
                        <p className="lettre-label">📄 Lettre de motivation</p>
                        <p className="lettre-text">{c.lettreMotivation}</p>
                      </div>
                    )}

                    {(c.linkedinUrl || c.portfolioUrl) && (
                      <div className="links-row">
                        {c.linkedinUrl && (
                          <a href={c.linkedinUrl} target="_blank" rel="noreferrer" className="link-btn">
                            🔗 LinkedIn
                          </a>
                        )}
                        {c.portfolioUrl && (
                          <a href={c.portfolioUrl} target="_blank" rel="noreferrer" className="link-btn">
                            💼 Portfolio / GitHub
                          </a>
                        )}
                      </div>
                    )}
                  </>
                )}

                <div className="actions">
                  <button className="btn-cv" onClick={() => handleDownloadCv(c.id, c.etudiantUsername)}>
                    📎 Télécharger CV
                  </button>
                  {c.statut !== "ACCEPTEE" && (
                    <button className="btn-accepter" onClick={() => openAcceptModal(c)}>
                      ✅ Accepter
                    </button>
                  )}
                  {c.statut !== "REFUSEE" && (
                    <button className="btn-refuser" onClick={() => handleStatut(c.id, "REFUSEE")}>
                      ❌ Refuser
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Modal d'acceptation */}
        {showModal && (
          <div className="modal-overlay" onClick={() => setShowModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <h3>Accepter la candidature</h3>
              <form>
                <label>Société d'accueil *</label>
                <input
                  type="text"
                  value={form.societeAccueil}
                  onChange={(e) => setForm({ ...form, societeAccueil: e.target.value })}
                  required
                />
                <label>Date de début *</label>
                <input
                  type="date"
                  value={form.dateDebut}
                  onChange={(e) => setForm({ ...form, dateDebut: e.target.value })}
                  required
                />
                <label>Date de fin *</label>
                <input
                  type="date"
                  value={form.dateFin}
                  onChange={(e) => setForm({ ...form, dateFin: e.target.value })}
                  required
                />
                <label>Mode *</label>
                <select
                  value={form.mode}
                  onChange={(e) => setForm({ ...form, mode: e.target.value })}
                >
                  <option value="PRESENTIEL">Présentiel</option>
                  <option value="DISTANCE">Distance</option>
                  <option value="HYBRIDE">Hybride</option>
                </select>
                <label>
                  <input
                    type="checkbox"
                    checked={form.remunere}
                    onChange={(e) => setForm({ ...form, remunere: e.target.checked })}
                  />
                  Rémunéré
                </label>
                <label>Description du sujet de stage *</label>
                <textarea
                  rows={4}
                  value={form.descriptionSujet}
                  onChange={(e) => setForm({ ...form, descriptionSujet: e.target.value })}
                  required
                />
                <label>Conditions complémentaires (optionnel)</label>
                <textarea
                  rows={2}
                  value={form.conditionsComplementaires}
                  onChange={(e) => setForm({ ...form, conditionsComplementaires: e.target.value })}
                />
                <div className="modal-buttons">
                  <button type="button" className="btn-modal-cancel" onClick={() => setShowModal(false)}>
                    Annuler
                  </button>
                  <button type="button" className="btn-modal-submit" onClick={handleAcceptSubmit}>
                    Valider l'acceptation
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default GererCandidatures;