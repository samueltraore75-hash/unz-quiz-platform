import { useEffect, useState } from "react";
import { adminApi } from "../../api/admin";
import Field from "../../components/ui/Field";
import Select from "../../components/ui/Select";
import Button from "../../components/ui/Button";
import Chip from "../../components/ui/Chip";
import PageHeader from "../../components/ui/PageHeader";

const TABS = ["Filières", "Niveaux", "Années", "Classes", "Semestres", "UE", "Matières", "Inscriptions"];

export default function Referential() {
  const [tab, setTab] = useState(TABS[0]);
  return (
    <div className="page">
      <PageHeader
        eyebrow="Administration"
        title="Référentiel académique"
        description="Filières, niveaux, années, classes, semestres, UE, matières et inscriptions — dans l'ordre de dépendance."
      />
      <div className="segmented" style={{ marginBottom: 24 }}>
        {TABS.map((t) => (
          <button key={t} className={tab === t ? "active" : ""} onClick={() => setTab(t)}>
            {t}
          </button>
        ))}
      </div>
      {tab === "Filières" && <FilieresSection />}
      {tab === "Niveaux" && <NiveauxSection />}
      {tab === "Années" && <AnneesSection />}
      {tab === "Classes" && <ClassesSection />}
      {tab === "Semestres" && <SemestresSection />}
      {tab === "UE" && <UesSection />}
      {tab === "Matières" && <MatieresSection />}
      {tab === "Inscriptions" && <InscriptionsSection />}
    </div>
  );
}

function Row({ title, subtitle, right }) {
  return (
    <div className="card quiz-row">
      <div>
        <p className="quiz-row-title">{title}</p>
        {subtitle && <p className="quiz-row-sub">{subtitle}</p>}
      </div>
      {right}
    </div>
  );
}

function FilieresSection() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState({ nom: "", code: "" });
  const [error, setError] = useState("");

  function load() { adminApi.filieres().then(setItems); }
  useEffect(load, []);

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await adminApi.createFiliere(form);
      setForm({ nom: "", code: "" });
      load();
    } catch (e2) { setError(e2.message); }
  }

  return (
    <div>
      <form className="card" onSubmit={submit} style={{ marginBottom: 20 }}>
        <Field label="Nom" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} required />
        <Field label="Code" value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} required />
        {error && <p className="login-error">{error}</p>}
        <Button type="submit">Créer</Button>
      </form>
      <div className="quiz-list">
        {items.map((f) => <Row key={f.id} title={f.nom} subtitle={f.code} />)}
      </div>
    </div>
  );
}

function NiveauxSection() {
  const [filieres, setFilieres] = useState([]);
  const [filiereId, setFiliereId] = useState("");
  const [items, setItems] = useState([]);
  const [libelle, setLibelle] = useState("");
  const [error, setError] = useState("");

  useEffect(() => { adminApi.filieres().then(setFilieres); }, []);
  useEffect(() => { if (filiereId) adminApi.niveaux(filiereId).then(setItems); else setItems([]); }, [filiereId]);

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await adminApi.createNiveau({ libelle, filiereId: Number(filiereId) });
      setLibelle("");
      adminApi.niveaux(filiereId).then(setItems);
    } catch (e2) { setError(e2.message); }
  }

  return (
    <div>
      <Select label="Filière" value={filiereId} onChange={(e) => setFiliereId(e.target.value)}
        options={filieres.map((f) => ({ value: f.id, label: f.nom }))} />
      {filiereId && (
        <form className="card" onSubmit={submit} style={{ margin: "16px 0 20px" }}>
          <Field label="Libellé (ex : L3)" value={libelle} onChange={(e) => setLibelle(e.target.value)} required />
          {error && <p className="login-error">{error}</p>}
          <Button type="submit">Créer</Button>
        </form>
      )}
      <div className="quiz-list">
        {items.map((n) => <Row key={n.id} title={n.libelle} subtitle={n.filiereNom} />)}
      </div>
    </div>
  );
}

