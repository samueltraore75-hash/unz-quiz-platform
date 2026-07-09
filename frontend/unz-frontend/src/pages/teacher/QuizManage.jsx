import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { quizApi } from "../../api/quiz";
import { teacherApi } from "../../api/teacher";
import Chip from "../../components/ui/Chip";
import Button from "../../components/ui/Button";
import Select from "../../components/ui/Select";
import PageHeader from "../../components/ui/PageHeader";
import ProgressBar from "../../components/ui/ProgressBar";

export default function QuizManage() {
  const { id } = useParams();
  const [quiz, setQuiz] = useState(null);
  const [matieres, setMatieres] = useState([]);
  const [matiereChoisie, setMatiereChoisie] = useState("");
  const [banque, setBanque] = useState([]);
  const [selection, setSelection] = useState(new Set());
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  function reload() {
    return quizApi.detail(id).then(setQuiz);
  }

  useEffect(() => {
    Promise.all([reload(), teacherApi.myMatieres()])
      .then(([, m]) => setMatieres(m))
      .catch(() => setError("Impossible de charger ce devoir."))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!matiereChoisie) { setBanque([]); return; }
    teacherApi.questions(matiereChoisie).then(setBanque);
  }, [matiereChoisie]);

  function toggleSelection(qId) {
    setSelection((prev) => {
      const next = new Set(prev);
      next.has(qId) ? next.delete(qId) : next.add(qId);
      return next;
    });
  }

  async function handleAdd() {
    setError(""); setMessage("");
    try {
      await teacherApi.addQuestions(id, Array.from(selection));
      setSelection(new Set());
      setMessage("Questions ajoutées.");
      await reload();
    } catch (e) {
      setError(e.message || "Impossible d'ajouter ces questions.");
    }
  }

  async function handleFermer() {
    if (!window.confirm("Fermer ce devoir ? Les notes deviendront visibles pour les étudiants et, pour un examen, la note officielle sera enregistrée.")) return;
    try {
      await teacherApi.cloturerQuiz(id);
      await reload();
    } catch (e) {
      setError(e.message || "Impossible de fermer ce devoir.");
    }
  }

  if (loading) return <div className="page">Chargement…</div>;
  if (!quiz) return <div className="page">{error}</div>;

  const idsExistants = new Set(quiz.questions.map((q) => q.id));
  const pointsAttribues = quiz.questions.reduce((sum, q) => sum + q.points, 0);

  return (
    <div className="page">
      <PageHeader
        eyebrow={quiz.matiere}
        title={quiz.titre}
        action={<Chip tone={quiz.estCloture ? "neutral" : "terracotta"}>{quiz.estCloture ? "fermé" : "ouvert"}</Chip>}
      />

      <div className="card section-card">
        <ProgressBar value={pointsAttribues} max={Number(quiz.noteSur)} label={`Points attribués — devoir noté sur ${quiz.noteSur}`} />
        <div className="toolbar" style={{ marginBottom: 0 }}>
          <Link to={`/quiz/${id}/stats`}><Button variant="secondary">Statistiques</Button></Link>
          <Link to={`/quiz/${id}/notes`}><Button variant="secondary">Notes de la classe</Button></Link>
          {!quiz.estCloture && <Button variant="dark" onClick={handleFermer}>Fermer le devoir</Button>}
        </div>
      </div>

      {error && <p className="login-error">{error}</p>}
      {message && <p className="quiz-row-sub" style={{ color: "var(--success-text)", marginBottom: 12 }}>{message}</p>}

      <p className="section-title">Questions du devoir ({quiz.questions.length})</p>
      <div className="question-list" style={{ marginBottom: 28 }}>
        {quiz.questions.length === 0 && <p className="empty-hint">Aucune question ajoutée pour le moment.</p>}
        {quiz.questions.map((q, i) => (
          <div className="card question-card" key={q.id}>
            <p className="question-index">Question {i + 1} · {q.points} pt{q.points > 1 ? "s" : ""}</p>
            <p className="question-enonce">{q.enonce}</p>
          </div>
        ))}
      </div>

      {!quiz.estCloture && (
        <>
          <p className="section-title">Ajouter des questions depuis la banque</p>
          <Select
            label="Matière"
            value={matiereChoisie}
            onChange={(e) => setMatiereChoisie(e.target.value)}
            options={matieres.map((m) => ({ value: m.id, label: m.nom }))}
          />
          {banque.length > 0 && (
            <div className="choix-list" style={{ marginBottom: 16 }}>
              {banque.filter((q) => !idsExistants.has(q.id)).map((q) => (
                <label className={`choix-option ${selection.has(q.id) ? "checked" : ""}`} key={q.id}>
                  <input
                    type="checkbox"
                    checked={selection.has(q.id)}
                    onChange={() => toggleSelection(q.id)}
                  />
                  {q.enonce}
                  <span className="mono" style={{ marginLeft: "auto", fontSize: 12, color: "var(--ink-soft)" }}>{q.points} pt{q.points > 1 ? "s" : ""}</span>
                </label>
              ))}
            </div>
          )}
          {selection.size > 0 && (
            <Button onClick={handleAdd}>Ajouter {selection.size} question{selection.size > 1 ? "s" : ""}</Button>
          )}
        </>
      )}
    </div>
  );
}
