import { useLocation, Link } from "react-router-dom";
import Chip from "../../components/ui/Chip";

export default function QuizResult() {
  const { state } = useLocation();
  const result = state?.result;

  if (!result) return (
    <div className="page">
      <div className="card" style={{ textAlign:"center" }}>
        <i className="ti ti-file-unknown" style={{ fontSize:40, color:"var(--ink-4)" }} aria-hidden="true" />
        <p style={{ marginTop:12, fontWeight:600 }}>Résultat non disponible</p>
        <p style={{ color:"var(--ink-3)", fontSize:13, margin:"6px 0 16px" }}>Consultez vos résultats depuis la page Notes.</p>
        <Link to="/notes"><button className="btn btn-dark btn-round">Voir mes notes</button></Link>
      </div>
    </div>
  );

  const visible = result.noteSurBareme != null;
  const bareme  = result.bareme != null ? Number(result.bareme) : 20;
  const equiv20 = visible && bareme !== 20;

  return (
    <div className="page">
      <h2 style={{ fontWeight:700, fontSize:18, marginBottom:4 }}>{result.quizTitre}</h2>
      <p style={{ color:"var(--ink-3)", fontSize:13, marginBottom:20 }}>{result.matiere}</p>

      <div className="card" style={{ marginBottom:20 }}>
        <div className="result-hero">
          {visible ? (
            <>
              <p style={{ fontSize:12, color:"var(--ink-3)", marginBottom:6 }}>Note obtenue</p>
              <p className="result-score">
                {Number(result.noteSurBareme).toFixed(2)}
                <span>/{bareme}</span>
              </p>
              {equiv20 && <p className="result-sub">soit {Number(result.noteSur20).toFixed(2)}/20</p>}
            </>
          ) : (
            <>
              <i className="ti ti-clock" style={{ fontSize:36, color:"var(--ink-4)" }} aria-hidden="true" />
              <p style={{ fontWeight:600, marginTop:10, marginBottom:4 }}>Devoir soumis avec succès</p>
              <p className="result-sub">Votre note sera visible dès que l'enseignant aura fermé le devoir.</p>
            </>
          )}
        </div>
      </div>

      {/* v3.3 : anti-triche — transparence totale envers l'étudiant, dans les deux sens */}
      {!result.nbEvenementsSuspects ? (
        <div className="alert alert-success" style={{ marginBottom:20 }}>
          <i className="ti ti-shield-check" aria-hidden="true" />
          Session propre — aucun comportement suspect détecté pendant ce devoir.
        </div>
      ) : (
        <div className="alert alert-warning" style={{ marginBottom:20 }}>
          <i className="ti ti-eye" aria-hidden="true" />
          {result.nbEvenementsSuspects} événement{result.nbEvenementsSuspects > 1 ? "s" : ""} suspect{result.nbEvenementsSuspects > 1 ? "s" : ""} enregistré{result.nbEvenementsSuspects > 1 ? "s" : ""} pendant ce devoir, visible{result.nbEvenementsSuspects > 1 ? "s" : ""} par l'enseignant.
        </div>
      )}

      {visible && result.reponses && (
        <div>
          <div className="section-lbl">Détail des réponses</div>
          <div className="list">
            {result.reponses.map((q, i) => (
              <div className="card" key={q.id} style={{ marginBottom:10 }}>
                <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:8 }}>
                  <p className="q-num">Question {i + 1}</p>
                  {q.correctionManuelle
                    ? <Chip tone="grey">Correction manuelle</Chip>
                    : <Chip tone={q.estCorrecte ? "green" : "red"}>{q.estCorrecte ? "✓ Correct" : "✗ Incorrect"}</Chip>
                  }
                </div>
                <p className="q-text">{q.enonce}</p>
                {q.type === "REPONSE_COURTE"
                  ? <div className="choice" style={{ background:"var(--page)" }}>{q.reponseTexteEtudiant || <span style={{color:"var(--ink-4)"}}>Pas de réponse</span>}</div>
                  : q.choix.map(c => (
                    <div key={c.id} className={`choice ${c.estCorrect ? "correct" : ""}`}>
                      <span>{c.texte}</span>
                      {c.estCorrect && <span style={{ marginLeft:"auto", color:"var(--ok-txt)", fontSize:12, fontWeight:700 }}>✓</span>}
                    </div>
                  ))
                }
                {q.explication && (
                  <div className="alert alert-info" style={{ marginTop:10, marginBottom:0 }}>
                    <i className="ti ti-bulb" aria-hidden="true" />
                    {q.explication}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div style={{ marginTop:20 }}>
        <Link to="/"><button className="btn btn-dark btn-round"><i className="ti ti-home" aria-hidden="true" /> Retour à l'accueil</button></Link>
      </div>
    </div>
  );
}
