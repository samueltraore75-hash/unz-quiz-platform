import { useEffect, useState } from "react";
import { teacherApi } from "../../api/teacher";
import Select from "../../components/ui/Select";
import Field from "../../components/ui/Field";
import Button from "../../components/ui/Button";
import PageHeader from "../../components/ui/PageHeader";

const TYPES_NOTE = [
  { value: "EXAMEN_ECRIT", label: "Examen écrit" },
  { value: "TP", label: "Travaux pratiques" },
  { value: "CONTROLE_CONTINU", label: "Contrôle continu" },
];

export default function GradeEntry() {
  const [matieres, setMatieres] = useState([]);
  const [classes, setClasses] = useState([]);
  const [matiereId, setMatiereId] = useState("");
  const [classeId, setClasseId] = useState("");
  const [etudiants, setEtudiants] = useState([]);
  const [form, setForm] = useState({ etudiantId: "", typeNote: "CONTROLE_CONTINU", valeur: "", ponderation: 1 });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([teacherApi.myMatieres(), teacherApi.classes()]).then(([m, c]) => {
      setMatieres(m);
      setClasses(c);
    });
  }, []);

  useEffect(() => {
    if (!classeId) { setEtudiants([]); return; }
    setError("");
    teacherApi.etudiantsDeClasse(classeId)
      .then(setEtudiants)
      .catch((e) => { setEtudiants([]); setError(e.message || "Impossible de charger les étudiants de cette classe."); });
  }, [classeId]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(""); setMessage("");
    try {
      await teacherApi.saisirNote(matiereId, {
        etudiantId: Number(form.etudiantId),
        typeNote: form.typeNote,
        valeur: Number(form.valeur),
        ponderation: Number(form.ponderation),
      });
      setMessage("Note enregistrée.");
      setForm({ ...form, valeur: "" });
    } catch (e2) {
      setError(e2.message || "Impossible d'enregistrer cette note.");
    }
  }

  return (
    <div className="page">
      <PageHeader eyebrow="Enseignement" title="Saisir une note" description="Pour les évaluations réalisées hors plateforme (TP, contrôle continu, examen écrit)." />

      <form className="card" onSubmit={handleSubmit}>
        <Select
          label="Matière"
          value={matiereId}
          onChange={(e) => setMatiereId(e.target.value)}
          options={matieres.map((m) => ({ value: m.id, label: m.nom }))}
          required
        />
        <Select
          label="Classe"
          value={classeId}
          onChange={(e) => setClasseId(e.target.value)}
          options={classes.map((c) => ({ value: c.id, label: c.nom }))}
          required
        />
        <Select
          label="Étudiant"
          value={form.etudiantId}
          onChange={(e) => setForm({ ...form, etudiantId: e.target.value })}
          options={etudiants.map((u) => ({ value: u.id, label: `${u.firstName || ""} ${u.lastName || u.username}`.trim() }))}
          required
        />
        <Select
          label="Type de note"
          value={form.typeNote}
          onChange={(e) => setForm({ ...form, typeNote: e.target.value })}
          options={TYPES_NOTE}
          required
        />
        <Field
          label="Valeur (/20)"
          type="number" min="0" max="20" step="0.25"
          value={form.valeur}
          onChange={(e) => setForm({ ...form, valeur: e.target.value })}
          required
        />
        <Field
          label="Pondération"
          type="number" min="0.1" step="0.1"
          value={form.ponderation}
          onChange={(e) => setForm({ ...form, ponderation: e.target.value })}
          required
        />

        {error && <p className="login-error">{error}</p>}
        {message && <p className="quiz-row-sub" style={{ color: "var(--success-text)", marginBottom: 12 }}>{message}</p>}

        <Button type="submit" block disabled={!matiereId || !form.etudiantId}>
          Enregistrer la note
        </Button>
      </form>
    </div>
  );
}
