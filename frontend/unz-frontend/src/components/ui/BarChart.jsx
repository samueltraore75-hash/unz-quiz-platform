/**
 * Barre horizontale avec graduations (traits verticaux) — un "vrai" bar chart en CSS pur,
 * réutilisable partout dans l'app (admin, enseignant, étudiant).
 *
 * rows: [{ label, value, tone? }]  — tone: "green" | "red" | "blue" (couleur de la barre)
 * max:  valeur max de l'échelle (ex: 20 pour une note/20). Si absent, calculée depuis les données.
 */
export default function BarChart({ rows, unit = "", max }) {
  if (!rows || rows.length === 0) return <p className="barchart-empty">Aucune donnée pour le moment.</p>;
  const computedMax = max ?? Math.max(...rows.map((r) => r.value), 1);
  return (
    <div className="barchart">
      {rows.map((r, i) => {
        const pct = Math.max(2, Math.round((r.value / computedMax) * 100));
        return (
          <div className="barchart-row" key={r.label ?? i}>
            <span className="barchart-label" title={r.label}>{r.label}</span>
            <div className="barchart-track">
              <div className="barchart-grid"><span /><span /><span /><span /></div>
              <div className={`barchart-fill${r.tone ? ` tone-${r.tone}` : ""}`} style={{ width: `${pct}%` }} />
            </div>
            <span className="barchart-val">{r.value}{unit}</span>
          </div>
        );
      })}
    </div>
  );
}
