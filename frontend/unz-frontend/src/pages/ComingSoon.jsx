export default function ComingSoon({ label }) {
  return (
    <div className="page" style={{ textAlign:"center", paddingTop:64 }}>
      <i className="ti ti-tools" style={{ fontSize:40, color:"var(--ink-4)" }} aria-hidden="true" />
      <h1 style={{ fontSize:22,color:"var(--ink)",margin:"14px 0 8px" }}>{label}</h1>
      <p style={{ color:"var(--ink-3)" }}>Cette section est en cours de construction.</p>
    </div>
  );
}
