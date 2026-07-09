import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { quizApi } from "../../api/quiz";
import { teacherApi } from "../../api/teacher";
import { useAuth } from "../../context/AuthContext";
import Chip from "../../components/ui/Chip";

export default function TeacherDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [quizzes, setQuizzes]   = useState([]);
  const [matieres, setMatieres] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");

  function load() {
    return Promise.all([quizApi.list(), teacherApi.myMatieres()])
      .then(([q, m]) => { setQuizzes(q); setMatieres(m); });
  }

  useEffect(() => { load().finally(() => setLoading(false)); }, []);

  async function handleDelete(id) {
    if (!window.confirm("Supprimer ce devoir ?")) return;
    try {
      await teacherApi.deleteQuiz(id);
      setQuizzes(qs => qs.filter(q => q.id !== id));
    } catch (e) { setError(e.message || "Impossible de supprimer."); }
  }

  async function handleClose(id) {
    if (!window.confirm("Fermer ce devoir ? Les notes deviendront visibles pour les étudiants.")) return;
    try {
      await teacherApi.cloturerQuiz(id);
      await load();
    } catch (e) { setError(e.message || "Impossible de fermer ce devoir."); }
  }

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;

  const ouverts = quizzes.filter(q => !q.estCloture);

  return (
    <div className="page">
      <div className="hero">
        <div>
          <p className="hero-greet">Espace enseignant</p>
          <p className="hero-name">{user?.firstName ? `${user.firstName} ${user.lastName || ""}` : user?.username}</p>
        </div>
        <div className="hero-stats">
          <div><p className="hero-stat-val hero-stat-acc">{ouverts.length}</p><p className="hero-stat-lbl">Ouverts</p></div>
          <div><p className="hero-stat-val">{quizzes.length}</p><p className="hero-stat-lbl">Total devoirs</p></div>
          <div><p className="hero-stat-val">{matieres.length}</p><p className="hero-stat-lbl">Matières</p></div>
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}><i className="ti ti-alert-circle" aria-hidden="true" />{error}</div>}

      <div className="toolbar">
        <Link to="/quiz/nouveau">
          <button className="btn btn-green btn-round">
            <i className="ti ti-plus" aria-hidden="true" /> Créer un devoir
          </button>
        </Link>
        <Link to="/questions">
          <button className="btn btn-ghost btn-round">
            <i className="ti ti-database" aria-hidden="true" /> Gérer la banque de questions
          </button>
        </Link>
        <Link to="/notes/saisie">
          <button className="btn btn-ghost btn-round">
            <i className="ti ti-pencil" aria-hidden="true" /> Saisir des notes
          </button>
        </Link>
      </div>

      <div className="section-lbl">Mes devoirs ({quizzes.length})</div>
      {quizzes.length === 0
        ? <p className="empty">Aucun devoir créé. Cliquez sur "Créer un devoir" pour commencer.</p>
        : (
          <div className="list">
            {quizzes.map(quiz => (
              <div className="row" key={quiz.id}>
                <div className="row-top">
                  <div>
                    <p className="row-title">{quiz.titre}</p>
                    <p className="row-sub">{quiz.matiere} · {quiz.classe} · /{quiz.noteSur} · {quiz.nbQuestions} question{quiz.nbQuestions > 1 ? "s" : ""}</p>
                  </div>
                  <Chip tone={quiz.estCloture ? "grey" : "green"}>
                    {quiz.estCloture ? "Fermé" : "Ouvert"}
                  </Chip>
                </div>
                <div className="row-actions">
                  <button className="btn btn-ghost btn-sm btn-round" onClick={() => navigate(`/quiz/${quiz.id}/gerer`)}>
                    <i className="ti ti-edit" aria-hidden="true" /> {quiz.estCloture ? "Voir" : "Modifier"}
                  </button>
                  <button className="btn btn-blue btn-sm btn-round" onClick={() => navigate(`/quiz/${quiz.id}/stats`)}>
                    <i className="ti ti-chart-bar" aria-hidden="true" /> Stats
                  </button>
                  <button className="btn btn-ghost btn-sm btn-round" onClick={() => navigate(`/quiz/${quiz.id}/notes`)}>
                    <i className="ti ti-file-text" aria-hidden="true" /> Notes
                  </button>
                  {!quiz.estCloture && (
                    <button className="btn btn-ghost btn-sm btn-round" onClick={() => handleClose(quiz.id)}>
                      <i className="ti ti-lock" aria-hidden="true" /> Fermer
                    </button>
                  )}
                  <button className="btn btn-danger btn-sm btn-round" onClick={() => handleDelete(quiz.id)}>
                    <i className="ti ti-trash" aria-hidden="true" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
    </div>
  );
}
