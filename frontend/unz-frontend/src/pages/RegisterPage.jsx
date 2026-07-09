import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { api } from "../api/client";

const ROLES = [
  { key: "ETUDIANT",   label: "Étudiant",    desc: "Je suis étudiant à l'UNZ",    icon: "ti-user-graduate" },
  { key: "ENSEIGNANT", label: "Enseignant",   desc: "J'enseigne à l'université",   icon: "ti-chalkboard" },
];

const ERRORS_MAP = {
  400: null, // message du serveur utilisé
  409: "Cet identifiant est déjà utilisé.",
  429: "Trop de tentatives. Réessayez dans quelques minutes.",
  500: "Erreur serveur. Réessayez dans un moment.",
};

export default function RegisterPage() {
  const navigate = useNavigate();
  const [role, setRole]         = useState("ETUDIANT");
  const [classes, setClasses]   = useState([]);
  const [form, setForm]         = useState({ username:"", password:"", firstName:"", lastName:"", email:"", classeId:"", motif:"" });
  const [showPwd, setShowPwd]   = useState(false);
  const [errors, setErrors]     = useState({});
  const [globalError, setGE]    = useState("");
  const [loading, setLoading]   = useState(false);
  const [success, setSuccess]   = useState(false);
  const [checking, setChecking] = useState(false);
  const [usernameOk, setUOk]    = useState(null);

  useEffect(() => {
    api.get("/classes").then(setClasses).catch(() => {});
  }, []);

  function set(k, v) { setForm(f => ({ ...f, [k]: v })); }

  async function checkUsername(val) {
    if (!val || val.length < 3) { setUOk(null); return; }
    setChecking(true);
    try {
      const res = await api.get(`/auth/inscription/check-username?username=${encodeURIComponent(val)}`);
      setUOk(res.disponible);
    } catch { setUOk(null); }
    finally { setChecking(false); }
  }

  function validate() {
    const e = {};
    if (!form.firstName.trim()) e.firstName = "Prénom requis.";
    if (!form.lastName.trim())  e.lastName  = "Nom requis.";
    if (!form.username.trim() || form.username.length < 3) e.username = "Identifiant trop court (min. 3 caractères).";
    if (usernameOk === false) e.username = "Cet identifiant est déjà utilisé.";
    if (!form.password || form.password.length < 8) e.password = "Mot de passe trop court (min. 8 caractères).";
    if (role === "ETUDIANT" && !form.classeId) e.classeId = "Veuillez choisir votre classe.";
    return e;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setGE("");
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length > 0) return;
    setLoading(true);
    try {
      await api.post("/auth/inscription", {
        username:  form.username,
        password:  form.password,
        firstName: form.firstName,
        lastName:  form.lastName,
        email:     form.email || null,
        role,
        classeId:  role === "ETUDIANT" ? Number(form.classeId) : null,
        motif:     form.motif || null,
      });
      setSuccess(true);
    } catch (err) {
      const status = err?.status;
      const msg = ERRORS_MAP[status] ?? err?.message ?? "Une erreur inattendue s'est produite.";
      setGE(msg || err?.message || "Erreur inattendue.");
    } finally { setLoading(false); }
  }

  if (success) return (
    <div className="login-screen">
      <div style={{ background:"var(--white)", borderRadius:20, padding:"48px 40px", maxWidth:440, width:"100%", boxShadow:"var(--sh-lg)", textAlign:"center" }}>
        <div style={{ width:64, height:64, borderRadius:"50%", background:"var(--ok-bg)", display:"flex", alignItems:"center", justifyContent:"center", margin:"0 auto 20px" }}>
          <i className="ti ti-check" style={{ fontSize:32, color:"var(--ok-txt)" }} aria-hidden="true" />
        </div>
        <h1 style={{ fontSize:22, fontWeight:700, color:"var(--ink)", marginBottom:10 }}>Demande envoyée !</h1>
        <p style={{ fontSize:14, color:"var(--ink-3)", marginBottom:8 }}>
          Votre demande de compte a bien été transmise à l'administrateur.
        </p>
        <p style={{ fontSize:14, color:"var(--ink-3)", marginBottom:28 }}>
          Vous recevrez une confirmation dès que votre compte sera activé. Vous pourrez alors vous connecter avec l'identifiant <strong>{form.username}</strong>.
        </p>
        <Link to="/connexion">
          <button className="btn btn-green btn-block btn-round" style={{ fontSize:14 }}>
            Retour à la connexion
          </button>
        </Link>
      </div>
    </div>
  );

  return (
    <div className="login-screen" style={{ alignItems:"flex-start", paddingTop:40, paddingBottom:40 }}>
      <div style={{ background:"var(--white)", borderRadius:20, padding:"40px 36px", maxWidth:520, width:"100%", boxShadow:"var(--sh-lg)" }}>

        {/* Header */}
        <div style={{ marginBottom:28 }}>
          <p style={{ fontSize:22, fontWeight:700, color:"var(--ink)", letterSpacing:"-.02em" }}>
            UNZ<span style={{ color:"var(--green)" }}>Quiz</span>
          </p>
          <h1 style={{ fontSize:20, fontWeight:700, color:"var(--ink)", marginTop:12, marginBottom:4 }}>Créer un compte</h1>
          <p style={{ fontSize:13, color:"var(--ink-3)" }}>
            Votre demande sera validée par l'administrateur avant activation.
          </p>
        </div>

        {/* Choix du rôle */}
        <p style={{ fontSize:11, fontWeight:700, color:"var(--ink-3)", textTransform:"uppercase", letterSpacing:".06em", marginBottom:10 }}>
          Je suis…
        </p>
        <div style={{ display:"flex", gap:10, marginBottom:24 }}>
          {ROLES.map(r => (
            <button key={r.key} type="button"
              onClick={() => setRole(r.key)}
              style={{
                flex:1, padding:"12px 14px", border:`1.5px solid ${role===r.key?"var(--green)":"var(--line-2)"}`,
                borderRadius:10, background: role===r.key ? "var(--ok-bg)" : "var(--white)",
                cursor:"pointer", textAlign:"left", transition:"all .15s"
              }}>
              <i className={`ti ${r.icon}`} style={{ fontSize:18, color: role===r.key?"var(--green)":"var(--ink-3)", display:"block", marginBottom:6 }} aria-hidden="true" />
              <p style={{ fontSize:13, fontWeight:600, color: role===r.key?"var(--ok-txt)":"var(--ink)", marginBottom:2 }}>{r.label}</p>
              <p style={{ fontSize:11, color:"var(--ink-3)" }}>{r.desc}</p>
            </button>
          ))}
        </div>

        {globalError && (
          <div className="alert alert-error" style={{ marginBottom:16 }}>
            <i className="ti ti-alert-circle" aria-hidden="true" /> {globalError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          {/* Prénom + Nom */}
          <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:12, marginBottom:0 }}>
            <div className="field">
              <label className="field-label">Prénom</label>
              <input className="field-input" placeholder="Daouda" value={form.firstName} onChange={e => set("firstName", e.target.value)} disabled={loading} />
              {errors.firstName && <p className="field-error">{errors.firstName}</p>}
            </div>
            <div className="field">
              <label className="field-label">Nom</label>
              <input className="field-input" placeholder="Traoré" value={form.lastName} onChange={e => set("lastName", e.target.value)} disabled={loading} />
              {errors.lastName && <p className="field-error">{errors.lastName}</p>}
            </div>
          </div>

          {/* Identifiant */}
          <div className="field">
            <label className="field-label">Identifiant</label>
            <div style={{ position:"relative" }}>
              <input className="field-input" placeholder="daouda.traore" value={form.username}
                onChange={e => { set("username", e.target.value); checkUsername(e.target.value); }}
                disabled={loading} style={{ paddingRight:32 }} />
              {checking && <span style={{ position:"absolute", right:10, top:"50%", transform:"translateY(-50%)", fontSize:11, color:"var(--ink-4)" }}>…</span>}
              {!checking && usernameOk === true  && <i className="ti ti-check" style={{ position:"absolute", right:10, top:"50%", transform:"translateY(-50%)", color:"var(--ok-txt)" }} aria-hidden="true" />}
              {!checking && usernameOk === false && <i className="ti ti-x" style={{ position:"absolute", right:10, top:"50%", transform:"translateY(-50%)", color:"var(--danger)" }} aria-hidden="true" />}
            </div>
            {errors.username && <p className="field-error">{errors.username}</p>}
            {!errors.username && usernameOk === true && <p className="field-hint" style={{ color:"var(--ok-txt)" }}>Identifiant disponible.</p>}
          </div>

          {/* Mot de passe */}
          <div className="field">
            <label className="field-label">Mot de passe</label>
            <div style={{ position:"relative" }}>
              <input className="field-input" type={showPwd?"text":"password"} placeholder="Min. 8 caractères"
                value={form.password} onChange={e => set("password", e.target.value)}
                disabled={loading} style={{ paddingRight:70 }} />
              <button type="button" onClick={() => setShowPwd(v=>!v)}
                style={{ position:"absolute", right:12, top:"50%", transform:"translateY(-50%)", background:"none", border:"none", cursor:"pointer", fontSize:11, color:"var(--ink-4)", fontFamily:"var(--font)", fontWeight:600 }}>
                {showPwd ? "Masquer" : "Afficher"}
              </button>
            </div>
            {errors.password && <p className="field-error">{errors.password}</p>}
          </div>

          {/* Email (optionnel) */}
          <div className="field">
            <label className="field-label">Email <span style={{ color:"var(--ink-4)", fontWeight:400 }}>(optionnel)</span></label>
            <input className="field-input" type="email" placeholder="daouda@email.com" value={form.email} onChange={e => set("email", e.target.value)} disabled={loading} />
          </div>

          {/* Classe (si étudiant) */}
          {role === "ETUDIANT" && (
            <div className="field">
              <label className="field-label">Ma classe</label>
              <select className="field-select" value={form.classeId} onChange={e => set("classeId", e.target.value)} disabled={loading}>
                <option value="">Sélectionner ma classe…</option>
                {classes.map(c => <option key={c.id} value={c.id}>{c.nom}</option>)}
              </select>
              {errors.classeId && <p className="field-error">{errors.classeId}</p>}
            </div>
          )}

          {/* Motif (optionnel) */}
          <div className="field">
            <label className="field-label">Message pour l'admin <span style={{ color:"var(--ink-4)", fontWeight:400 }}>(optionnel)</span></label>
            <textarea className="field-input" placeholder="Précisez si nécessaire…" rows={2}
              value={form.motif} onChange={e => set("motif", e.target.value)}
              disabled={loading} style={{ resize:"vertical" }} />
          </div>

          <button type="submit" className="btn btn-green btn-xl btn-block btn-round" disabled={loading} style={{ marginBottom:16 }}>
            {loading
              ? <><i className="ti ti-loader-2" style={{ animation:"spin 1s linear infinite" }} aria-hidden="true" /> Envoi en cours…</>
              : <><i className="ti ti-send" aria-hidden="true" /> Envoyer ma demande</>}
          </button>
        </form>

        <p style={{ fontSize:12, color:"var(--ink-4)", textAlign:"center" }}>
          Déjà un compte ? <Link to="/connexion" style={{ color:"var(--green)", fontWeight:600 }}>Se connecter</Link>
        </p>
      </div>
      <style>{`@keyframes spin{from{transform:rotate(0)}to{transform:rotate(360deg)}}`}</style>
    </div>
  );
}