function AnneesSection() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState({ libelle: "", dateDebut: "", dateFin: "" });
  const [error, setError] = useState("");

  function load() { adminApi.annees().then(setItems); }
  useEffect(load, []);

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await adminApi.createAnnee(form);
      setForm({ libelle: "", dateDebut: "", dateFin: "" });
      load();
    } catch (e2) { setError(e2.message); }
  }

  async function activer(id) {
    await adminApi.activerAnnee(id);
    load();
  }

  return (
    <div>
      <form className="card" onSubmit={submit} style={{ marginBottom: 20 }}>
        <Field label="Libellé (ex : 2025-2026)" value={form.libelle} onChange={(e) => setForm({ ...form, libelle: e.target.value })} required />
        <Field label="Date de début" type="date" value={form.dateDebut} onChange={(e) => setForm({ ...form, dateDebut: e.target.value })} required />
        <Field label="Date de fin" type="date" value={form.dateFin} onChange={(e) => setForm({ ...form, dateFin: e.target.value })} required />
        {error && <p className="login-error">{error}</p>}
        <Button type="submit">Créer</Button>
      </form>
      <div className="quiz-list">
        {items.map((a) => (
          <Row key={a.id} title={a.libelle} subtitle={`${a.dateDebut} → ${a.dateFin}`}
            right={a.active
              ? <Chip tone="success">active</Chip>
              : <Button variant="secondary" style={{ fontSize: 12, padding: "6px 10px" }} onClick={() => activer(a.id)}>activer</Button>} />
        ))}
      </div>
    </div>
  );
}

function ClassesSection() {
  const [niveaux, setNiveaux] = useState([]);
  const [annees, setAnnees] = useState([]);
  const [items, setItems] = useState([]);
  const [form, setForm] = useState({ nom: "", niveauId: "", anneeAcademiqueId: "" });
  const [error, setError] = useState("");

  function load() { adminApi.classes().then(setItems); }
  useEffect(() => {
    load();
    adminApi.niveaux().then(setNiveaux);
    adminApi.annees().then(setAnnees);
  }, []);

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await adminApi.createClasse({ ...form, niveauId: Number(form.niveauId), anneeAcademiqueId: Number(form.anneeAcademiqueId) });
      setForm({ nom: "", niveauId: "", anneeAcademiqueId: "" });
      load();
    } catch (e2) { setError(e2.message); }
  }

  return (
    <div>
      <form className="card" onSubmit={submit} style={{ marginBottom: 20 }}>
        <Field label="Nom (ex : L3-INFO-A)" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} required />
        <Select label="Niveau" value={form.niveauId} onChange={(e) => setForm({ ...form, niveauId: e.target.value })}
          options={niveaux.map((n) => ({ value: n.id, label: `${n.libelle} — ${n.filiereNom}` }))} required />
        <Select label="Année académique" value={form.anneeAcademiqueId} onChange={(e) => setForm({ ...form, anneeAcademiqueId: e.target.value })}
          options={annees.map((a) => ({ value: a.id, label: a.libelle }))} required />
        {error && <p className="login-error">{error}</p>}
        <Button type="submit">Créer</Button>
      </form>
      <div className="quiz-list">
        {items.map((c) => <Row key={c.id} title={c.nom} subtitle={`${c.niveauLibelle} · ${c.anneeLibelle}`} />)}
      </div>
    </div>
  );
}

function SemestresSection() {
  const [niveaux, setNiveaux] = useState([]);
  const [annees, setAnnees] = useState([]);
  const [niveauId, setNiveauId] = useState("");
  const [anneeId, setAnneeId] = useState("");
  const [items, setItems] = useState([]);
  const [numero, setNumero] = useState(1);
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi.niveaux().then(setNiveaux);
    adminApi.annees().then(setAnnees);
  }, []);

  function load() {
    if (niveauId && anneeId) adminApi.semestres(niveauId, anneeId).then(setItems);
  }
  useEffect(load, [niveauId, anneeId]);

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await adminApi.createSemestre({ numero: Number(numero), niveauId: Number(niveauId), anneeAcademiqueId: Number(anneeId) });
      load();
    } catch (e2) { setError(e2.message); }
  }

  return (
    <div>
      <Select label="Niveau" value={niveauId} onChange={(e) => setNiveauId(e.target.value)}
        options={niveaux.map((n) => ({ value: n.id, label: n.libelle }))} />
      <Select label="Année académique" value={anneeId} onChange={(e) => setAnneeId(e.target.value)}
        options={annees.map((a) => ({ value: a.id, label: a.libelle }))} />
      {niveauId && anneeId && (
        <form className="card" onSubmit={submit} style={{ margin: "16px 0 20px" }}>
          <Select label="Numéro" value={numero} onChange={(e) => setNumero(e.target.value)}
            options={[{ value: 1, label: "Semestre 1" }, { value: 2, label: "Semestre 2" }]} required />
          {error && <p className="login-error">{error}</p>}
          <Button type="submit">Créer</Button>
        </form>
      )}
      <div className="quiz-list">
        {items.map((s) => <Row key={s.id} title={`S${s.numero}`} subtitle={`${s.niveauLibelle} · ${s.anneeLibelle}`} />)}
      </div>
    </div>
  );
}

