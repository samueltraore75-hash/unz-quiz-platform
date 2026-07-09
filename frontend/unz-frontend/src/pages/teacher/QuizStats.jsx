import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { teacherApi } from "../../api/teacher";
import VerticalBarChart from "../../components/ui/VerticalBarChart";

export default function QuizStats() {
  const { id } = useParams();
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    teacherApi.quizStats(id).then(setStats).catch(() => setError("Impossible de charger les statistiques."));
  }, [id]);

  if (error) return <div className="page">{error}</div>;
  if (!stats) return <div className="page">Chargement…</div>;

  return (
    <div className="page">
      <h1 className="dash-title" style={{ marginBottom: 6 }}>{stats.quizTitre}</h1>
      <p className="quiz-row-sub" style={{ marginBottom: 24 }}>
        Statistiques anonymisées et agrégées — aucune réponse individuelle n'est identifiable.
      </p>

      <div className="card" style={{ marginBottom: 20 }}>
        <p className="card-title">Taux de réussite par question</p>
        <VerticalBarChart
          max={100}
          unit="%"
          rows={stats.statistiques.map((q, i) => ({
            label: `Q${i + 1}`,
            value: q.tauxReussite,
            tone: q.tauxReussite >= 50 ? "green" : "red",
          }))}
        />
      </div>

      <div className="question-list">
        {stats.statistiques.map((q, i) => (
          <div className="card question-card" key={q.questionId}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline" }}>
              <p className="question-index" style={{ margin: 0 }}>Question {i + 1}</p>
              <span className="mono" style={{ fontSize: 13, color: "var(--indigo)" }}>
                {q.tauxReussite}% de réussite
              </span>
            </div>
            <p className="question-enonce">{q.enonce}</p>
            <div className="choix-list">
              {q.repartitionChoix.map((c) => (
                <div key={c.choixId} className="choix-option" style={{ justifyContent: "space-between" }}>
                  <span>{c.texte}{c.estCorrect ? " ✓" : ""}</span>
                  <span className="mono" style={{ fontSize: 12, color: "var(--ink-soft)" }}>{c.pourcentage}%</span>
                </div>
              ))}
            </div>
            {q.tauxReussiteT1 != null && (
              <p className="quiz-row-sub" style={{ marginTop: 10 }}>
                1re tentative : {q.tauxReussiteT1}% · tentatives suivantes : {q.tauxReussiteTentativesSuivantes ?? "—"}%
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
