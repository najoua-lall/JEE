import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import OffreService from "../services/offre.service";
import AuthService from "../services/auth.service";
import CompetenceTag from "../components/CompetenceTag";

const SECTEURS = ["IT", "Finance", "Marketing", "RH", "Industrie", "Santé", "Autre"];

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap');
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  :root {
    --bg: #eef2ff; --surface: #ffffff; --primary: #6366f1; --accent: #8b5cf6;
    --text: #1e1b4b; --text-muted: #6b7280; --border: rgba(99,102,241,0.15);
    --error: #ef4444; --font: 'Sora', sans-serif; --mono: 'JetBrains Mono', monospace;
  }
  body { font-family: var(--font); background: var(--bg); color: var(--text); }
  .page-root { min-height: 100vh; background: #eef2ff; padding: 2.5rem; font-family: var(--font); position: relative; overflow-x: hidden; }
  .bg-grid { position: fixed; inset: 0; background-image: linear-gradient(rgba(99,102,241,0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(99,102,241,0.06) 1px, transparent 1px); background-size: 52px 52px; animation: gridShift 28s linear infinite; pointer-events: none; z-index: 0; }
  @keyframes gridShift { 0%{transform:translate(0,0)} 100%{transform:translate(52px,52px)} }
  .orb { position:fixed;border-radius:50%;filter:blur(90px);pointer-events:none;z-index:0;animation:orbFloat 9s ease-in-out infinite alternate; }
  .orb-1 { width:500px;height:500px;background:radial-gradient(circle,rgba(99,102,241,0.15),transparent 70%);top:-200px;left:-150px; }
  .orb-2 { width:400px;height:400px;background:radial-gradient(circle,rgba(139,92,246,0.12),transparent 70%);bottom:-100px;right:-100px;animation-delay:-4s; }
  @keyframes orbFloat { 0%{transform:translate(0,0) scale(1)} 100%{transform:translate(30px,20px) scale(1.06)} }
  .content { position: relative; z-index: 1; max-width: 1400px; margin: 0 auto; }
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 12px; }
  .page-title { font-size: 32px; font-weight: 700; color: #1e1b4b; letter-spacing: -0.03em; }
  .page-title span { color: var(--primary); }
  .page-subtitle { font-size: 13px; color: #9ca3af; font-family: var(--mono); margin-top: 4px; }
  .btn-primary { background: linear-gradient(135deg,#6366f1,#8b5cf6); color: #fff; border: none; border-radius: 12px; padding: 12px 22px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: var(--font); box-shadow: 0 4px 16px rgba(99,102,241,0.35); transition: opacity 0.2s, transform 0.15s; }
  .btn-primary:hover { opacity: 0.9; transform: translateY(-1px); }
  .btn-secondary { background: rgba(255,255,255,0.8); color: #3730a3; border: 1.5px solid rgba(99,102,241,0.25); border-radius: 12px; padding: 12px 22px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: var(--font); transition: background 0.2s; }
  .btn-secondary:hover { background: rgba(99,102,241,0.08); }
  .btn-danger { background: rgba(255,255,255,0.8); color: #dc2626; border: 1.5px solid rgba(239,68,68,0.25); border-radius: 12px; padding: 12px 22px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: var(--font); transition: background 0.2s; }
  .btn-danger:hover { background: rgba(239,68,68,0.06); }
  .header-actions { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
  .filter-bar { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 24px; align-items: center; background: rgba(255,255,255,0.85); border: 1.5px solid rgba(99,102,241,0.14); border-radius: 16px; padding: 16px; backdrop-filter: blur(12px); }
  .filter-input { flex: 2; min-width: 180px; padding: 11px 16px; border: 2px solid rgba(99,102,241,0.18); border-radius: 10px; font-size: 14px; font-family: var(--font); outline: none; background: rgba(238,242,255,0.6); color: var(--text); transition: border-color 0.2s; }
  .filter-input:focus { border-color: rgba(99,102,241,0.5); background: #fff; }
  .filter-select { flex: 1; min-width: 130px; padding: 11px 14px; border: 2px solid rgba(99,102,241,0.18); border-radius: 10px; font-size: 14px; font-family: var(--font); outline: none; background: rgba(238,242,255,0.6); color: var(--text); cursor: pointer; }
  .btn-reset { padding: 11px 18px; background: rgba(99,102,241,0.08); border: 1px solid rgba(99,102,241,0.2); border-radius: 10px; color: #6366f1; cursor: pointer; font-weight: 600; font-size: 13px; font-family: var(--font); transition: background 0.2s; }
  .btn-reset:hover { background: rgba(99,102,241,0.15); }
  .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 1.5rem; }
  .card { background: rgba(255,255,255,0.90); border: 1.5px solid rgba(99,102,241,0.14); border-radius: 20px; padding: 1.75rem; backdrop-filter: blur(16px); box-shadow: 0 4px 20px rgba(99,102,241,0.08); display: flex; flex-direction: column; transition: transform 0.2s, box-shadow 0.2s; animation: cardIn 0.5s cubic-bezier(0.22,1,0.36,1) both; }
  .card:hover { transform: translateY(-3px); box-shadow: 0 12px 32px rgba(99,102,241,0.14); }
  @keyframes cardIn { from{opacity:0;transform:translateY(20px)} to{opacity:1;transform:translateY(0)} }
  .card-tag { display: inline-flex; align-items: center; gap: 5px; background: rgba(99,102,241,0.10); color: var(--primary); border-radius: 20px; padding: 4px 12px; font-size: 12px; font-weight: 600; font-family: var(--mono); margin-bottom: 14px; width: fit-content; }
  .card-title { font-size: 18px; font-weight: 700; color: #1e1b4b; margin-bottom: 8px; }
  .card-meta { font-size: 13px; color: var(--text-muted); margin: 4px 0; display: flex; align-items: center; gap: 6px; }
  .card-desc { font-size: 13.5px; color: #475569; margin: 12px 0 10px; line-height: 1.65; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; flex: 1; }
  .card-footer { font-size: 11px; color: #a5b4fc; font-family: var(--mono); margin-bottom: 10px; }
  .tags-row { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
  .secteur-badge { background: rgba(139,92,246,0.1); color: #8b5cf6; border-radius: 8px; padding: 2px 10px; font-size: 11px; font-weight: 600; width: fit-content; margin-bottom: 8px; }
  .card-actions { display: flex; gap: 8px; flex-direction: column; }
  .card-actions-row { display: flex; gap: 8px; }
  .btn-edit { flex: 1; padding: 10px; border-radius: 10px; border: 1.5px solid rgba(99,102,241,0.2); background: rgba(238,242,255,0.7); color: #3730a3; font-size: 13px; font-weight: 600; cursor: pointer; font-family: var(--font); transition: background 0.2s; }
  .btn-edit:hover { background: rgba(99,102,241,0.12); }
  .btn-del { flex: 1; padding: 10px; border-radius: 10px; border: none; background: rgba(239,68,68,0.08); color: #dc2626; font-size: 13px; font-weight: 600; cursor: pointer; font-family: var(--font); transition: background 0.2s; }
  .btn-del:hover { background: rgba(239,68,68,0.15); }
  .btn-postuler { width: 100%; padding: 10px; border-radius: 10px; border: none; font-size: 13px; font-weight: 600; cursor: pointer; font-family: var(--font); transition: all 0.2s; margin-top: 4px; }
  .btn-candidatures { width: 100%; padding: 10px; border-radius: 10px; border: 1.5px solid rgba(99,102,241,0.25); background: rgba(238,242,255,0.7); color: #3730a3; font-size: 13px; font-weight: 600; cursor: pointer; font-family: var(--font); transition: background 0.2s; margin-top: 4px; }
  .btn-candidatures:hover { background: rgba(99,102,241,0.1); }
  .pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: 32px; }
  .page-btn { width: 38px; height: 38px; border-radius: 10px; border: 1.5px solid rgba(99,102,241,0.2); background: #fff; color: #6366f1; cursor: pointer; font-weight: 600; font-size: 14px; font-family: var(--font); transition: all 0.2s; }
  .page-btn.active { background: #6366f1; color: #fff; border-color: #6366f1; }
  .page-btn:hover:not(.active) { background: rgba(99,102,241,0.08); }
  .nav-btn { padding: 8px 16px; border-radius: 10px; border: 1.5px solid rgba(99,102,241,0.2); background: #fff; color: #6366f1; cursor: pointer; font-weight: 600; font-size: 14px; font-family: var(--font); transition: all 0.2s; }
  .nav-btn:disabled { color: #ccc; cursor: not-allowed; }
  .nav-btn:hover:not(:disabled) { background: rgba(99,102,241,0.08); }
  .state-msg { text-align: center; padding: 3rem; font-size: 16px; color: var(--text-muted); }
  .state-err { color: var(--error); }

  /* Banner d'information pour le recruteur */
  .recruiter-banner {
    background: linear-gradient(135deg, rgba(99,102,241,0.08), rgba(139,92,246,0.06));
    border: 1.5px solid rgba(99,102,241,0.2);
    border-radius: 14px;
    padding: 12px 20px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #3730a3;
    font-weight: 500;
  }
  .recruiter-banner strong { color: var(--primary); }
`;

const OffresList = () => {
  const [offres, setOffres]         = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState("");
  const [search, setSearch]         = useState("");
  const [secteur, setSecteur]       = useState("");
  const [competence, setCompetence] = useState("");
  const [page, setPage]             = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const SIZE = 6;

  const navigate    = useNavigate();
  const currentUser = AuthService.getCurrentUser();

  const isRecruteur = currentUser?.roles?.some(
    r => r === "ROLE_RECRUTEUR" || r === "ROLE_ADMIN"
  );
  const isEtudiant = currentUser?.roles?.some(r => r === "ROLE_ETUDIANT");
  const isAdmin    = currentUser?.roles?.some(r => r === "ROLE_ADMIN");

  const fetchOffres = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const params = {
        search:     search     || undefined,
        secteur:    secteur    || undefined,
        competence: competence || undefined,
        page,
        size: SIZE,
      };

      // Un recruteur ne voit QUE ses propres offres via /mes-offres
      const { data } = isRecruteur && !isAdmin
        ? await OffreService.searchMesOffres(params)
        : await OffreService.searchOffres(params);

      setOffres(data.content);
      setTotalPages(data.totalPages);
    } catch {
      setError("Impossible de charger les offres.");
    } finally {
      setLoading(false);
    }
  }, [search, secteur, competence, page, isRecruteur, isAdmin]);

  useEffect(() => { fetchOffres(); }, [fetchOffres]);
  useEffect(() => { setPage(0); }, [search, secteur, competence]);

  const handleDelete = async (id) => {
    if (!window.confirm("Supprimer cette offre ?")) return;
    try {
      await OffreService.deleteOffre(id);
      fetchOffres();
    } catch {
      alert("Erreur lors de la suppression.");
    }
  };

  const handlePostuler = (offre) => {
    navigate(`/offres/${offre.id}/postuler`, { state: { offre } });
  };

  const reset = () => {
    setSearch(""); setSecteur(""); setCompetence(""); setPage(0);
  };

  return (
    <>
      <style>{styles}</style>
      <div className="page-root">
        <div className="bg-grid" />
        <div className="orb orb-1" />
        <div className="orb orb-2" />

        <div className="content">

          {/* ── Header ── */}
          <div className="page-header">
            <div>
              <h1 className="page-title">
                {isRecruteur && !isAdmin ? "Mes offres de " : "Offres de "}
                <span>stage</span>
              </h1>
              {isRecruteur && !isAdmin && (
                <p className="page-subtitle">
                  Vous ne voyez que vos propres offres publiées
                </p>
              )}
            </div>
            <div className="header-actions">
              {isAdmin && (
                <button className="btn-secondary"
                  onClick={() => navigate("/statistiques")}>
                   Statistiques
                </button>
              )}
              {isRecruteur && (
                <button className="btn-primary"
                  onClick={() => navigate("/offres/create")}>
                  + Nouvelle offre
                </button>
              )}
              {isEtudiant && (
                <button className="btn-secondary"
                  onClick={() => navigate("/mes-candidatures")}>
                   Mes candidatures
                </button>
              )}
              <button className="btn-danger"
                onClick={() => { AuthService.logout(); navigate("/login"); }}>
                Déconnexion
              </button>
            </div>
          </div>

          {/* ── Bannière recruteur ── */}
          {isRecruteur && !isAdmin && (
            <div className="recruiter-banner">
             Mode recruteur — <strong>isolation activée</strong> : vous ne pouvez voir, modifier ou supprimer que vos propres offres.
            </div>
          )}

          {/* ── Filtres ── */}
          <div className="filter-bar">
            <input className="filter-input"
              placeholder=" Rechercher (titre, entreprise...)"
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
            <select className="filter-select" value={secteur}
              onChange={e => setSecteur(e.target.value)}>
              <option value="">Tous les secteurs</option>
              {SECTEURS.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
            <input className="filter-input"
              style={{ flex: 1, minWidth: "130px" }}
              placeholder="Compétence (ex: React)"
              value={competence}
              onChange={e => setCompetence(e.target.value)}
            />
            <button className="btn-reset" onClick={reset}>
              ↺ Réinitialiser
            </button>
          </div>

          {/* ── États ── */}
          {loading && <p className="state-msg">Chargement…</p>}
          {error   && <p className="state-msg state-err">{error}</p>}
          {!loading && offres.length === 0 && !error && (
            <p className="state-msg">
              {isRecruteur && !isAdmin
                ? "Vous n'avez pas encore publié d'offre."
                : "Aucune offre trouvée."}
            </p>
          )}

          {/* ── Grille ── */}
          <div className="grid">
            {offres.map((offre, idx) => {
              // Pour un recruteur, toutes les offres affichées lui appartiennent
              const isOwner = isRecruteur
                ? true
                : currentUser?.username === offre.recruteurUsername;

              return (
                <div key={offre.id} className="card"
                  style={{ animationDelay: `${idx * 0.06}s` }}>

                  <span className="card-tag">{offre.localisation}</span>

                  {offre.secteur && (
                    <span className="secteur-badge">{offre.secteur}</span>
                  )}

                  <p className="card-title">{offre.titre}</p>
                  <p className="card-meta"> {offre.entreprise}</p>
                  <p className="card-meta"> {offre.dateDebut} → {offre.dateFin}</p>
                  <p className="card-desc">{offre.description}</p>

                  {offre.competences?.length > 0 && (
                    <div className="tags-row">
                      {offre.competences.map(c => (
                        <CompetenceTag key={c.id} nom={c.nom} />
                      ))}
                    </div>
                  )}

                  <p className="card-footer">{offre.recruteurUsername}</p>

                  <div className="card-actions">

                    {/* Recruteur propriétaire */}
                    {isOwner && isRecruteur && (
                      <>
                        <div className="card-actions-row">
                          <button className="btn-edit"
                            onClick={() => navigate(`/offres/edit/${offre.id}`)}>
                             Modifier
                          </button>
                          <button className="btn-del"
                            onClick={() => handleDelete(offre.id)}>
                             Supprimer
                          </button>
                        </div>
                        <button className="btn-candidatures"
                          onClick={() => navigate(`/offres/${offre.id}/candidatures`)}>
                           Voir les candidatures
                        </button>
                      </>
                    )}

                    {/* Étudiant */}
                    {isEtudiant && (
                      <button
                        className="btn-postuler"
                        onClick={() => handlePostuler(offre)}
                        style={{
                          background: "linear-gradient(135deg,#6366f1,#8b5cf6)",
                          color: "#fff",
                        }}
                      >
                         Postuler
                      </button>
                    )}

                  </div>
                </div>
              );
            })}
          </div>

          {/* ── Pagination ── */}
          {totalPages > 1 && (
            <div className="pagination">
              <button className="nav-btn"
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}>
                ← Précédent
              </button>
              {[...Array(totalPages)].map((_, i) => (
                <button key={i}
                  className={`page-btn ${i === page ? "active" : ""}`}
                  onClick={() => setPage(i)}>
                  {i + 1}
                </button>
              ))}
              <button className="nav-btn"
                disabled={page === totalPages - 1}
                onClick={() => setPage(p => p + 1)}>
                Suivant →
              </button>
            </div>
          )}

        </div>
      </div>
    </>
  );
};

export default OffresList;