import { useEffect, useState } from "react";
import { adminApi } from "../../api/admin";
import Select from "../../components/ui/Select";
import Button from "../../components/ui/Button";
import Chip from "../../components/ui/Chip";
import PageHeader from "../../components/ui/PageHeader";

export default function Bulletins() {
  const [etudiants, setEtudiants] = useState([]);
  const [niveaux, setNiveaux] = useState([]);
  const [annees, setAnnees] = useState([]);
  const [niveauId, setNiveauId] = useState("");
  const [anneeId, setAnneeId] = useState("");
  const [semestres, setSemestres] = useState([]);
  const [etudiantId, setEtudiantId] = useState("");
  const [semestreId, setSemestreId] = useState("");
  const [bulletins, setBulletins] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi.users("ETUDIANT").then(setEtudiants);
    adminApi.niveaux().then(setNiveaux);
    adminApi.annees().then(setAnnees);
  }, []);
  useEffect(() => { if (niveauId && anneeId) adminApi.semestres(niveauId, anneeId).then(setSemestres); }, [niveauId, anneeId]);
  useEffect(() => { if (etudiantId) adminApi.bulletinsEtudiant(etudiantId).then(setBulletins); }, [etudiantId]);

  async function generer(publier) {
    setError("");
    try {
      await adminApi.genererBulletin({ etudiantId: Number(etudiantId), semestreId: Number(semestreId), publier });
      adminApi.bulletinsEtudiant(etudiantId).then(setBulletins);
    } catch (e) { setError(e.message); }
  }

  async function publier(id) {
    try {
      await adminApi.publierBulletin(id);
      adminApi.bulletinsEtudiant(etudiantId).then(setBulletins);
    } catch (e) { setError(e.message); }
  }

  return (
    <div className="page">
      <PageHeader eyebrow="Administration" title="Bulletins" description="Génère la moyenne semestrielle d'un étudiant et publie le bulletin lorsqu'il est prêt." />

      <div className="card" style={{ marginBottom: 24 }}>
        <Select label="Étudiant" value={etudiantId} onChange={(e) => setEtudiantId(e.target.value)}
          options={etudiants.map((u) => ({ value: u.id, label: `${u.firstName || ""} ${u.lastName || u.username}`.trim() }))} required />
        <Select label="Niveau" value={niveauId} onChange={(e) => setNiveauId(e.target.value)}
          options={niveaux.map((n) => ({ value: n.id, label: n.libelle }))} required />
        <Select label="Année académique" value={anneeId} onChange={(e) => setAnneeId(e.target.value)}
          options={annees.map((a) => ({ value: a.id, label: a.libelle }))} required />
        <Select label="Semestre" value={semestreId} onChange={(e) => setSemestreId(e.target.value)}
          options={semestres.map((s) => ({ value: s.id, label: `S${s.numero}` }))} required />

        {error && <p className="login-error">{error}</p>}

        <div style={{ display: "flex", gap: 8 }}>
          <Button variant="secondary" onClick={() => generer(false)} disabled={!etudiantId || !semestreId}>
            Générer (brouillon)
          </Button>
          <Button onClick={() => generer(true)} disabled={!etudiantId || !semestreId}>
            Générer et publier
          </Button>
        </div>
      </div>

      {etudiantId && (
        <>
          <p className="section-title">Bulletins de cet étudiant</p>
          <div className="quiz-list">
            {bulletins.length === 0 && <p className="empty-hint">Aucun bulletin généré pour le moment.</p>}
            {bulletins.map((b) => (
              <div className="card quiz-row" key={b.id}>
                <div>
                  <p className="quiz-row-title">{b.semestreLabel}</p>
                  <p className="quiz-row-sub">
                    {b.moyenneGenerale != null ? Number(b.moyenneGenerale).toFixed(2) : "—"}/20 ·{" "}
                    {b.creditsAcquis}/{b.creditsTotal} crédits
                  </p>
                </div>
                {b.publie ? (
                  <Chip tone="success">publié</Chip>
                ) : (
                  <Button variant="secondary" style={{ fontSize: 12, padding: "6px 12px" }} onClick={() => publier(b.id)}>
                    publier
                  </Button>
                )}
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