function UesSection() {
  const [niveaux, setNiveaux] = useState([]);
  const [annees, setAnnees] = useState([]);
  const [niveauId, setNiveauId] = useState("");
  const [anneeId, setAnneeId] = useState("");
  const [semestres, setSemestres] = useState([]);
  const [semestreId, setSemestreId] = useState("");
  const [items, setItems] = useState([]);
  const [form, setForm] = useState({ nom: "", credits: 3, seuilValidation: 10 });
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi.niveaux().then(setNiveaux);
    adminApi.annees().then(setAnnees);
  }, []);
  useEffect(() => {
    if (niveauId && anneeId) adminApi.semestres(niveauId, anneeId).then(setSemestres);
  }, [niveauId, anneeId]);
  useEffect(() => {
    if (semestreId) adminApi.ues(semestreId).then(setItems);
  }, [semestreId]);

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await adminApi.createUe({ ...form, credits: Number(form.credits), seuilValidation: Number(form.seuilValidation), semestreId: Number(semestreId) });
      setForm({ nom: "", credits: 3, seuilValidation: 10 });
      adminApi.ues(semestreId).then(setItems);
    } catch (e2) { setError(e2.message); }
  }

  return (
    <div>
      <Select label="Niveau" value={niveauId} onChange={(e) => setNiveauId(e.target.value)}
        options={niveaux.map((n) => ({ value: n.id, label: n.libelle }))} />
      <Select label="Année" value={anneeId} onChange={(e) => setAnneeId(e.target.value)}
        options={annees.map((a) => ({ value: a.id, label: a.libelle }))} />
      <Select label="Semestre" value={semestreId} onChange={(e) => setSemestreId(e.target.value)}
        options={semestres.map((s) => ({ value: s.id, label: `S${s.numero}` }))} />
      {semestreId && (
        <form className="card" onSubmit={submit} style={{ margin: "16px 0 20px" }}>
          <Field label="Nom de l'UE" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} required />
          <Field label="Crédits" type="number" min="1" value={form.credits} onChange={(e) => setForm({ ...form, credits: e.target.value })} required />
          <Field label="Seuil de validation (/20)" type="number" min="0" max="20" value={form.seuilValidation} onChange={(e) => setForm({ ...form, seuilValidation: e.target.value })} required />
          {error && <p className="login-error">{error}</p>}
          <Button type="submit">Créer</Button>
        </form>
      )}
      <div className="quiz-list">
        {items.map((u) => <Row key={u.id} title={u.nom} subtitle={`${u.credits} crédits · seuil ${u.seuilValidation}/20`} />)}
      </div>
    </div>
  );
}

