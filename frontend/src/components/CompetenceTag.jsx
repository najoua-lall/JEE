const CompetenceTag = ({ nom, onRemove }) => (
  <span style={{
    display: "inline-flex", alignItems: "center", gap: "6px",
    background: "rgba(99,102,241,0.12)", color: "#6366f1",
    border: "1px solid rgba(99,102,241,0.3)",
    borderRadius: "20px", padding: "4px 12px",
    fontSize: "13px", fontWeight: 600,
  }}>
    {nom}
    {onRemove && (
      <button onClick={onRemove} style={{
        background: "none", border: "none", cursor: "pointer",
        color: "#6366f1", fontSize: "16px", lineHeight: 1,
        padding: 0, display: "flex", alignItems: "center"
      }}>×</button>
    )}
  </span>
);

export default CompetenceTag;