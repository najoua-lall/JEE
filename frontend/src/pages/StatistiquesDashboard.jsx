import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import StatistiquesService from "../services/statistiques.service";

const COLORS = ["#6366f1", "#8b5cf6", "#06b6d4", "#10b981", "#f59e0b", "#ef4444", "#ec4899", "#14b8a6"];
const STATUT_COLORS = {
  EN_ATTENTE: "#f59e0b",
  ACCEPTEE:   "#10b981",
  REFUSEE:    "#ef4444",
};
const STATUT_LABELS = {
  EN_ATTENTE: "En attente",
  ACCEPTEE:   "Acceptées",
  REFUSEE:    "Refusées",
};

// ── Donut Chart ───────────────────────────────────────────────────────────────
const DonutChart = ({ data, title, colors }) => {
  const [hovered, setHovered] = useState(null);
  const total = data.reduce((s, d) => s + d.value, 0);
  if (total === 0) return (
    <p style={{ textAlign: "center", color: "#9ca3af", padding: "2rem", fontFamily: "'JetBrains Mono'" }}>
      Aucune donnée
    </p>
  );

  const R = 70, cx = 90, cy = 90, stroke = 26;
  const circumference = 2 * Math.PI * R;
  let offset = 0;

  const slices = data.map((d, i) => {
    const pct = d.value / total;
    const dash = pct * circumference;
    const slice = { ...d, dash, offset: circumference - offset, color: colors[i % colors.length] };
    offset += dash;
    return slice;
  });

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "28px", flexWrap: "wrap" }}>
      <div style={{ position: "relative", flexShrink: 0 }}>
        <svg viewBox="0 0 180 180" width={160} height={160}>
          <circle cx={cx} cy={cy} r={R} fill="none"
            stroke="rgba(99,102,241,0.06)" strokeWidth={stroke} />
          {slices.map((s, i) => (
            <circle key={i} cx={cx} cy={cy} r={R} fill="none"
              stroke={s.color}
              strokeWidth={hovered === i ? stroke + 5 : stroke}
              strokeOpacity={hovered !== null && hovered !== i ? 0.25 : 1}
              strokeDasharray={`${s.dash} ${circumference - s.dash}`}
              strokeDashoffset={s.offset}
              transform={`rotate(-90 ${cx} ${cy})`}
              style={{ transition: "all 0.2s", cursor: "pointer" }}
              onMouseEnter={() => setHovered(i)}
              onMouseLeave={() => setHovered(null)}
            />
          ))}
          <text x={cx} y={cy - 8} textAnchor="middle"
            fontSize="24" fontWeight="800" fill="#1e1b4b"
            fontFamily="'JetBrains Mono'">{total}</text>
          <text x={cx} y={cy + 10} textAnchor="middle"
            fontSize="9" fill="#9ca3af"
            fontFamily="'JetBrains Mono'">{title}</text>
        </svg>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "10px", flex: 1, minWidth: "130px" }}>
        {slices.map((s, i) => (
          <div key={i}
            onMouseEnter={() => setHovered(i)}
            onMouseLeave={() => setHovered(null)}
            style={{
              display: "flex", alignItems: "center", gap: "10px",
              cursor: "pointer",
              opacity: hovered !== null && hovered !== i ? 0.35 : 1,
              transition: "opacity 0.2s",
            }}>
            <div style={{
              width: 8, height: 8, borderRadius: 2,
              background: s.color, flexShrink: 0
            }} />
            <span style={{ fontSize: "13px", color: "#374151", flex: 1 }}>{s.label}</span>
            <span style={{
              fontSize: "12px", fontFamily: "'JetBrains Mono'",
              color: s.color, fontWeight: 700
            }}>
              {s.value} · {((s.value / total) * 100).toFixed(0)}%
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

// ── Bar Chart ─────────────────────────────────────────────────────────────────
const BarChart = ({ data, color = "#6366f1" }) => {
  const [hovered, setHovered] = useState(null);
  if (!data || data.length === 0) return (
    <p style={{ textAlign: "center", color: "#9ca3af", padding: "2rem", fontFamily: "'JetBrains Mono'" }}>
      Aucune donnée
    </p>
  );

  const maxVal = Math.max(...data.map(d => d.value), 1);

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
      {data.map((d, i) => {
        const pct = (d.value / maxVal) * 100;
        const isH = hovered === i;
        return (
          <div key={i}
            onMouseEnter={() => setHovered(i)}
            onMouseLeave={() => setHovered(null)}
            style={{ display: "flex", alignItems: "center", gap: "12px", cursor: "pointer" }}>

            {/* Label */}
            <div style={{
              width: "100px", fontSize: "12px", color: isH ? "#1e1b4b" : "#6b7280",
              fontFamily: "'JetBrains Mono'", textAlign: "right",
              transition: "color 0.2s", flexShrink: 0,
              overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap"
            }}>
              {d.label}
            </div>

            {/* Barre */}
            <div style={{
              flex: 1, height: "28px",
              background: "rgba(99,102,241,0.05)",
              borderRadius: "6px", overflow: "hidden",
              border: "1px solid rgba(99,102,241,0.08)"
            }}>
              <div style={{
                width: `${pct}%`, height: "100%",
                background: isH
                  ? `linear-gradient(90deg, ${color}, ${color}bb)`
                  : `linear-gradient(90deg, ${color}bb, ${color}77)`,
                borderRadius: "6px",
                transition: "all 0.3s cubic-bezier(0.22,1,0.36,1)",
                minWidth: d.value > 0 ? "6px" : "0"
              }} />
            </div>

            {/* Valeur */}
            <div style={{
              width: "32px", fontSize: "13px",
              fontFamily: "'JetBrains Mono'",
              color: isH ? color : "#9ca3af",
              fontWeight: isH ? 700 : 400,
              transition: "all 0.2s", textAlign: "right", flexShrink: 0
            }}>
              {d.value}
            </div>
          </div>
        );
      })}
    </div>
  );
};

// ── KPI Card ──────────────────────────────────────────────────────────────────
const KpiCard = ({ label, value, color, sub, delay }) => (
  <div style={{
    background: "#fff",
    border: `1.5px solid ${color}18`,
    borderRadius: "16px",
    padding: "24px 28px",
    boxShadow: `0 4px 24px ${color}10`,
    animation: `cardIn 0.5s cubic-bezier(0.22,1,0.36,1) ${delay}s both`,
    position: "relative", overflow: "hidden",
  }}>
    {/* Accent top */}
    <div style={{
      position: "absolute", top: 0, left: 0, right: 0,
      height: "3px",
      background: `linear-gradient(90deg, ${color}, ${color}44)`,
      borderRadius: "16px 16px 0 0"
    }} />

    <p style={{
      fontSize: "38px", fontWeight: "800",
      color: "#1e1b4b", fontFamily: "'JetBrains Mono'",
      lineHeight: 1, marginBottom: "10px"
    }}>
      {value}
    </p>
    <p style={{ fontSize: "13px", color: "#6b7280", fontWeight: 500 }}>
      {label}
    </p>
    {sub && (
      <p style={{
        fontSize: "11px", color: color,
        fontFamily: "'JetBrains Mono'",
        marginTop: "6px", fontWeight: 600
      }}>
        {sub}
      </p>
    )}
  </div>
);

// ── Chart Card ────────────────────────────────────────────────────────────────
const ChartCard = ({ title, subtitle, children, delay = 0 }) => (
  <div style={{
    background: "#fff",
    border: "1.5px solid rgba(99,102,241,0.10)",
    borderRadius: "16px",
    padding: "28px 32px",
    boxShadow: "0 4px 20px rgba(99,102,241,0.06)",
    animation: `cardIn 0.5s cubic-bezier(0.22,1,0.36,1) ${delay}s both`,
  }}>
    <div style={{ marginBottom: "22px", paddingBottom: "16px", borderBottom: "1px solid rgba(99,102,241,0.08)" }}>
      <p style={{ fontSize: "15px", fontWeight: 700, color: "#1e1b4b" }}>{title}</p>
      {subtitle && (
        <p style={{ fontSize: "11px", color: "#9ca3af", fontFamily: "'JetBrains Mono'", marginTop: "5px" }}>
          {subtitle}
        </p>
      )}
    </div>
    {children}
  </div>
);

// ── Section Label ─────────────────────────────────────────────────────────────
const SectionLabel = ({ children, mt = 0 }) => (
  <div style={{
    display: "flex", alignItems: "center", gap: "12px",
    marginBottom: "16px", marginTop: mt
  }}>
    <span style={{
      fontSize: "11px", fontWeight: 700, color: "#6366f1",
      textTransform: "uppercase", letterSpacing: "0.1em",
      fontFamily: "'JetBrains Mono'", whiteSpace: "nowrap"
    }}>
      {children}
    </span>
    <div style={{ flex: 1, height: "1px", background: "rgba(99,102,241,0.10)" }} />
  </div>
);

// ── Dashboard ─────────────────────────────────────────────────────────────────
const StatistiquesDashboard = () => {
  const [stats, setStats]     = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState("");
  const navigate              = useNavigate();

  const load = () => {
    setLoading(true);
    StatistiquesService.getStatistiques()
      .then(r => setStats(r.data))
      .catch(e => {
        setError(e.response?.status === 403
          ? "Accès réservé aux administrateurs."
          : "Impossible de charger les statistiques.");
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const secteurData = stats
    ? Object.entries(stats.offreParSecteur)
        .map(([label, value]) => ({ label, value }))
        .sort((a, b) => b.value - a.value)
    : [];

  const competenceData = stats
    ? stats.topCompetences.map(c => ({ label: c.nom, value: c.count }))
    : [];

  const statutData = stats
    ? Object.entries(stats.candidaturesParStatut).map(([key, value]) => ({
        label: STATUT_LABELS[key] || key,
        value,
      }))
    : [];

  const tauxAcceptation = stats && stats.totalCandidatures > 0
    ? Math.round(((stats.candidaturesParStatut.ACCEPTEE || 0) / stats.totalCandidatures) * 100)
    : 0;

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600;700&display=swap');
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Sora', sans-serif; background: #f5f7ff; }

        .dash-root { min-height: 100vh; background: #f5f7ff; padding: 2.5rem; }

        .content { max-width: 1280px; margin: 0 auto; }

        .page-header {
          display: flex; justify-content: space-between;
          align-items: flex-start; margin-bottom: 2.5rem;
          gap: 16px; flex-wrap: wrap;
          padding-bottom: 24px;
          border-bottom: 1.5px solid rgba(99,102,241,0.10);
        }
        .page-title { font-size: 28px; font-weight: 800; color: #1e1b4b; letter-spacing: -0.04em; }
        .page-title span { color: #6366f1; }
        .page-sub { font-size: 13px; color: #9ca3af; font-family: 'JetBrains Mono'; margin-top: 6px; }

        .role-tag {
          display: inline-block;
          background: rgba(99,102,241,0.08);
          color: #6366f1; border: 1px solid rgba(99,102,241,0.2);
          border-radius: 6px; padding: 3px 10px;
          font-size: 11px; font-weight: 700;
          font-family: 'JetBrains Mono'; margin-top: 8px;
          letter-spacing: 0.08em;
        }

        .header-actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

        .btn-back {
          padding: 9px 18px; border-radius: 8px;
          border: 1.5px solid rgba(99,102,241,0.2);
          background: #fff; color: #3730a3;
          font-size: 13px; font-weight: 600;
          cursor: pointer; font-family: 'Sora';
          transition: all 0.2s;
        }
        .btn-back:hover { background: rgba(99,102,241,0.05); border-color: rgba(99,102,241,0.35); }

        .btn-refresh {
          padding: 9px 18px; border-radius: 8px;
          border: 1.5px solid rgba(99,102,241,0.15);
          background: rgba(99,102,241,0.06); color: #6366f1;
          font-size: 13px; font-weight: 600;
          cursor: pointer; font-family: 'JetBrains Mono';
          transition: all 0.2s;
        }
        .btn-refresh:hover { background: rgba(99,102,241,0.12); }

        .kpi-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
          gap: 16px; margin-bottom: 28px;
        }

        .charts-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
        .charts-1 { display: grid; grid-template-columns: 1fr; gap: 20px; margin-bottom: 20px; }

        @media (max-width: 860px) {
          .charts-2 { grid-template-columns: 1fr; }
        }

        @keyframes cardIn {
          from { opacity: 0; transform: translateY(20px); }
          to   { opacity: 1; transform: translateY(0); }
        }

        .spinner {
          width: 36px; height: 36px;
          border: 2.5px solid rgba(99,102,241,0.15);
          border-top-color: #6366f1;
          border-radius: 50%;
          animation: spin 0.7s linear infinite;
          margin: 0 auto;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
      `}</style>

      <div className="dash-root">
        <div className="content">

          {/* ── Header ── */}
          <div className="page-header">
            <div>
              <h1 className="page-title">
                Tableau de <span>bord</span>
              </h1>
              <p className="page-sub">
                Vue d'ensemble de la plateforme
              </p>
              <span className="role-tag">ROLE_ADMIN</span>
            </div>
            <div className="header-actions">
              <button className="btn-refresh" onClick={load}>
                Actualiser
              </button>
              <button className="btn-back" onClick={() => navigate("/offres")}>
                ← Retour
              </button>
            </div>
          </div>

          {/* ── Loading ── */}
          {loading && (
            <div style={{ padding: "5rem", textAlign: "center" }}>
              <div className="spinner" />
              <p style={{ marginTop: "16px", color: "#9ca3af", fontSize: "13px", fontFamily: "'JetBrains Mono'" }}>
                Chargement…
              </p>
            </div>
          )}

          {/* ── Error ── */}
          {!loading && error && (
            <div style={{ textAlign: "center", padding: "4rem", color: "#ef4444" }}>
              <p style={{ fontSize: "14px" }}>{error}</p>
            </div>
          )}

          {/* ── Content ── */}
          {!loading && stats && (
            <>
              {/* KPI */}
              <SectionLabel>Vue d'ensemble</SectionLabel>
              <div className="kpi-grid">
                <KpiCard label="Offres publiées"     value={stats.totalOffres}       color="#6366f1" delay={0.05} />
                <KpiCard label="Candidatures"        value={stats.totalCandidatures} color="#8b5cf6" delay={0.10} />
                <KpiCard label="Étudiants inscrits"  value={stats.totalEtudiants}    color="#06b6d4" delay={0.15} />
                <KpiCard label="Recruteurs inscrits" value={stats.totalRecruteurs}   color="#10b981" delay={0.20} />
              </div>

              {/* Charts ligne 1 */}
              <SectionLabel mt={8}>Candidatures et secteurs</SectionLabel>
              <div className="charts-2">

                <ChartCard
                  title="Statut des candidatures"
                  subtitle={`${stats.totalCandidatures} candidatures au total`}
                  delay={0.25}
                >
                  <DonutChart
                    data={statutData}
                    title="total"
                    colors={Object.values(STATUT_COLORS)}
                  />
                </ChartCard>

                <ChartCard
                  title="Répartition par secteur"
                  subtitle={`${secteurData.length} secteurs`}
                  delay={0.30}
                >
                  <DonutChart
                    data={secteurData}
                    title="offres"
                    colors={COLORS}
                  />
                </ChartCard>

              </div>

              {/* Compétences */}
              <SectionLabel mt={8}>Compétences les plus demandées</SectionLabel>
              <div className="charts-1">
                <ChartCard
                  title="Top compétences"
                  subtitle="Nombre d'offres requérant chaque compétence"
                  delay={0.35}
                >
                  <BarChart data={competenceData} color="#6366f1" />
                </ChartCard>
              </div>

              {/* Secteurs détail */}
              {secteurData.length > 0 && (
                <>
                  <SectionLabel mt={8}>Détail par secteur</SectionLabel>
                  <div className="charts-1">
                    <ChartCard
                      title="Offres par secteur"
                      subtitle="Distribution par domaine d'activité"
                      delay={0.40}
                    >
                      <BarChart data={secteurData} color="#8b5cf6" />
                    </ChartCard>
                  </div>
                </>
              )}

              {/* Indicateurs */}
              {stats.totalCandidatures > 0 && (
                <>
                  <SectionLabel mt={8}>Indicateurs clés</SectionLabel>
                  <div className="kpi-grid">
                    <KpiCard
                      label="Taux d'acceptation"
                      value={`${tauxAcceptation}%`}
                      color="#10b981"
                      sub={`${stats.candidaturesParStatut.ACCEPTEE || 0} acceptées`}
                      delay={0.45}
                    />
                    <KpiCard
                      label="Taux de refus"
                      value={`${Math.round(((stats.candidaturesParStatut.REFUSEE || 0) / stats.totalCandidatures) * 100)}%`}
                      color="#ef4444"
                      sub={`${stats.candidaturesParStatut.REFUSEE || 0} refusées`}
                      delay={0.50}
                    />
                    <KpiCard
                      label="En attente"
                      value={stats.candidaturesParStatut.EN_ATTENTE || 0}
                      color="#f59e0b"
                      sub="à traiter"
                      delay={0.55}
                    />
                    <KpiCard
                      label="Candidatures / offre"
                      value={stats.totalOffres > 0
                        ? (stats.totalCandidatures / stats.totalOffres).toFixed(1)
                        : "—"}
                      color="#6366f1"
                      sub="en moyenne"
                      delay={0.60}
                    />
                  </div>
                </>
              )}

            </>
          )}
        </div>
      </div>
    </>
  );
};

export default StatistiquesDashboard;