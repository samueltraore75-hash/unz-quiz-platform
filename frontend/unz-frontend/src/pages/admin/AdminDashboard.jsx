import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { adminApi } from "../../api/admin";
import { useAuth } from "../../context/AuthContext";

const QA = [
  { to: "/utilisateurs", icon: "ti-users",     title: "Utilisateurs",       desc: "Créer · Modifier · Réinitialiser · Désactiver" },
  { to: "/referentiel",  icon: "ti-building",   title: "Référentiel",        desc: "Filières · Niveaux · Classes · UE · Matières" },
  { to: "/bulletins",    icon: "ti-file-text",  title: "Bulletins",          desc: "Générer · Vérifier · Publier · Télécharger" },
  { to: "/statistiques", icon: "ti-chart-bar",  title: "Statistiques",       desc: "Moyennes · Résultats · Progression · Suivi" },
];

export default function AdminDashboard() {
  const { user } = useAuth();
  const [counts, setCounts] = useState({ etudiants:0, enseignants:0, classes:0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([adminApi.users("ETUDIANT"), adminApi.users("ENSEIGNANT"), adminApi.classes()])
      .then(([e, en, cl]) => setCounts({ etudiants:e.length, enseignants:en.length, classes:cl.length }))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;

  return (
    <div className="page">
      <div className="hero">
        <div>
          <p className="hero-greet">Administration</p>
          <p className="hero-name">Université Norbert Zongo</p>
          <p className="hero-sub">Année académique 2025-2026</p>
        </div>
        <div className="hero-stats">
          <div><p className="hero-stat-val hero-stat-acc">{counts.etudiants}</p><p className="hero-stat-lbl">Étudiants</p></div>
          <div><p className="hero-stat-val">{counts.enseignants}</p><p className="hero-stat-lbl">Enseignants</p></div>
          <div><p className="hero-stat-val">{counts.classes}</p><p className="hero-stat-lbl">Classes</p></div>
        </div>
      </div>

      <div className="section-lbl">Accès rapide — 1 clic</div>
      <div className="qa-grid">
        {QA.map((q, i) => (
          <Link to={q.to} key={i}>
            <div className="qa-card">
              <div className="qa-icon"><i className={`ti ${q.icon}`} aria-hidden="true" /></div>
              <p className="qa-title">{q.title}</p>
              <p className="qa-desc">{q.desc}</p>
            </div>
          </Link>
        ))}
      </div>

      <div className="section-lbl">Actions fréquentes</div>
      <div className="toolbar">
        <Link to="/utilisateurs"><button className="btn btn-green btn-round"><i className="ti ti-user-plus" aria-hidden="true" /> Créer un compte</button></Link>
        <Link to="/referentiel"><button className="btn btn-ghost btn-round"><i className="ti ti-building-plus" aria-hidden="true" /> Ajouter une classe</button></Link>
        <Link to="/bulletins"><button className="btn btn-ghost btn-round"><i className="ti ti-file-plus" aria-hidden="true" /> Générer des bulletins</button></Link>
      </div>
    </div>
  );
}
