/**
 * Barres VERTICALES avec graduations horizontales (traits) — pour les vues de
 * progression étudiant/enseignant (une colonne par tentative, matière, etc.).
 *
 * rows: [{ label, value, tone? }]  — tone: "green" | "red" | "blue" | "grey"
 * max:  valeur max de l'échelle (ex: 20 pour une note/20). Si absent, calculée depuis les données.
 */
export default function VerticalBarChart({ rows, unit = "", max }) {
  if (!rows || rows.length === 0) return <p className="barchart-empty">Aucune donnée pour le moment.</p>;
  const computedMax = max ?? Math.max(...rows.map((r) => r.value), 1);
  return (
    <div className="vbarchart">
      <div className="vbarchart-plot">
        <div className="vbarchart-grid"><span /><span /><span /><span /></div>
        {rows.map((r, i) => {
          const pct = Math.max(2, Math.round((r.value / computedMax) * 100));
          return (
            <div className="vbar-col" key={r.label ?? i}>
              <span className="vbar-val">{r.value}{unit}</span>
              <div className={`vbar-fill${r.tone ? ` tone-${r.tone}` : ""}`} style={{ height: `${pct}%` }} />
            </div>
          );
        })}
      </div>
      <div className="vbarchart-labels">
        {rows.map((r, i) => (
          <span className="vbar-label" title={r.label} key={r.label ?? i}>{r.label}</span>
        ))}
      </div>
    </div>
  );
}
