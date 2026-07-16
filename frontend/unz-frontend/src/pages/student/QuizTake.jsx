import { useEffect, useState, useCallback, useRef, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { quizApi } from "../../api/quiz";
import Timer from "../../components/ui/Timer";
import Chip from "../../components/ui/Chip";

// v3.3 : anti-triche — poids de chaque type d'événement (doit rester cohérent avec
// StatsService.POIDS_EVENEMENT côté backend, qui calcule le même score pour l'enseignant).
const POIDS_EVENEMENT = {
  PERTE_FOCUS: 1,
  RETOUR_FOCUS: 0,
  CHANGEMENT_ONGLET: 3,
  SORTIE_PLEIN_ECRAN: 4,
  COPIER_COLLER: 5,
};

// v3.3 : mélange déterministe — même ordre à chaque rechargement de page pour une
// tentative donnée, mais différent d'un étudiant/tentative à l'autre (rend le
// partage de réponses du type "la question 3 c'est B" inutile entre étudiants).
function seedFromString(str) {
  let h = 0;
  for (let i = 0; i < str.length; i++) { h = (h << 5) - h + str.charCodeAt(i); h |= 0; }
  return h >>> 0;
}
function seededShuffle(array, seed) {
  const arr = array.slice();
  let s = seed >>> 0;
  function rand() {
    s = (s + 0x6D2B79F5) | 0;
    let t = Math.imul(s ^ (s >>> 15), 1 | s);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  }
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(rand() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

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

  // ── Anti-triche : état ────────────────────────────────────────────────
  const [nbEvents, setNbEvents]         = useState(0);
  const [score, setScore]               = useState(0);
  const [countdown, setCountdown]       = useState(null); // secondes restantes avant soumission forcée, ou null
  const [fullscreenNeeded, setFullscreenNeeded] = useState(false);
  const tentativeIdRef  = useRef(null);
  const lastSentRef     = useRef({});
  const seuilRef        = useRef(null);
  const nextTriggerRef  = useRef(1); // multiplicateur du seuil pour le prochain déclenchement
  const countdownTimer  = useRef(null);

  useEffect(() => { tentativeIdRef.current = tentativeId; }, [tentativeId]);

  useEffect(() => {
    let cancelled = false;
    quizApi.detail(id).then(async q => {
      if (cancelled) return;
      setQuiz(q);
      seuilRef.current = q.seuilAlerteScore || 15;
      if (q.tentativeEnCoursId) {
        const enc = await quizApi.enCours(q.tentativeEnCoursId);
        if (cancelled) return;
        setTentId(enc.id);
        setDateDebut(enc.dateDebut);
        setReponses(initReponses(q, enc.reponses));
        // v3.3 : reprise après rechargement — le navigateur a quitté le plein écran,
        // il faut redemander explicitement (un clic utilisateur est requis par l'API).
        if (q.pleinEcranObligatoire) setFullscreenNeeded(true);
      }
      setLoading(false);
    }).catch(() => { setError("Impossible de charger ce devoir."); setLoading(false); });
    return () => { cancelled = true; };
  }, [id]);

  // ── Anti-triche : détection des comportements suspects ─────────────────
  function cancelCountdown() {
    if (countdownTimer.current) { clearInterval(countdownTimer.current); countdownTimer.current = null; }
    setCountdown(null);
  }

  const startCountdown = useCallback(() => {
    if (countdownTimer.current) return;
    let restant = 15;
    setCountdown(restant);
    countdownTimer.current = setInterval(() => {
      restant -= 1;
      if (restant <= 0) {
        clearInterval(countdownTimer.current);
        countdownTimer.current = null;
        setCountdown(null);
        handleSubmit(true);
      } else {
        setCountdown(restant);
      }
    }, 1000);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function signaler(type) {
    const tid = tentativeIdRef.current;
    if (!tid || submittedRef.current) return;
    const now = Date.now();
    const dernier = lastSentRef.current[type] || 0;
    if (now - dernier < 3000) return; // anti-spam : 3s minimum entre deux signalements du même type
    lastSentRef.current[type] = now;
    const poids = POIDS_EVENEMENT[type] ?? 1;
    setNbEvents(n => n + 1);
    setScore(s => {
      const suivant = s + poids;
      const seuil = seuilRef.current;
      if (seuil && suivant >= seuil * nextTriggerRef.current) {
        nextTriggerRef.current += 1;
        startCountdown();
      }
      return suivant;
    });
    quizApi.signalerEvenement(tid, type).catch(() => {});
  }

  useEffect(() => {
    function onVisibilityChange() {
      signaler(document.hidden ? "CHANGEMENT_ONGLET" : "RETOUR_FOCUS");
    }
    function onBlur() { signaler("PERTE_FOCUS"); }
    function onCopy()  { signaler("COPIER_COLLER"); }
    function onPaste() { signaler("COPIER_COLLER"); }
    function onFullscreenChange() {
      if (!quiz?.pleinEcranObligatoire) return;
      if (!document.fullscreenElement) {
        if (!submittedRef.current) { signaler("SORTIE_PLEIN_ECRAN"); setFullscreenNeeded(true); }
      } else {
        setFullscreenNeeded(false);
      }
    }

    document.addEventListener("visibilitychange", onVisibilityChange);
    window.addEventListener("blur", onBlur);
    document.addEventListener("copy", onCopy);
    document.addEventListener("paste", onPaste);
    document.addEventListener("fullscreenchange", onFullscreenChange);
    return () => {
      document.removeEventListener("visibilitychange", onVisibilityChange);
      window.removeEventListener("blur", onBlur);
      document.removeEventListener("copy", onCopy);
      document.removeEventListener("paste", onPaste);
      document.removeEventListener("fullscreenchange", onFullscreenChange);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [quiz]);

  useEffect(() => () => { if (countdownTimer.current) clearInterval(countdownTimer.current); }, []);

  async function enterFullscreen() {
    try { await document.documentElement.requestFullscreen(); setFullscreenNeeded(false); } catch { /* refusé par le navigateur */ }
  }

  async function handleStart() {
    setError(""); setStarting(true);
    try {
      const { tentativeId: nid, dateDebut: dd } = await quizApi.start(id);
      setTentId(nid); setDateDebut(dd); setReponses(initReponses(quiz, []));
      if (quiz.pleinEcranObligatoire) {
        try { await document.documentElement.requestFullscreen(); }
        catch { setFullscreenNeeded(true); }
      }
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

  // v3.3 : ordre mélangé, stable pour une tentative donnée, différent d'une tentative à l'autre
  const displayQuestions = useMemo(() => {
    if (!quiz || !tentativeId) return quiz?.questions || [];
    const seed = seedFromString(`tentative-${tentativeId}`);
    return seededShuffle(quiz.questions, seed).map((q, idx) => ({
      ...q,
      choix: q.choix ? seededShuffle(q.choix, seed + idx * 7919 + 1) : q.choix,
    }));
  }, [quiz, tentativeId]);

  const nbRepondues = quiz
    ? quiz.questions.filter(q => {
        const r = reponses[q.id];
        if (!r) return false;
        return q.type === "REPONSE_COURTE" ? r.reponseTexte?.trim().length > 0 : r.choixIds?.size > 0;
      }).length : 0;

  const handleSubmit = useCallback(async (force = false) => {
    if (submittedRef.current || !tentativeId) return;
    const restantes = quiz.questions.length - nbRepondues;
    if (restantes > 0 && !force) {
      if (!window.confirm(`${restantes} question${restantes > 1 ? "s" : ""} sans réponse. Soumettre quand même ?`)) return;
    }
    submittedRef.current = true; setSub(true); cancelCountdown();
    try {
      const result = await quizApi.submit(tentativeId);
      if (document.fullscreenElement) document.exitFullscreen().catch(() => {});
      navigate(`/quiz/${id}/resultat`, { state: { result } });
    } catch (e) {
      setError(e.message || "Impossible de soumettre.");
      submittedRef.current = false; setSub(false);
    }
  }, [tentativeId, id, navigate, nbRepondues, quiz]);

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;
  if (!quiz)   return <div className="page">{error || "Devoir introuvable."}</div>;
  if (quiz.estCloture) return (
    <div className="page"><div className="card" style={{fontWeight:600,marginBottom:6}}><p style={{fontWeight:600,marginBottom:6}}>Devoir fermé</p><p style={{color:"var(--ink-3)"}}>Consultez vos résultats depuis la page Notes.</p></div></div>
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
          {quiz.pleinEcranObligatoire && (
            <div className="alert alert-info" style={{ marginBottom:16 }}>
              <i className="ti ti-maximize" aria-hidden="true" />
              Ce devoir doit être passé en plein écran. Le comportement pendant l'épreuve est tracé et visible par l'enseignant.
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

  // ── Bandeau anti-triche : ton qui s'intensifie avec le score ───────────
  const seuil = quiz.seuilAlerteScore || 15;
  let bandeauTone = null;
  if (score > 0) bandeauTone = score >= seuil ? "alert-error" : score >= seuil * 0.5 ? "alert-warning" : "alert-info";

  return (
    <div className="page">
      {fullscreenNeeded && (
        <div style={{ position:"fixed", inset:0, background:"rgba(10,10,10,0.94)", zIndex:9999,
                      display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center",
                      color:"#fff", textAlign:"center", padding:24 }}>
          <i className="ti ti-maximize" style={{ fontSize:40, marginBottom:16 }} aria-hidden="true" />
          <p style={{ fontWeight:700, fontSize:18, marginBottom:8 }}>Mode plein écran requis</p>
          <p style={{ opacity:0.8, marginBottom:20, maxWidth:420 }}>
            Cet examen doit être passé en plein écran. En sortir est enregistré comme événement suspect.
          </p>
          <button className="btn btn-green btn-lg btn-round" onClick={enterFullscreen}>
            <i className="ti ti-player-play" aria-hidden="true" /> Revenir en plein écran
          </button>
        </div>
      )}

      {countdown != null && (
        <div style={{ position:"fixed", inset:0, background:"rgba(120,20,20,0.94)", zIndex:9998,
                      display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center",
                      color:"#fff", textAlign:"center", padding:24 }}>
          <i className="ti ti-alert-triangle" style={{ fontSize:40, marginBottom:16 }} aria-hidden="true" />
          <p style={{ fontWeight:700, fontSize:20, marginBottom:8 }}>Comportement suspect répété</p>
          <p style={{ opacity:0.9, marginBottom:4 }}>Soumission automatique dans</p>
          <p style={{ fontSize:48, fontWeight:800, marginBottom:20 }}>{countdown}s</p>
          <button className="btn btn-round" style={{ background:"#fff", color:"#111" }} onClick={cancelCountdown}>
            Je suis toujours là — continuer le devoir
          </button>
        </div>
      )}

      <div className="quiz-header">
        <div>
          <h2 style={{ fontWeight:700, fontSize:17 }}>{quiz.titre}</h2>
          <p style={{ color:"var(--ink-3)", fontSize:12 }}>{nbRepondues}/{quiz.questions.length} répondues</p>
        </div>
        <Timer dateDebut={dateDebut} dureeMinutes={quiz.dureeMinutes} onExpire={() => handleSubmit(true)} />
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom:16 }}><i className="ti ti-alert-circle" aria-hidden="true" />{error}</div>}

      {bandeauTone && (
        <div className={`alert ${bandeauTone}`} style={{ marginBottom:16 }}>
          <i className="ti ti-eye" aria-hidden="true" />
          Comportement suspect détecté ({nbEvents}) — ces événements sont enregistrés et visibles par l'enseignant.
        </div>
      )}

      <div className="list" style={{ marginBottom:20 }}>
        {displayQuestions.map((question, index) => {
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

      <button className="btn btn-green btn-xl btn-block btn-round" onClick={() => handleSubmit(false)} disabled={submitting}>
        {submitting ? "Envoi en cours…" : <><i className="ti ti-send" aria-hidden="true" /> Soumettre le devoir</>}
      </button>
    </div>
  );
}
