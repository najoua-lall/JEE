import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import CandidatureService from "../services/candidature.service";
// AuthService n'est pas utilisé ici, on peut le retirer si souhaité
// import AuthService from "../services/auth.service";

const STATUT_STYLE = {
  EN_ATTENTE: {
    bg: "rgba(251,191,36,0.15)",
    color: "#d97706",
    label: " En attente",
    message: "Votre candidature est en cours d’examen par le recruteur."
  },
  ACCEPTEE: {
    bg: "rgba(34,197,94,0.15)",
    color: "#16a34a",
    label: " Acceptée",
    message: "Félicitations ! Vous pouvez télécharger votre invitation ci-dessous."
  },
  REFUSEE: {
    bg: "rgba(239,68,68,0.15)",
    color: "#dc2626",
    label: " Refusée",
    message: "Nous sommes désolés, votre candidature n’a pas été retenue."
  },
};

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  :root { --primary: #6366f1; --font: 'Sora', sans-serif; --mono: 'JetBrains Mono', monospace; }
  body { font-family: var(--font); }

  .page-root { min-height: 100vh; background: #eef2ff; padding: 2.5rem; position: relative; overflow-x: hidden; }
  .bg-grid { position: fixed; inset: 0; background-image: linear-gradient(rgba(99,102,241,0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(99,102,241,0.06) 1px, transparent 1px); background-size: 52px 52px; pointer-events: none; z-index: 0; }
  .orb { position:fixed;border-radius:50%;filter:blur(90px);pointer-events:none;z-index:0; }
  .orb-1 { width:500px;height:500px;background:radial-gradient(circle,rgba(99,102,241,0.15),transparent 70%);top:-200px;left:-150px; }
  .content { position: relative; z-index: 1; max-width: 1000px; margin: 0 auto; }

  .page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:2rem; }
  .page-title { font-size:28px;font-weight:700;color:#1e1b4b; }
  .page-title span { color: var(--primary); }

  .btn-back { padding:10px 20px;border-radius:10px;border:1.5px solid rgba(99,102,241,0.25);background:rgba(255,255,255,0.8);color:#3730a3;font-size:14px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-back:hover { background:rgba(99,102,241,0.08); }

  .card { background:rgba(255,255,255,0.92);border:1.5px solid rgba(99,102,241,0.12);border-radius:18px;padding:24px;margin-bottom:16px;box-shadow:0 4px 16px rgba(99,102,241,0.07);display:flex;flex-direction:column;gap:16px;transition:transform 0.2s; }
  .card:hover { transform:translateY(-2px); }

  .card-left { flex:1; }
  .card-titre { font-size:17px;font-weight:700;color:#1e1b4b;margin-bottom:4px; }
  .card-entreprise { font-size:13px;color:#6366f1;font-weight:600;margin-bottom:6px; }
  .card-date { font-size:12px;color:#9ca3af;font-family:var(--mono); }

  .statut-badge { padding:6px 14px;border-radius:20px;font-size:13px;font-weight:700;font-family:var(--mono); }
  .statut-message { font-size:13px; color:#4b5563; margin-top:8px; font-style:italic; }

  .btn-annuler { padding:8px 16px;border-radius:10px;border:none;background:rgba(239,68,68,0.1);color:#dc2626;font-size:13px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-annuler:hover { background:rgba(239,68,68,0.2); }

  .btn-invitation { padding:8px 16px;border-radius:10px;border:none;background:rgba(99,102,241,0.1);color:#6366f1;font-size:13px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.2s; }
  .btn-invitation:hover { background:rgba(99,102,241,0.2); }

  .empty { text-align:center;padding:4rem;color:#9ca3af;font-size:16px; }
  .loading { text-align:center;padding:3rem;color:#6b7280; }
`;

const MesCandidatures = () => {
  const [candidatures, setCandidatures] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    CandidatureService.getMesCandidatures()
      .then(r => setCandidatures(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleAnnuler = async (id) => {
    if (!window.confirm("Annuler cette candidature ?")) return;
    try {
      await CandidatureService.annuler(id);
      setCandidatures(prev => prev.filter(c => c.id !== id));
    } catch {
      alert("Erreur lors de l'annulation.");
    }
  };

  return (
    <>
      <style>{styles}</style>
      <div className="page-root">
        <div className="bg-grid" />
        <div className="orb orb-1" />

        <div className="content">
          <div className="page-header">
            <h1 className="page-title">
              Mes <span>candidatures</span>
            </h1>
            <button className="btn-back" onClick={() => navigate("/offres")}>
              ← Retour aux offres
            </button>
          </div>

          {loading && <p className="loading">Chargement…</p>}

          {!loading && candidatures.length === 0 && (
            <div className="empty">
              <p style={{ fontSize: "3rem", marginBottom: "1rem" }}>📭</p>
              <p>Vous n'avez pas encore postulé à une offre.</p>
              <button
                onClick={() => navigate("/offres")}
                style={{
                  marginTop: "1rem", padding: "10px 24px",
                  background: "linear-gradient(135deg,#6366f1,#8b5cf6)",
                  color: "#fff", border: "none", borderRadius: "10px",
                  cursor: "pointer", fontWeight: 600, fontSize: "14px"
                }}
              >
                Voir les offres
              </button>
            </div>
          )}

          {candidatures.map(c => {
            const s = STATUT_STYLE[c.statut];
            if (!s) return null; // sécurité
            return (
              <div key={c.id} className="card">
                <div className="card-left">
                  <p className="card-titre">{c.offreTitre}</p>
                  <p className="card-entreprise"> {c.offreEntreprise}</p>
                  <p className="card-date">
                     Postulé le {new Date(c.datePostulation).toLocaleDateString("fr-FR")}
                  </p>
                </div>

                <div>
                  <span className="statut-badge"
                    style={{ background: s.bg, color: s.color }}>
                    {s.label}
                  </span>
                  <div className="statut-message">{s.message}</div>
                </div>

                <div style={{ display: "flex", gap: "12px", marginTop: "4px" }}>
                  {c.statut === "EN_ATTENTE" && (
                    <button className="btn-annuler" onClick={() => handleAnnuler(c.id)}>
                      Annuler la candidature
                    </button>
                  )}
                  {c.statut === "ACCEPTEE" && (
                    <button
                      className="btn-invitation"
                      onClick={() =>
                        CandidatureService.telechargerInvitation(c.id)
                          .then(response => {
                            const url = window.URL.createObjectURL(new Blob([response.data]));
                            const link = document.createElement("a");
                            link.href = url;
                            link.setAttribute("download", `invitation_${c.offreTitre}.pdf`);
                            document.body.appendChild(link);
                            link.click();
                            link.remove();
                            window.URL.revokeObjectURL(url);
                          })
                          .catch(() => alert("Erreur lors du téléchargement de l'invitation."))
                      }
                    >
                       Télécharger l’invitation
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
};

export default MesCandidatures;