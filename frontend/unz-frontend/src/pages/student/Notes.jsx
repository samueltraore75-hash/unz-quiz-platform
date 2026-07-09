import { useEffect, useState } from "react";
import { gradesApi } from "../../api/grades";
import { tokenStore } from "../../api/client";
import Chip from "../../components/ui/Chip";
import VerticalBarChart from "../../components/ui/VerticalBarChart";

export default function Notes() {
  const [bulletins, setBulletins]     = useState([]);
  const [progression, setProgression] = useState([]);
  const [loading, setLoading]         = useState(true);

  useEffect(() => {
    Promise.all([gradesApi.bulletinsMine(), gradesApi.progression()])
      .then(([b, p]) => { setBulletins(b); setProgression(p); })
      .finally(() => setLoading(false));
  }, []);

  async function downloadBulletin(id) {
    try {
      const { accessToken } = tokenStore.getTokens();
      const res = await fetch(`/api/bulletins/${id}/pdf`, {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      if (!res.ok) throw new Error(`Erreur ${res.status}`);
      const blob = await res.blob();
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement("a");
      a.href     = url;
      a.download = `bulletin_${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      alert("Impossible de télécharger le bulletin : " + e.message);
    }
  }

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;

  return (
    <div className="page">
      {/* Bulletins */}
      <div className="section-lbl">Bulletins publiés</div>
      {bulletins.length === 0
        ? <p className="empty" style={{ marginBottom:24 }}>Aucun bulletin publié pour le moment.</p>
        : (
          <div className="list" style={{ marginBottom:28 }}>
            {bulletins.map(b => (
              <div className="row" key={b.id}>
                <div className="row-top">
                  <div>
                    <p className="row-title">{b.semestreLabel}</p>
                    <p className="row-sub">{b.creditsAcquis}/{b.creditsTotal} crédits · {b.semestreValide ? "semestre validé ✓" : "non validé"}</p>
                  </div>
                  <div style={{ display:"flex", alignItems:"center", gap:10 }}>
                    <span className="mono" style={{ fontSize:20, fontWeight:700, color:"var(--ink)" }}>
                      {b.moyenneGenerale != null ? Number(b.moyenneGenerale).toFixed(2) : "—"}
                    </span>
                  </div>
                </div>
                <div className="row-actions">
                  <button className="btn btn-ghost btn-sm btn-round" onClick={() => downloadBulletin(b.id)}>
                    <i className="ti ti-download" aria-hidden="true" /> Télécharger PDF
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

      {/* Progression par matière */}
      <div className="section-lbl">Progression par matière</div>
      {progression.length === 0
        ? <p className="empty">Aucune tentative soumise pour le moment.</p>
        : (
          <div className="list">
            {progression.map(mat => {
              const notes = mat.historique.filter(h => h.noteSur20 != null).map(h => h.noteSur20);
              const moy   = notes.length ? (notes.reduce((a, b) => a + b, 0) / notes.length).toFixed(2) : null;
              return (
                <div className="card" key={mat.matiere}>
                  <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:12 }}>
                    <p style={{ fontWeight:700, fontSize:14 }}>{mat.matiere}</p>
                    {moy && <span className="mono" style={{ fontWeight:700, color:"var(--green)" }}>{moy}/20 moy.</span>}
                  </div>
                  <VerticalBarChart
                    max={20}
                    unit="/20"
                    rows={mat.historique
                      .filter(h => h.noteSur20 != null)
                      .map(h => ({
                        label: h.quizTitre,
                        value: Number(h.noteSur20).toFixed(1),
                        tone: h.noteSur20 >= 10 ? "green" : "red",
                      }))}
                  />
                  <div style={{ display:"flex", flexDirection:"column", gap:8, marginTop:14 }}>
                    {mat.historique.map((item, i) => (
                      <div key={i} style={{ display:"flex", justifyContent:"space-between", alignItems:"center", padding:"7px 0", borderBottom:i < mat.historique.length - 1 ? "1px solid var(--line)" : "none" }}>
                        <div>
                          <p style={{ fontSize:13, fontWeight:500 }}>{item.quizTitre}</p>
                          <p style={{ fontSize:11, color:"var(--ink-3)" }}>{item.typeQuiz === "EXAMEN" ? "Examen" : "Entraînement"} · {new Date(item.date).toLocaleDateString("fr-FR")}</p>
                        </div>
                        {item.noteSur20 != null
                          ? <Chip tone={item.noteSur20 >= 10 ? "green" : "red"} mono>{Number(item.noteSur20).toFixed(2)}/20</Chip>
                          : <Chip tone="grey">En attente</Chip>}
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        )}
    </div>
  );
}
