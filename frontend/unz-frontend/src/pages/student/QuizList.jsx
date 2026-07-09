import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { quizApi } from "../../api/quiz";
import Chip from "../../components/ui/Chip";

export default function QuizList() {
  const [quizzes, setQuizzes] = useState([]);
  const [filter, setFilter]   = useState("ouverts");
  const [loading, setLoading] = useState(true);

  useEffect(() => { quizApi.list().then(setQuizzes).finally(() => setLoading(false)); }, []);

  const filtered = quizzes.filter(q => filter === "ouverts" ? !q.estCloture : q.estCloture);

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;

  return (
    <div className="page">
      <div className="tabs">
        <button className={`tab ${filter === "ouverts" ? "active" : ""}`} onClick={() => setFilter("ouverts")}>
          Ouverts ({quizzes.filter(q => !q.estCloture).length})
        </button>
        <button className={`tab ${filter === "fermes" ? "active" : ""}`} onClick={() => setFilter("fermes")}>
          Fermés ({quizzes.filter(q => q.estCloture).length})
        </button>
      </div>

      {filtered.length === 0
        ? <p className="empty">Aucun devoir {filter === "ouverts" ? "ouvert" : "fermé"}.</p>
        : (
          <div className="list">
            {filtered.map(quiz => (
              <div className="row" key={quiz.id}>
                <div className="row-top">
                  <div>
                    <p className="row-title">{quiz.titre}</p>
                    <p className="row-sub">{quiz.matiere} · {quiz.classe} · noté /{quiz.noteSur} · {quiz.dureeMinutes} min</p>
                  </div>
                  <Chip tone={quiz.estCloture ? "grey" : quiz.typeQuiz === "EXAMEN" ? "warn" : "blue"}>
                    {quiz.estCloture ? "Fermé" : quiz.typeQuiz === "EXAMEN" ? "Examen" : "Entraînement"}
                  </Chip>
                </div>
                <div className="row-actions">
                  {quiz.estCloture
                    ? <Link to="/notes"><button className="btn btn-blue btn-sm btn-round"><i className="ti ti-eye" aria-hidden="true" /> Voir résultats</button></Link>
                    : <Link to={`/quiz/${quiz.id}`}><button className="btn btn-green btn-sm btn-round"><i className="ti ti-player-play" aria-hidden="true" /> Commencer</button></Link>
                  }
                </div>
              </div>
            ))}
          </div>
        )}
    </div>
  );
}