function MatieresSection() {
  const [niveaux, setNiveaux] = useState([]);
  const [annees, setAnnees] = useState([]);
  const [niveauId, setNiveauId] = useState("");
  const [anneeId, setAnneeId] = useState("");
  const [semestres, setSemestres] = useState([]);
  const [semestreId, setSemestreId] = useState("");
  const [ues, setUes] = useState([]);
  const [ueId, setUeId] = useState("");
  const [items, setItems] = useState([]);
  const [enseignants, setEnseignants] = useState([]);
  const [form, setForm] = useState({ nom: "", coefficient: 1 });
  const [affectForm, setAffectForm] = useState({});
  const [error, setError] = useState("");

  useEffect(() => {
    adminApi.niveaux().then(setNiveaux);
    adminApi.annees().then(setAnnees);
    adminApi.users("ENSEIGNANT").then(setEnseignants);
  }, []);
  useEffect(() => { if (niveauId && anneeId) adminApi.semestres(niveauId, anneeId).then(setSemestres); }, [niveauId, anneeId]);
  useEffect(() => { if (semestreId) adminApi.ues(semestreId).then(setUes); }, [semestreId]);
  function load() { if (ueId) adminApi.matieres(ueId).then(setItems); }
  useEffect(load, [ueId]);

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      await adminApi.createMatiere({ ...form, coefficient: Number(form.coefficient), ueId: Number(ueId) });
      setForm({ nom: "", coefficient: 1 });
      load();
    } catch (e2) { setError(e2.message); }
  }

  async function affecter(matiereId) {
    const enseignantId = affectForm[matiereId];
    const anneeAcademiqueId = anneeId;
    if (!enseignantId) return;
    try {
      await adminApi.affecterEnseignant(matiereId, { enseignantId: Number(enseignantId), anneeAcademiqueId: Number(anneeAcademiqueId) });
      load();
    } catch (e2) { setError(e2.message); }
  }

  return (
    <div>
      <Select label="Niveau" value={niveauId} onChange={(e) => setNiveauId(e.target.value)} options={niveaux.map((n) => ({ value: n.id, label: n.libelle }))} />
      <Select label="Année" value={anneeId} onChange={(e) => setAnneeId(e.target.value)} options={annees.map((a) => ({ value: a.id, label: a.libelle }))} />
      <Select label="Semestre" value={semestreId} onChange={(e) => setSemestreId(e.target.value)} options={semestres.map((s) => ({ value: s.id, label: `S${s.numero}` }))} />
      <Select label="UE" value={ueId} onChange={(e) => setUeId(e.target.value)} options={ues.map((u) => ({ value: u.id, label: u.nom }))} />

      {ueId && (
        <form className="card" onSubmit={submit} style={{ margin: "16px 0 20px" }}>
          <Field label="Nom de la matière" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} required />
          <Field label="Coefficient" type="number" min="0.1" step="0.1" value={form.coefficient} onChange={(e) => setForm({ ...form, coefficient: e.target.value })} required />
          {error && <p className="login-error">{error}</p>}
          <Button type="submit">Créer</Button>
        </form>
      )}

      <div className="quiz-list">
        {items.map((m) => (
          <div className="card" key={m.id}>
            <p className="quiz-row-title">{m.nom}</p>
            <p className="quiz-row-sub" style={{ marginBottom: 10 }}>
              coefficient {m.coefficient} · {(m.enseignants || []).join(", ") || "aucun enseignant affecté"}
            </p>
            {anneeId && (
              <div style={{ display: "flex", gap: 8 }}>
                <select
                  style={{ flex: 1, border: "1px solid var(--line)", borderRadius: "var(--radius-sm)", padding: "8px 10px", fontSize: 13 }}
                  value={affectForm[m.id] || ""}
                  onChange={(e) => setAffectForm({ ...affectForm, [m.id]: e.target.value })}
                >
                  <option value="">Affecter un enseignant…</option>
                  {enseignants.map((ens) => (
                    <option key={ens.id} value={ens.id}>{ens.firstName} {ens.lastName}</option>
                  ))}
                </select>
                <Button variant="secondary" style={{ fontSize: 12, padding: "8px 12px" }} onClick={() => affecter(m.id)}>affecter</Button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

function InscriptionsSection() {
  const [etudiants, setEtudiants] = useState([]);
  const [classes, setClasses] = useState([]);
  const [annees, setAnnees] = useState([]);
  const [form, setForm] = useState({ etudiantId: "", classeId: "", anneeAcademiqueId: "" });
  const [historique, setHistorique] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    adminApi.users("ETUDIANT").then(setEtudiants);
    adminApi.classes().then(setClasses);
    adminApi.annees().then(setAnnees);
  }, []);

  async function submit(e) {
    e.preventDefault();
    setError(""); setMessage("");
    try {
      await adminApi.inscrire({
        etudiantId: Number(form.etudiantId),
        classeId: Number(form.classeId),
        anneeAcademiqueId: Number(form.anneeAcademiqueId),
      });
      setMessage("Inscription enregistrée.");
      if (form.etudiantId) adminApi.historiqueInscriptions(form.etudiantId).then(setHistorique);
    } catch (e2) { setError(e2.message); }
  }

  async function voirHistorique(etudiantId) {
    setForm({ ...form, etudiantId });
    const h = await adminApi.historiqueInscriptions(etudiantId);
    setHistorique(h);
  }

  return (
    <div>
      <p className="quiz-row-sub" style={{ marginBottom: 16 }}>
        Inscrit un étudiant existant dans une classe pour une année académique — utile pour le passage d'une année à l'autre (ex : L3 → L4).
      </p>
      <form className="card" onSubmit={submit} style={{ marginBottom: 20 }}>
        <Select label="Étudiant" value={form.etudiantId} onChange={(e) => { setForm({ ...form, etudiantId: e.target.value }); voirHistorique(e.target.value); }}
          options={etudiants.map((u) => ({ value: u.id, label: `${u.firstName || ""} ${u.lastName || u.username}`.trim() }))} required />
        <Select label="Classe" value={form.classeId} onChange={(e) => setForm({ ...form, classeId: e.target.value })}
          options={classes.map((c) => ({ value: c.id, label: `${c.nom} (${c.anneeLibelle})` }))} required />
        <Select label="Année académique" value={form.anneeAcademiqueId} onChange={(e) => setForm({ ...form, anneeAcademiqueId: e.target.value })}
          options={annees.map((a) => ({ value: a.id, label: a.libelle }))} required />
        {error && <p className="login-error">{error}</p>}
        {message && <p className="quiz-row-sub" style={{ color: "var(--success-text)", marginBottom: 12 }}>{message}</p>}
        <Button type="submit">Inscrire</Button>
      </form>

      {historique && (
        <>
          <p className="section-title">Historique de progression</p>
          <div className="quiz-list">
            {historique.length === 0 && <p className="empty-hint">Aucune inscription enregistrée.</p>}
            {historique.map((h, i) => (
              <Row key={i} title={h.classe} subtitle={h.annee} right={<Chip tone="neutral">{h.statut}</Chip>} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
