import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { teacherApi } from "../../api/teacher";
import VerticalBarChart from "../../components/ui/VerticalBarChart";

export default function QuizNotes() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    teacherApi.quizNotes(id).then(setData).catch((e) => setError(e.message || "Impossible de charger les notes."));
  }, [id]);

  if (error) return <div className="page">{error}</div>;
  if (!data) return <div className="page">Chargement…</div>;

  return (
    <div className="page">
      <h1 className="dash-title" style={{ marginBottom: 6 }}>{data.quizTitre}</h1>
      <p className="quiz-row-sub" style={{ marginBottom: 24 }}>
        Barème : {data.baremeTotal} pts · Moyenne de classe : {data.moyenneClasse ?? "—"}/20
      </p>

      <div className="card" style={{ marginBottom: 20 }}>
        <p className="card-title">Progression des étudiants</p>
        <VerticalBarChart
          max={20}
          unit="/20"
          rows={data.resultats.map((r) => ({
            label: r.etudiantNom,
            value: r.noteSur20 ?? 0,
            tone: r.noteSur20 == null ? "grey" : r.noteSur20 >= 10 ? "green" : "red",
          }))}
        />
      </div>

      <div className="quiz-list">
        {data.resultats.map((r, i) => (
          <div className="card quiz-row" key={i}>
            <p className="quiz-row-title" style={{ marginBottom: 0 }}>{r.etudiantNom}</p>
            <span className="mono" style={{ fontSize: 14, fontWeight: 600 }}>{r.noteSur20}/20</span>
          </div>
        ))}
      </div>
    </div>
  );
}
