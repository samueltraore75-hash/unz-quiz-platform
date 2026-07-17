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

const DIFFICULTES = [
  { value: "FACILE", label: "Facile" },
  { value: "MOYEN", label: "Moyen" },
  { value: "DIFFICILE", label: "Difficile" },
];

const CHOIX_VIDE = { texte: "", estCorrect: false };

function emptyForm(matiereId) {
  return { enonce: "", explication: "", type: "QCM_UNIQUE", difficulte: "MOYEN", points: 1, matiereId, tags: "", choix: [{ ...CHOIX_VIDE }, { ...CHOIX_VIDE }] };
}

/** "algo, boucles ,Algo" -> ["algo", "boucles"] (dédoublonné, insensible à la casse) */
function parseTagsInput(texte) {
  const seen = new Set();
  const result = [];
  for (const brut of texte.split(",")) {
    const t = brut.trim();
    if (!t || seen.has(t.toLowerCase())) continue;
    seen.add(t.toLowerCase());
    result.push(t);
  }
  return result;
}

export default function QuestionBank() {
  const [matieres, setMatieres] = useState([]);
  const [matiereId, setMatiereId] = useState("");
  const [questions, setQuestions] = useState([]);
  const [tagsDisponibles, setTagsDisponibles] = useState([]);
  const [difficulteFiltre, setDifficulteFiltre] = useState("");
  const [tagFiltre, setTagFiltre] = useState("");
  const [rechercheFiltre, setRechercheFiltre] = useState("");
  const [form, setForm] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => { teacherApi.myMatieres().then(setMatieres); }, []);

  function rechargerQuestions() {
    if (!matiereId) { setQuestions([]); return; }
    teacherApi.questions(matiereId, {
      difficulte: difficulteFiltre || undefined,
      tag: tagFiltre || undefined,
      recherche: rechercheFiltre || undefined,
    }).then(setQuestions);
  }

  useEffect(() => {
    if (!matiereId) { setQuestions([]); setTagsDisponibles([]); return; }
    teacherApi.questionTags(matiereId).then(setTagsDisponibles);
    setDifficulteFiltre("");
    setTagFiltre("");
    setRechercheFiltre("");
    setForm(null);
    setEditingId(null);
  }, [matiereId]);

  // Recherche par sous-chaîne : léger debounce pour ne pas requêter à chaque frappe
  useEffect(() => {
    const t = setTimeout(rechargerQuestions, rechercheFiltre ? 300 : 0);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [matiereId, difficulteFiltre, tagFiltre, rechercheFiltre]);

  function startCreate() {
    setForm(emptyForm(matiereId));
    setEditingId(null);
  }

  function startEdit(q) {
    setForm({
      enonce: q.enonce, explication: q.explication || "", type: q.type, difficulte: q.difficulte, points: q.points,
      matiereId, tags: (q.tags || []).join(", "),
      choix: q.choix.length ? q.choix.map((c) => ({ texte: c.texte, estCorrect: c.estCorrect })) : [{ ...CHOIX_VIDE }],
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
    const payload = {
      ...form,
      points: Number(form.points),
      choix: besoinDeChoix ? form.choix : [],
      tags: parseTagsInput(form.tags || ""),
    };
    try {
      if (editingId) {
        await teacherApi.updateQuestion(editingId, payload);
      } else {
        await teacherApi.createQuestion(payload);
      }
      rechargerQuestions();
      teacherApi.questionTags(matiereId).then(setTagsDisponibles);
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
      teacherApi.questionTags(matiereId).then(setTagsDisponibles);
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

      {matiereId && (
        <div className="toolbar" style={{ marginBottom: 16, flexWrap: "wrap" }}>
          <input
            type="text"
            placeholder="Rechercher dans l'énoncé…"
            value={rechercheFiltre}
            onChange={(e) => setRechercheFiltre(e.target.value)}
            className="field-input"
            style={{ flex: "1 1 220px" }}
          />
          <Select
            value={difficulteFiltre}
            onChange={(e) => setDifficulteFiltre(e.target.value)}
            placeholder="Toutes difficultés"
            options={DIFFICULTES}
          />
          <Select
            value={tagFiltre}
            onChange={(e) => setTagFiltre(e.target.value)}
            placeholder="Tous les tags"
            options={tagsDisponibles.map((t) => ({ value: t, label: t }))}
          />
        </div>
      )}

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
          <Select
            label="Difficulté"
            value={form.difficulte}
            onChange={(e) => setForm({ ...form, difficulte: e.target.value })}
            options={DIFFICULTES}
            required
          />
          <Field
            label="Tags (séparés par des virgules — ex : boucles, chapitre 3)"
            value={form.tags}
            onChange={(e) => setForm({ ...form, tags: e.target.value })}
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
                <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
                  <Chip tone="neutral">{q.type.replace("_", " ").toLowerCase()} · {q.points} pt{q.points > 1 ? "s" : ""}</Chip>
                  <Chip tone={q.difficulte === "DIFFICILE" ? "warn" : q.difficulte === "FACILE" ? "green" : "grey"}>
                    {q.difficulte.toLowerCase()}
                  </Chip>
                  {(q.tags || []).map((t) => <Chip key={t} tone="blue">#{t}</Chip>)}
                </div>
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
