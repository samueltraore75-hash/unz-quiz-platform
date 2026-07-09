import { Link } from "react-router-dom";
export default function NotFound() {
  return (
    <div className="page" style={{ textAlign:"center", paddingTop:80 }}>
      <p style={{ fontSize:64,fontWeight:800,color:"var(--line-2)",marginBottom:16 }}>404</p>
      <h1 style={{ fontSize:22,color:"var(--ink)",marginBottom:8 }}>Page introuvable</h1>
      <p style={{ color:"var(--ink-3)",marginBottom:24 }}>La page demandée n'existe pas.</p>
      <Link to="/" className="btn btn-dark btn-round">← Retour à l'accueil</Link>
    </div>
  );
}
