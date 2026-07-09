import { useEffect, useState } from "react";
import { adminApi } from "../../api/admin";
import Chip from "../../components/ui/Chip";

export default function Users() {
  const [tab, setTab]           = useState("attente");
  const [attente, setAttente]   = useState([]);
  const [users, setUsers]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");
  const [success, setSuccess]   = useState("");
  const [showCreate, setCreate] = useState(false);
  const [form, setForm]         = useState({ username:"", password:"", firstName:"", lastName:"", email:"", role:"ETUDIANT" });
  const [creating, setCreating] = useState(false);

  function notify(msg, isError = false) {
    if (isError) setError(msg); else setSuccess(msg);
    setTimeout(() => { setError(""); setSuccess(""); }, 4000);
  }

  async function load() {
    try {
      const [a, e, en] = await Promise.all([
        adminApi.enAttente(),
        adminApi.users("ETUDIANT"),
        adminApi.users("ENSEIGNANT"),
      ]);
      setAttente(a);
      setUsers([...e, ...en].sort((a, b) => b.id - a.id));
    } catch { notify("Impossible de charger les utilisateurs.", true); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  async function handleValider(id) {
    try {
      await adminApi.validerCompte(id);
      notify("Compte activé avec succès.");
      load();
    } catch (e) { notify(e.message || "Erreur lors de la validation.", true); }
  }

  async function handleRejeter(id) {
    if (!window.confirm("Rejeter cette demande ? L'utilisateur ne pourra pas se connecter.")) return;
    try {
      await adminApi.rejeterCompte(id);
      notify("Demande rejetée.");
      load();
    } catch (e) { notify(e.message || "Erreur lors du rejet.", true); }
  }

  async function handleReinitialiser(id) {
    try {
      const res = await adminApi.reinitialiserMdp(id);
      alert(`Mot de passe temporaire : ${res.motDePasseTemporaire}\nCommuniquez-le à l'utilisateur.`);
    } catch (e) { notify(e.message || "Erreur.", true); }
  }

  async function handleDesactiver(id) {
    if (!window.confirm("Désactiver ce compte ?")) return;
    try {
      await adminApi.desactiverUser(id);
      notify("Compte désactivé.");
      load();
    } catch (e) { notify(e.message || "Erreur.", true); }
  }

  async function handleCreer(e) {
    e.preventDefault();
    setCreating(true);
    try {
      await adminApi.createUser(form);
      notify("Compte créé avec succès.");
      setCreate(false);
      setForm({ username:"", password:"", firstName:"", lastName:"", email:"", role:"ETUDIANT" });
      load();
    } catch (err) { notify(err.message || "Erreur lors de la création.", true); }
    finally { setCreating(false); }
  }

  function set(k, v) { setForm(f => ({...f, [k]:v})); }

  if (loading) return <div className="page"><p className="empty">Chargement…</p></div>;

  return (
    <div className="page">
      {error   && <div className="alert alert-error"   style={{ marginBottom:16 }}><i className="ti ti-alert-circle" aria-hidden="true" /> {error}</div>}
      {success && <div className="alert alert-success" style={{ marginBottom:16 }}><i className="ti ti-check" aria-hidden="true" /> {success}</div>}

      {/* Tabs */}
      <div className="tabs" style={{ marginBottom:20 }}>
        <button className={`tab ${tab==="attente"?"active":""}`} onClick={() => setTab("attente")}>
          En attente {attente.length > 0 && <span style={{ background:"var(--danger)", color:"#fff", borderRadius:10, padding:"1px 7px", fontSize:10, marginLeft:6, fontWeight:700 }}>{attente.length}</span>}
        </button>
        <button className={`tab ${tab==="tous"?"active":""}`} onClick={() => setTab("tous")}>
          Tous les comptes ({users.length})
        </button>
      </div>

      {/* ── ONGLET EN ATTENTE ── */}
      {tab === "attente" && (
        <>
          {attente.length === 0
            ? <p className="empty">Aucune demande en attente. Tout est à jour ✓</p>
            : (
              <div className="list">
                {attente.map(u => (
                  <div className="row" key={u.id}>
                    <div className="row-top">
                      <div>
                        <p className="row-title">{u.firstName} {u.lastName}</p>
                        <p className="row-sub">@{u.username} · {u.role}{u.email ? ` · ${u.email}` : ""}</p>
                        {u.motif && <p className="row-sub" style={{ marginTop:4, fontStyle:"italic" }}>"{u.motif}"</p>}
                      </div>
                      <Chip tone="warn">En attente</Chip>
                    </div>
                    <div className="row-actions">
                      <button className="btn btn-green btn-sm btn-round" onClick={() => handleValider(u.id)}>
                        <i className="ti ti-check" aria-hidden="true" /> Valider
                      </button>
                      <button className="btn btn-danger btn-sm btn-round" onClick={() => handleRejeter(u.id)}>
                        <i className="ti ti-x" aria-hidden="true" /> Rejeter
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
        </>
      )}

      {/* ── ONGLET TOUS ── */}
      {tab === "tous" && (
        <>
          <div className="toolbar">
            <button className="btn btn-green btn-round" onClick={() => setCreate(v => !v)}>
              <i className="ti ti-user-plus" aria-hidden="true" /> {showCreate ? "Annuler" : "Créer un compte"}
            </button>
          </div>

          {/* Formulaire création */}
          {showCreate && (
            <div className="card" style={{ marginBottom:20 }}>
              <p className="card-title">Nouveau compte</p>
              <form onSubmit={handleCreer}>
                <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:12 }}>
                  <div className="field">
                    <label className="field-label">Prénom</label>
                    <input className="field-input" placeholder="Prénom" value={form.firstName} onChange={e => set("firstName", e.target.value)} required />
                  </div>
                  <div className="field">
                    <label className="field-label">Nom</label>
                    <input className="field-input" placeholder="Nom" value={form.lastName} onChange={e => set("lastName", e.target.value)} required />
                  </div>
                </div>
                <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:12 }}>
                  <div className="field">
                    <label className="field-label">Identifiant</label>
                    <input className="field-input" placeholder="prénom.nom" value={form.username} onChange={e => set("username", e.target.value)} required />
                  </div>
                  <div className="field">
                    <label className="field-label">Mot de passe</label>
                    <input className="field-input" type="password" placeholder="Min. 8 caractères" value={form.password} onChange={e => set("password", e.target.value)} required />
                  </div>
                </div>
                <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:12 }}>
                  <div className="field">
                    <label className="field-label">Email (optionnel)</label>
                    <input className="field-input" type="email" placeholder="email@unz.bf" value={form.email} onChange={e => set("email", e.target.value)} />
                  </div>
                  <div className="field">
                    <label className="field-label">Rôle</label>
                    <select className="field-select" value={form.role} onChange={e => set("role", e.target.value)}>
                      <option value="ETUDIANT">Étudiant</option>
                      <option value="ENSEIGNANT">Enseignant</option>
                      <option value="ADMIN">Administrateur</option>
                    </select>
                  </div>
                </div>
                <button type="submit" className="btn btn-green btn-round" disabled={creating}>
                  {creating ? "Création…" : <><i className="ti ti-user-plus" aria-hidden="true" /> Créer</>}
                </button>
              </form>
            </div>
          )}

          {/* Liste des comptes */}
          <div className="list">
            {users.map(u => (
              <div className="row" key={u.id}>
                <div className="row-top">
                  <div>
                    <p className="row-title">{u.firstName || u.username} {u.lastName || ""}</p>
                    <p className="row-sub">@{u.username} · {u.role}</p>
                  </div>
                  <Chip tone={u.active && u.statutCompte === "ACTIF" ? "green" : u.statutCompte === "EN_ATTENTE" ? "warn" : "red"}>
                    {u.statutCompte === "EN_ATTENTE" ? "En attente" : u.active ? "Actif" : "Désactivé"}
                  </Chip>
                </div>
                <div className="row-actions">
                  {u.statutCompte === "EN_ATTENTE" && (
                    <button className="btn btn-green btn-sm btn-round" onClick={() => handleValider(u.id)}>
                      <i className="ti ti-check" aria-hidden="true" /> Valider
                    </button>
                  )}
                  <button className="btn btn-ghost btn-sm btn-round" onClick={() => handleReinitialiser(u.id)}>
                    <i className="ti ti-key" aria-hidden="true" /> Réinitialiser mdp
                  </button>
                  {u.active && (
                    <button className="btn btn-danger btn-sm btn-round" onClick={() => handleDesactiver(u.id)}>
                      <i className="ti ti-user-off" aria-hidden="true" /> Désactiver
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
