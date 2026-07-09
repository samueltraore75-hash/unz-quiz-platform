import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { teacherApi } from "../../api/teacher";
import Field from "../../components/ui/Field";
import Select from "../../components/ui/Select";
import Button from "../../components/ui/Button";
import PageHeader from "../../components/ui/PageHeader";

const SCALES = [20, 40, 50];

export default function QuizCreate() {
  const navigate = useNavigate();
  const [matieres, setMatieres] = useState([]);
  const [classes, setClasses] = useState([]);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [customScale, setCustomScale] = useState(false);
  const [form, setForm] = useState({
    titre: "",
    matiereId: "",
    classeId: "",
    typeQuiz: "EXAMEN",
    noteSur: 20,
    dureeMinutes: 30,
    tentativesMax: 1,
    delaiEntreTentativesMinutes: 0,
  });

  useEffect(() => {
    Promise.all([teacherApi.myMatieres(), teacherApi.classes()]).then(([m, c]) => {
      setMatieres(m);
      setClasses(c);
    });
  }, []);

  function update(key, value) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const quiz = await teacherApi.createQuiz({
        ...form,
        matiereId: Number(form.matiereId),
        classeId: Number(form.classeId),
        noteSur: Number(form.noteSur),
        dureeMinutes: Number(form.dureeMinutes),
        tentativesMax: Number(form.tentativesMax),
        delaiEntreTentativesMinutes: Number(form.delaiEntreTentativesMinutes),
      });
      navigate(`/quiz/${quiz.id}/gerer`);
    } catch (e2) {
      setError(e2.message || "Impossible de créer ce devoir.");
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <PageHeader
        eyebrow="Nouveau devoir"
        title="Créer un devoir de QCM"
        description="Choisissez le barème, la durée et la classe concernée. Vous ajouterez les questions à l'étape suivante."
      />

      <form className="card" onSubmit={handleSubmit}>
        <Field
          label="Titre du devoir"
          placeholder="Ex : Contrôle — Bases de données relationnelles"
          value={form.titre}
          onChange={(e) => update("titre", e.target.value)}
          required
        />

        <Select
          label="Matière"
          value={form.matiereId}
          onChange={(e) => update("matiereId", e.target.value)}
          options={matieres.map((m) => ({ value: m.id, label: m.nom }))}
          required
        />
        <Select
          label="Classe"
          value={form.classeId}
          onChange={(e) => update("classeId", e.target.value)}
          options={classes.map((c) => ({ value: c.id, label: `${c.nom} (${c.anneeLibelle})` }))}
          required
        />

        <div className="field">
          <label>Noté sur</label>
          <div className="scale-picker">
            {SCALES.map((s) => (
              <button
                type="button"
                key={s}
                className={`scale-option ${!customScale && Number(form.noteSur) === s ? "active" : ""}`}
                onClick={() => { setCustomScale(false); update("noteSur", s); }}
              >
                /{s}
              </button>
            ))}
            <button
              type="button"
              className={`scale-option ${customScale ? "active" : ""}`}
              onClick={() => setCustomScale(true)}
            >
              autre…
            </button>
          </div>
          {customScale && (
            <input
              type="number"
              min="1"
              step="0.5"
              placeholder="Barème personnalisé"
              value={form.noteSur}
              onChange={(e) => update("noteSur", e.target.value)}
              required
            />
          )}
        </div>

        <Select
          label="Type de devoir"
          value={form.typeQuiz}
          onChange={(e) => update("typeQuiz", e.target.value)}
          options={[
            { value: "EXAMEN", label: "Examen — tentative unique, note officielle" },
            { value: "ENTRAINEMENT", label: "Entraînement — tentatives multiples, sans note officielle" },
          ]}
          required
        />
        <Field
          label="Durée accordée aux étudiants (minutes)"
          type="number"
          min="1"
          value={form.dureeMinutes}
          onChange={(e) => update("dureeMinutes", e.target.value)}
          required
        />
        {form.typeQuiz === "ENTRAINEMENT" && (
          <>
            <Field
              label="Tentatives maximum"
              type="number"
              min="1"
              value={form.tentativesMax}
              onChange={(e) => update("tentativesMax", e.target.value)}
            />
            <Field
              label="Délai entre deux tentatives (minutes)"
              type="number"
              min="0"
              value={form.delaiEntreTentativesMinutes}
              onChange={(e) => update("delaiEntreTentativesMinutes", e.target.value)}
            />
          </>
        )}

        {error && <p className="login-error">{error}</p>}

        <Button type="submit" block disabled={submitting}>
          {submitting ? "Création…" : "Créer le devoir et ajouter les questions"}
        </Button>
      </form>
    </div>
  );
}
