import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { quizApi } from "../../api/quiz";
import { gradesApi } from "../../api/grades";
import { useAuth } from "../../context/AuthContext";
import Chip from "../../components/ui/Chip";

function fmtDate(iso) {
  if (!iso) return null;
  return new Date(iso).toLocaleDateString("fr-FR", { day:"2-digit", month:"short" });
}

export default function StudentDashboard() {
  const { user } = useAuth();
  const [quizzes, setQuizzes]     = useState([]);
  const [bulletins, setBulletins] = useState([]);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    Promise.all([quizApi.list(), gradesApi.bulletinsMine()])
      .then(([q, b]) => { setQuizzes(q); setBulletins(b); })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;

  const ouverts = quizzes.filter(q => !q.estCloture);
  const b = bulletins[0];
  const moyenne = b?.moyenneGenerale != null ? Number(b.moyenneGenerale).toFixed(2) : "—";
  const credits = b ? `${b.creditsAcquis}/${b.creditsTotal}` : "—";

  return (
    <div className="page">
      {/* Hero */}
      <div className="hero">
        <div>
          <p className="hero-greet">Bonjour 👋</p>
          <p className="hero-name">{user?.firstName ? `${user.firstName} ${user.lastName || ""}` : user?.username}</p>
          {user?.classeNom && <p className="hero-sub">{user.classeNom}</p>}
        </div>
        <div className="hero-stats">
          <div>
            <p className="hero-stat-val hero-stat-acc">{moyenne}</p>
            <p className="hero-stat-lbl">Moyenne</p>
          </div>
          <div>
            <p className="hero-stat-val">{ouverts.length}</p>
            <p className="hero-stat-lbl">Devoirs ouverts</p>
          </div>
          <div>
            <p className="hero-stat-val">{credits}</p>
            <p className="hero-stat-lbl">Crédits</p>
          </div>
        </div>
      </div>

      {/* Devoirs ouverts */}
      <div className="section-lbl">
        Devoirs en cours
        <Link to="/quiz">Tout voir →</Link>
      </div>
      {ouverts.length === 0
        ? <p className="empty">Aucun devoir ouvert pour le moment.</p>
        : (
          <div className="list" style={{ marginBottom: 24 }}>
            {ouverts.slice(0, 5).map(quiz => (
              <div className="row" key={quiz.id}>
                <div className="row-top">
                  <div>
                    <p className="row-title">{quiz.titre}</p>
                    <p className="row-sub">{quiz.matiere} · noté /{quiz.noteSur} · {quiz.dureeMinutes} min{fmtDate(quiz.dateCloture) ? ` · clôture ${fmtDate(quiz.dateCloture)}` : ""}</p>
                  </div>
                  <Chip tone={quiz.typeQuiz === "EXAMEN" ? "warn" : "blue"}>
                    {quiz.typeQuiz === "EXAMEN" ? "Examen" : "Entraînement"}
                  </Chip>
                </div>
                <div className="row-actions">
                  <Link to={`/quiz/${quiz.id}`}>
                    <button className="btn btn-green btn-sm btn-round">
                      <i className="ti ti-player-play" aria-hidden="true" /> Commencer
                    </button>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}

      {/* Devoirs fermés récents */}
      {quizzes.filter(q => q.estCloture).length > 0 && (
        <>
          <div className="section-lbl">Résultats récents</div>
          <div className="list">
            {quizzes.filter(q => q.estCloture).slice(0, 3).map(quiz => (
              <div className="row" key={quiz.id}>
                <div className="row-top">
                  <div>
                    <p className="row-title">{quiz.titre}</p>
                    <p className="row-sub">{quiz.matiere} · noté /{quiz.noteSur}</p>
                  </div>
                  <Chip tone="grey">Fermé</Chip>
                </div>
                <div className="row-actions">
                  <Link to="/notes">
                    <button className="btn btn-blue btn-sm btn-round">
                      <i className="ti ti-eye" aria-hidden="true" /> Voir résultats
                    </button>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
