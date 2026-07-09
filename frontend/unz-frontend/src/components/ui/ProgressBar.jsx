export default function ProgressBar({ label, value, max }) {
  const pct = max > 0 ? Math.min(100, Math.round(value / max * 100)) : 0;
  const tone = value > max ? "over" : value === max ? "done" : "ok";
  return (
    <div className="pb">
      <div className="pb-top"><span className="pb-label">{label}</span><span className="pb-val mono">{value} / {max}</span></div>
      <div className="pb-track"><div className={`pb-fill ${tone}`} style={{ width:`${pct}%` }} /></div>
    </div>
  );
}
