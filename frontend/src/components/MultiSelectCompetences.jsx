import { useState, useEffect } from "react";
import competenceService from "../services/competence.service";
import CompetenceTag from "./CompetenceTag";

const MultiSelectCompetences = ({ selected = [], onChange }) => {
  const [allCompetences, setAllCompetences] = useState([]);
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [creating, setCreating] = useState(false);

  // Charger toutes les compétences existantes
  useEffect(() => {
    competenceService.getAll()
      .then(r => setAllCompetences(r.data))
      .catch(() => console.error("Erreur chargement compétences"));
  }, []);

  // Compétences filtrées (non encore sélectionnées)
  const filtered = allCompetences.filter(c =>
    c.nom.toLowerCase().includes(search.toLowerCase()) &&
    !selected.find(s => s.id === c.id)
  );

  // Vérifie si la saisie correspond exactement à une compétence existante
  const exactMatch = allCompetences.find(
    c => c.nom.toLowerCase() === search.toLowerCase()
  );

  // Ajouter une compétence existante
  const add = (competence) => {
    onChange([...selected, competence]);
    setSearch("");
    setOpen(false);
  };

  // Créer et ajouter une nouvelle compétence
  const createAndAdd = async () => {
    if (!search.trim() || creating) return;
    setCreating(true);
    try {
      const res = await competenceService.create({ nom: search.trim() });
      const newComp = res.data;
      setAllCompetences(prev => [...prev, newComp]);
      onChange([...selected, newComp]);
      setSearch("");
      setOpen(false);
    } catch (err) {
      // Si conflit (déjà existante), on la charge
      if (err.response?.status === 409) {
        const existing = allCompetences.find(
          c => c.nom.toLowerCase() === search.toLowerCase()
        );
        if (existing) add(existing);
      }
    } finally {
      setCreating(false);
    }
  };

  // Supprimer une compétence sélectionnée
  const remove = (id) => onChange(selected.filter(c => c.id !== id));

  // Créer avec la touche Enter
  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      if (filtered.length > 0 && !search) return;
      if (exactMatch) {
        add(exactMatch);
      } else if (search.trim()) {
        createAndAdd();
      }
    }
    if (e.key === "Escape") setOpen(false);
  };

  return (
    <div style={{ position: "relative" }}>

      {/* Zone des tags sélectionnés + input */}
      <div
        style={{
          display: "flex", flexWrap: "wrap", gap: "8px",
          minHeight: "48px", padding: "8px 12px",
          border: "2px solid rgba(99,102,241,0.18)",
          borderRadius: "12px",
          background: open ? "rgba(255,255,255,0.95)" : "rgba(238,242,255,0.6)",
          cursor: "text", transition: "all 0.2s",
          boxShadow: open ? "0 0 0 4px rgba(99,102,241,0.10)" : "none",
          borderColor: open ? "rgba(99,102,241,0.55)" : "rgba(99,102,241,0.18)",
        }}
        onClick={() => { setOpen(true); }}
      >
        {/* Tags sélectionnés */}
        {selected.map(c => (
          <CompetenceTag key={c.id} nom={c.nom} onRemove={() => remove(c.id)} />
        ))}

        {/* Input de recherche/création */}
        <input
          value={search}
          onChange={e => { setSearch(e.target.value); setOpen(true); }}
          onFocus={() => setOpen(true)}
          onKeyDown={handleKeyDown}
          placeholder={selected.length === 0 ? "Tapez une compétence et appuyez sur Entrée..." : "Ajouter..."}
          style={{
            border: "none", outline: "none",
            background: "transparent",
            fontSize: "14px", minWidth: "180px",
            flex: 1, padding: "4px 0",
            color: "#1e1b4b", fontFamily: "'Sora', sans-serif",
          }}
        />
      </div>

      {/* Dropdown */}
      {open && (
        <div style={{
          position: "absolute", top: "calc(100% + 6px)", left: 0, right: 0,
          background: "#fff",
          border: "1.5px solid rgba(99,102,241,0.2)",
          borderRadius: "12px",
          boxShadow: "0 8px 32px rgba(99,102,241,0.15)",
          zIndex: 200, overflow: "hidden",
          maxHeight: "240px", overflowY: "auto",
        }}>

          {/* Option créer si pas de match exact */}
          {search.trim() && !exactMatch && (
            <div
              onClick={createAndAdd}
              style={{
                padding: "12px 16px", cursor: "pointer",
                fontSize: "14px", fontWeight: 600,
                color: "#6366f1",
                background: "rgba(99,102,241,0.05)",
                borderBottom: "1px solid rgba(99,102,241,0.1)",
                display: "flex", alignItems: "center", gap: "8px",
              }}
              onMouseEnter={e => e.currentTarget.style.background = "rgba(99,102,241,0.1)"}
              onMouseLeave={e => e.currentTarget.style.background = "rgba(99,102,241,0.05)"}
            >
              {creating ? (
                <span>⏳ Création en cours...</span>
              ) : (
                <span>✨ Créer "<strong>{search}</strong>"</span>
              )}
            </div>
          )}

          {/* Compétences existantes filtrées */}
          {filtered.length > 0 ? (
            filtered.map(c => (
              <div key={c.id}
                onClick={() => add(c)}
                style={{
                  padding: "11px 16px", cursor: "pointer",
                  fontSize: "14px", color: "#1e1b4b",
                  display: "flex", alignItems: "center", gap: "8px",
                  transition: "background 0.15s",
                }}
                onMouseEnter={e => e.currentTarget.style.background = "#eef2ff"}
                onMouseLeave={e => e.currentTarget.style.background = "transparent"}
              >
                <span style={{
                  width: "8px", height: "8px", borderRadius: "50%",
                  background: "#a5b4fc", flexShrink: 0
                }} />
                {c.nom}
              </div>
            ))
          ) : (
            !search.trim() && (
              <div style={{
                padding: "12px 16px", fontSize: "13px",
                color: "#9ca3af", textAlign: "center"
              }}>
                Tapez pour rechercher ou créer une compétence
              </div>
            )
          )}
        </div>
      )}

      {/* Overlay pour fermer le dropdown */}
      {open && (
        <div
          style={{ position: "fixed", inset: 0, zIndex: 199 }}
          onClick={() => setOpen(false)}
        />
      )}

      {/* Aide */}
      <p style={{
        fontSize: "11px", color: "#9ca3af",
        marginTop: "6px", fontFamily: "'JetBrains Mono', monospace"
      }}>
        Tapez une compétence → sélectionnez ou appuyez Entrée pour créer
      </p>
    </div>
  );
};

export default MultiSelectCompetences;