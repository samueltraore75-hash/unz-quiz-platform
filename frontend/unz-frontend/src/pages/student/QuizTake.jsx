import { useEffect, useState, useCallback, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { quizApi } from "../../api/quiz";
import Timer from "../../components/ui/Timer";
import Chip from "../../components/ui/Chip";

function initReponses(quiz, saved) {
  const map = {};
  quiz.questions.forEach(q => { map[q.id] = { choixIds: new Set(), reponseTexte: "" }; });
  (saved || []).forEach(r => {
    map[r.questionId] = { choixIds: new Set(r.choixIds || []), reponseTexte: r.reponseTexte || "" };
  });
  return map;
}

export default function QuizTake() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [quiz, setQuiz]           = useState(null);
  const [tentativeId, setTentId]  = useState(null);
  const [dateDebut, setDateDebut] = useState(null);
  const [reponses, setReponses]   = useState({});
  const [loading, setLoading]     = useState(true);
  const [starting, setStarting]   = useState(false);
  const [submitting, setSub]      = useState(false);
  const [error, setError]         = useState("");
  const submittedRef = useRef(false);
  const saveTimers   = useRef({});

  useEffect(() => {
    let cancelled = false;
    quizApi.detail(id).then(async q => {
      if (cancelled) return;
      setQuiz(q);
      if (q.tentativeEnCoursId) {
        const enc = await quizApi.enCours(q.tentativeEnCoursId);
        if (cancelled) return;
        setTentId(enc.id);
        setDateDebut(enc.dateDebut);
        setReponses(initReponses(q, enc.reponses));
      }
      setLoading(false);
    }).catch(() => { setError("Impossible de charger ce devoir."); setLoading(false); });
    return () => { cancelled = true; };
  }, [id]);

  async function handleStart() {
    setError(""); setStarting(true);
    try {
      const { tentativeId: nid, dateDebut: dd } = await quizApi.start(id);
      setTentId(nid); setDateDebut(dd); setReponses(initReponses(quiz, []));
    } catch (e) { setError(e.message || "Impossible de démarrer."); }
    finally { setStarting(false); }
  }

  function scheduleAutosave(questionId, payload) {
    clearTimeout(saveTimers.current[questionId]);
    saveTimers.current[questionId] = setTimeout(() => {
      quizApi.sauvegarderReponse(tentativeId, { questionId, ...payload }).catch(() => {});
    }, 500);
  }

  function toggleChoix(question, choixId) {
    setReponses(prev => {
      const cur = new Set(prev[question.id]?.choixIds || []);
      if (question.reponseMultiple) { cur.has(choixId) ? cur.delete(choixId) : cur.add(choixId); }
      else { cur.clear(); cur.add(choixId); }
      scheduleAutosave(question.id, { choixIds: Array.from(cur) });
      return { ...prev, [question.id]: { ...prev[question.id], choixIds: cur } };
    });
  }

  function updateTexte(question, texte) {
    setReponses(prev => ({ ...prev, [question.id]: { ...prev[question.id], reponseTexte: texte } }));
    scheduleAutosave(question.id, { reponseTexte: texte });
  }

  const nbRepondues = quiz
    ? quiz.questions.filter(q => {
        const r = reponses[q.id];
        if (!r) return false;
        return q.type === "REPONSE_COURTE" ? r.reponseTexte?.trim().length > 0 : r.choixIds?.size > 0;
      }).length : 0;

  const handleSubmit = useCallback(async () => {
    if (submittedRef.current || !tentativeId) return;
    const restantes = quiz.questions.length - nbRepondues;
    if (restantes > 0) {
      if (!window.confirm(`${restantes} question${restantes > 1 ? "s" : ""} sans réponse. Soumettre quand même ?`)) return;
    }
    submittedRef.current = true; setSub(true);
    try {
      const result = await quizApi.submit(tentativeId);
      navigate(`/quiz/${id}/resultat`, { state: { result } });
    } catch (e) {
      setError(e.message || "Impossible de soumettre.");
      submittedRef.current = false; setSub(false);
    }
  }, [tentativeId, id, navigate, nbRepondues, quiz]);

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;
  if (!quiz)   return <div className="page">{error || "Devoir introuvable."}</div>;
  if (quiz.estCloture) return (
    <div className="page"><div className="card"><p style={{fontWeight:600,marginBottom:6}}>Devoir fermé</p><p style={{color:"var(--ink-3)"}}>Consultez vos résultats depuis la page Notes.</p></div></div>
  );

  if (!tentativeId) {
    const restantes = quiz.typeQuiz === "ENTRAINEMENT" ? quiz.tentativesMax - (quiz.tentativesUtilisees || 0) : null;
    const attente   = quiz.prochaineTentativeDisponibleA ? new Date(quiz.prochaineTentativeDisponibleA) : null;
    const enAttente = attente && attente > new Date();
    return (
      <div className="page">
        <div className="card" style={{ maxWidth: 560 }}>
          <h2 style={{ fontWeight:700, fontSize:18, marginBottom:6 }}>{quiz.titre}</h2>
          <p style={{ color:"var(--ink-3)", fontSize:13, marginBottom:20 }}>
            {quiz.matiere} · {quiz.questions.length} questions · noté /{quiz.noteSur} · {quiz.dureeMinutes} min
          </p>
          {quiz.typeQuiz === "EXAMEN" && (
            <div className="alert alert-warning" style={{ marginBottom:16 }}>
              <i className="ti ti-alert-triangle" aria-hidden="true" />
              Tentative unique — une fois démarré vous ne pouvez pas recommencer.
            </div>
          )}
          {quiz.typeQuiz === "ENTRAINEMENT" && (
            <div className="alert alert-info" style={{ marginBottom:16 }}>
              <i className="ti ti-info-circle" aria-hidden="true" />
              Tentatives restantes : {restantes} / {quiz.tentativesMax}
            </div>
          )}
          {enAttente && (
            <div className="alert alert-warning" style={{ marginBottom:16 }}>
              <i className="ti ti-clock" aria-hidden="true" />
              Prochaine tentative disponible à {attente.toLocaleTimeString("fr-FR", { hour:"2-digit", minute:"2-digit" })}
            </div>
          )}
          {error && <div className="alert alert-error" style={{ marginBottom:16 }}><i className="ti ti-alert-circle" aria-hidden="true" />{error}</div>}
          <button className="btn btn-green btn-lg btn-round" onClick={handleStart} disabled={starting || enAttente || restantes === 0}>
            {starting ? "Démarrage…" : <><i className="ti ti-player-play" aria-hidden="true" /> Commencer le devoir</>}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="quiz-header">
        <div>
          <h2 style={{ fontWeight:700, fontSize:17 }}>{quiz.titre}</h2>
          <p style={{ color:"var(--ink-3)", fontSize:12 }}>{nbRepondues}/{quiz.questions.length} répondues</p>
        </div>
        <Timer dateDebut={dateDebut} dureeMinutes={quiz.dureeMinutes} onExpire={handleSubmit} />
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom:16 }}><i className="ti ti-alert-circle" aria-hidden="true" />{error}</div>}

      <div className="list" style={{ marginBottom:20 }}>
        {quiz.questions.map((question, index) => {
          const rep = reponses[question.id] || { choixIds: new Set(), reponseTexte: "" };
          const repondu = question.type === "REPONSE_COURTE" ? rep.reponseTexte?.trim().length > 0 : rep.choixIds?.size > 0;
          return (
            <div className="card" key={question.id} style={{ marginBottom:10 }}>
              <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:8 }}>
                <p className="q-num">Question {index + 1} · {question.points} pt{question.points > 1 ? "s" : ""}</p>
                {repondu && <Chip tone="green"><i className="ti ti-check" aria-hidden="true" /> Répondu</Chip>}
              </div>
              <p className="q-text">{question.enonce}</p>
              {question.type === "REPONSE_COURTE"
                ? <input className="field-input" placeholder="Votre réponse…" value={rep.reponseTexte} onChange={e => updateTexte(question, e.target.value)} />
                : (
                  <div>
                    {question.choix.map(choix => {
                      const checked = rep.choixIds?.has(choix.id) || false;
                      return (
                        <label key={choix.id} className={`choice ${checked ? "picked" : ""}`}>
                          <input type={question.reponseMultiple ? "checkbox" : "radio"} name={`q-${question.id}`} checked={checked} onChange={() => toggleChoix(question, choix.id)} />
                          {choix.texte}
                        </label>
                      );
                    })}
                  </div>
                )}
            </div>
          );
        })}
      </div>

      <button className="btn btn-green btn-xl btn-block btn-round" onClick={handleSubmit} disabled={submitting}>
        {submitting ? "Envoi en cours…" : <><i className="ti ti-send" aria-hidden="true" /> Soumettre le devoir</>}
      </button>
    </div>
  );
}
