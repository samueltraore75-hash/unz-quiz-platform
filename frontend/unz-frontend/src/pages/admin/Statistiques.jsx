import { useEffect, useState } from "react";
import { adminApi } from "../../api/admin";
import PageHeader from "../../components/ui/PageHeader";
import StatCard from "../../components/ui/StatCard";
import BarChart from "../../components/ui/BarChart";

export default function Statistiques() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminApi.statistiques()
      .then(setStats)
      .catch((e) => setError(e.message || "Impossible de charger les statistiques."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page">
      <PageHeader
        eyebrow="Administration"
        title="Statistiques"
        description="Vue d'ensemble de la plateforme : effectifs, moyennes et activité par acteur."
      />

      {loading && <p className="empty">Chargement…</p>}
      {error && <p className="login-error">{error}</p>}

      {stats && (
        <>
          <div className="stats-row" style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 12, marginBottom: 22 }}>
            <StatCard label="Étudiants" value={stats.nbEtudiants} />
            <StatCard label="Enseignants" value={stats.nbEnseignants} />
            <StatCard label="Classes" value={stats.nbClasses} />
            <StatCard label="Devoirs créés" value={stats.nbDevoirs} />
            <StatCard label="Moyenne générale" value={`${stats.moyenneGenerale}/20`} green />
          </div>

          <div className="card" style={{ marginBottom: 18 }}>
            <p className="card-title">Effectifs par classe</p>
            <BarChart
              unit=" étud."
              rows={stats.effectifsParClasse.map((c) => ({ label: c.classeNom, value: c.nbEtudiants }))}
            />
          </div>

          <div className="card" style={{ marginBottom: 18 }}>
            <p className="card-title">Moyenne par matière (/20)</p>
            <BarChart
              unit="/20"
              rows={stats.moyennesParMatiere.map((m) => ({ label: m.matiereNom, value: m.moyenne }))}
            />
          </div>

          <div className="card">
            <p className="card-title">Activité des enseignants (devoirs créés)</p>
            <BarChart
              rows={stats.activiteEnseignants.map((a) => ({ label: a.enseignantNom.trim(), value: a.nbDevoirs }))}
            />
          </div>
        </>
      )}
    </div>
  );
}
