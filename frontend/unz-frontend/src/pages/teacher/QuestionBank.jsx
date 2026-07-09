import { useEffect, useState } from "react";
import { teacherApi } from "../../api/teacher";
import Field from "../../components/ui/Field";
import Select from "../../components/ui/Select";
import Button from "../../components/ui/Button";
import Chip from "../../components/ui/Chip";
import PageHeader from "../../components/ui/PageHeader";

const TYPES = [
  { value: "QCM_UNIQUE", label: "QCM — réponse unique" },
  { value: "QCM_MULTIPLE", label: "QCM — réponse multiple" },
  { value: "VRAI_FAUX", label: "Vrai / Faux" },
  { value: "REPONSE_COURTE", label: "Réponse courte (correction manuelle)" },
];

const CHOIX_VIDE = { texte: "", estCorrect: false };

function emptyForm(matiereId) {
  return { enonce: "", explication: "", type: "QCM_UNIQUE", difficulte: "MOYEN", points: 1, matiereId, choix: [{ ...CHOIX_VIDE }, { ...CHOIX_VIDE }] };
}

export default function QuestionBank() {
  const [matieres, setMatieres] = useState([]);
  const [matiereId, setMatiereId] = useState("");
  const [questions, setQuestions] = useState([]);
  const [form, setForm] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => { teacherApi.myMatieres().then(setMatieres); }, []);

  useEffect(() => {
    if (!matiereId) { setQuestions([]); return; }
    teacherApi.questions(matiereId).then(setQuestions);
    setForm(null);
    setEditingId(null);
  }, [matiereId]);

  function startCreate() {
    setForm(emptyForm(matiereId));
    setEditingId(null);
  }

  function startEdit(q) {
    setForm({
      enonce: q.enonce, explication: q.explication || "", type: q.type, difficulte: q.difficulte, points: q.points,
      matiereId, choix: q.choix.length ? q.choix.map((c) => ({ texte: c.texte, estCorrect: c.estCorrect })) : [{ ...CHOIX_VIDE }],
    });
    setEditingId(q.id);
  }

  function updateChoix(index, key, value) {
    setForm((f) => {
      const choix = [...f.choix];
      choix[index] = { ...choix[index], [key]: value };
      return { ...f, choix };
    });
  }

  function addChoixRow() {
    setForm((f) => ({ ...f, choix: [...f.choix, { ...CHOIX_VIDE }] }));
  }

  function removeChoixRow(index) {
    setForm((f) => ({ ...f, choix: f.choix.filter((_, i) => i !== index) }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    const besoinDeChoix = form.type !== "REPONSE_COURTE";
    const payload = { ...form, points: Number(form.points), choix: besoinDeChoix ? form.choix : [] };
    try {
      if (editingId) {
        await teacherApi.updateQuestion(editingId, payload);
      } else {
        await teacherApi.createQuestion(payload);
      }
      const fresh = await teacherApi.questions(matiereId);
      setQuestions(fresh);
      setForm(null);
      setEditingId(null);
    } catch (e2) {
      setError(e2.message || "Impossible d'enregistrer cette question.");
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Supprimer cette question ?")) return;
    try {
      await teacherApi.deleteQuestion(id);
      setQuestions((qs) => qs.filter((q) => q.id !== id));
    } catch (e) {
      setError(e.message || "Impossible de supprimer cette question.");
    }
  }

  return (
    <div className="page">
      <PageHeader eyebrow="Enseignement" title="Banque de questions" description="Créez et réutilisez vos questions entre plusieurs devoirs." />

      <Select
        label="Matière"
        value={matiereId}
        onChange={(e) => setMatiereId(e.target.value)}
        options={matieres.map((m) => ({ value: m.id, label: m.nom }))}
      />

      {matiereId && !form && (
        <Button variant="secondary" onClick={startCreate} style={{ marginBottom: 20 }}>
          Nouvelle question
        </Button>
      )}

      {error && <p className="login-error">{error}</p>}

      {form && (
        <form className="card" onSubmit={handleSubmit} style={{ marginBottom: 24 }}>
          <Field
            label="Énoncé"
            value={form.enonce}
            onChange={(e) => setForm({ ...form, enonce: e.target.value })}
            required
          />
          <Field
            label="Explication (facultatif — affichée à l'étudiant après correction)"
            value={form.explication}
            onChange={(e) => setForm({ ...form, explication: e.target.value })}
          />
          <Select
            label="Type"
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
            options={TYPES}
            required
          />
          <Field
            label="Points"
            type="number"
            step="0.5"
            min="0"
            value={form.points}
            onChange={(e) => setForm({ ...form, points: e.target.value })}
            required
          />

          {form.type !== "REPONSE_COURTE" && (
            <div className="field">
              <label>Choix de réponse</label>
              <div className="choix-list">
                {form.choix.map((c, i) => (
                  <div key={i} style={{ display: "flex", gap: 8, alignItems: "center" }}>
                    <input
                      type="checkbox"
                      checked={c.estCorrect}
                      onChange={(e) => updateChoix(i, "estCorrect", e.target.checked)}
                      title="Réponse correcte"
                    />
                    <input
                      type="text"
                      value={c.texte}
                      placeholder={`Choix ${i + 1}`}
                      onChange={(e) => updateChoix(i, "texte", e.target.value)}
                      style={{ flex: 1 }}
                      required
                    />
                    <button type="button" className="btn btn-secondary" style={{ padding: "6px 10px", fontSize: 12 }} onClick={() => removeChoixRow(i)}>
                      retirer
                    </button>
                  </div>
                ))}
              </div>
              <button type="button" className="btn btn-secondary" style={{ marginTop: 8, fontSize: 12, padding: "6px 12px" }} onClick={addChoixRow}>
                + ajouter un choix
              </button>
            </div>
          )}

          <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
            <Button type="submit">{editingId ? "Enregistrer" : "Créer la question"}</Button>
            <Button type="button" variant="secondary" onClick={() => { setForm(null); setEditingId(null); }}>
              Annuler
            </Button>
          </div>
        </form>
      )}

      <div className="question-list">
        {questions.map((q) => (
          <div className="card question-card" key={q.id}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 10 }}>
              <div>
                <p className="question-enonce" style={{ marginBottom: 6 }}>{q.enonce}</p>
                <Chip tone="neutral">{q.type.replace("_", " ").toLowerCase()} · {q.points} pt{q.points > 1 ? "s" : ""}</Chip>
              </div>
              <div style={{ display: "flex", gap: 6, flexShrink: 0 }}>
                <button className="btn btn-secondary" style={{ fontSize: 12, padding: "6px 10px" }} onClick={() => startEdit(q)}>éditer</button>
                <button className="btn btn-secondary" style={{ fontSize: 12, padding: "6px 10px" }} onClick={() => handleDelete(q.id)}>supprimer</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
