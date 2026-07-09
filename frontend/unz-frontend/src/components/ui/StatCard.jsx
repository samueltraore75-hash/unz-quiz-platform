export default function StatCard({ label, value, green }) {
  return (
    <div className={`stat-card${green ? " green" : ""}`}>
      <p className="stat-val mono">{value}</p>
      <p className="stat-lbl">{label}</p>
    </div>
  );
}
