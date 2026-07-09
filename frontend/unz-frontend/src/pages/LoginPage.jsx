import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";

const ROLES = [
  { key: "ETUDIANT",   label: "Étudiant",        desc: "Passer mes devoirs · Voir mes notes", icon: "ti-user-graduate" },
  { key: "ENSEIGNANT", label: "Enseignant",       desc: "Créer devoirs · Gérer mes classes",   icon: "ti-chalkboard" },
  { key: "ADMIN",      label: "Administrateur",   desc: "Gérer la plateforme",                 icon: "ti-shield-check" },
];

const ROLE_LABELS = { ETUDIANT: "Espace Étudiant", ENSEIGNANT: "Espace Enseignant", ADMIN: "Espace Administrateur" };

function friendlyError(err) {
  const s = err?.status;
  if (s === 401) return "Identifiant ou mot de passe incorrect.";
  if (s === 403) return "Votre compte est désactivé. Contactez l'administrateur.";
  if (s === 429) return "Trop de tentatives. Patientez quelques minutes.";
  if (s === 500) return "Problème serveur. Réessayez dans un moment.";
  if (!navigator.onLine || err?.message?.includes("fetch")) return "Impossible de joindre le serveur. Vérifiez que le backend est démarré.";
  return err?.message || "Une erreur inattendue s'est produite.";
}

export default function LoginPage() {
  const { login }   = useAuth();
  const navigate    = useNavigate();
  const location    = useLocation();
  const [role, setRole]     = useState("ETUDIANT");
  const [username, setUser] = useState("");
  const [password, setPass] = useState("");
  const [showPwd, setShow]  = useState(false);
  const [error, setError]   = useState("");
  const [loading, setLoad]  = useState(false);

  const [showForgot, setShowForgot]   = useState(false);
  const [forgotEmail, setForgotEmail] = useState("");
  const [forgotMsg, setForgotMsg]     = useState("");
  const [forgotLoading, setForgotLoading] = useState(false);

  async function handleForgotSubmit(e) {
    e.preventDefault();
    if (!forgotEmail.trim()) return;
    setForgotLoading(true); setForgotMsg("");
    try {
      const res = await api.post("/auth/mot-de-passe-oublie", { email: forgotEmail.trim() });
      setForgotMsg(res?.message || "Si cette adresse est associée à un compte, un lien de réinitialisation vient de lui être envoyé.");
    } catch (err) {
      setForgotMsg("Une erreur est survenue. Réessayez dans un instant.");
    } finally {
      setForgotLoading(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!username.trim()) { setError("Veuillez saisir votre identifiant."); return; }
    if (!password)        { setError("Veuillez saisir votre mot de passe."); return; }
    setError(""); setLoad(true);
    try {
      await login(username.trim(), password);
      navigate(location.state?.from || "/", { replace: true });
    } catch (err) {
      setError(friendlyError(err));
    } finally { setLoad(false); }
  }

  return (
    <div className="login-screen">
      <div className="login-card">

        {/* ── GAUCHE ── */}
        <div className="login-left">
          <div>
            <p className="login-brand-name">UNZ<em>Quiz</em></p>
            <p className="login-brand-sub">Plateforme d'évaluation en ligne<br />Université Norbert Zongo</p>
          </div>

          <div>
            <p className="login-roles-label">Choisissez votre espace</p>
            <div className="login-roles">
              {ROLES.map(r => (
                <button
                  key={r.key}
                  className={`role-btn ${role === r.key ? "active" : ""}`}
                  onClick={() => setRole(r.key)}
                  type="button"
                >
                  <div className="role-icon">
                    <i className={`ti ${r.icon}`} aria-hidden="true" />
                  </div>
                  <div>
                    <p className="role-name">{r.label}</p>
                    <p className="role-desc">{r.desc}</p>
                  </div>
                  <div className="role-check">
                    {role === r.key && "✓"}
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* ── DROITE ── */}
        <div className="login-right">
          <p className="login-eyebrow">{ROLE_LABELS[role]}</p>

          {!showForgot ? (
            <>
              <h1 className="login-title">Bonjour 👋</h1>
              <p className="login-subtitle">Entrez vos identifiants pour accéder à votre espace.</p>

              <form onSubmit={handleSubmit} noValidate>
                <div className="field">
                  <label className="field-label">Identifiant</label>
                  <input
                    className="field-input"
                    type="text"
                    placeholder="votre.identifiant"
                    value={username}
                    onChange={e => setUser(e.target.value)}
                    autoComplete="username"
                    autoFocus
                    disabled={loading}
                  />
                </div>

                <div className="field">
                  <label className="field-label">Mot de passe</label>
                  <div style={{ position: "relative" }}>
                    <input
                      className="field-input"
                      type={showPwd ? "text" : "password"}
                      placeholder="••••••••"
                      value={password}
                      onChange={e => setPass(e.target.value)}
                      autoComplete="current-password"
                      disabled={loading}
                      style={{ paddingRight: 70 }}
                    />
                    <button
                      type="button"
                      onClick={() => setShow(v => !v)}
                      style={{ position:"absolute",right:12,top:"50%",transform:"translateY(-50%)",
                        background:"none",border:"none",color:"var(--ink-4)",cursor:"pointer",
                        fontSize:11,fontFamily:"var(--font)",fontWeight:600 }}
                    >
                      {showPwd ? "Masquer" : "Afficher"}
                    </button>
                  </div>
                </div>

                {error && (
                  <div className="alert alert-error" style={{ marginBottom: 16 }}>
                    <i className="ti ti-alert-circle" aria-hidden="true" />
                    {error}
                  </div>
                )}

                <button
                  type="submit"
                  className="btn btn-green btn-xl btn-block"
                  disabled={loading}
                >
                  {loading
                    ? <><i className="ti ti-loader-2" aria-hidden="true" style={{ animation:"spin 1s linear infinite" }} /> Connexion…</>
                    : <><i className="ti ti-login" aria-hidden="true" /> Se connecter</>
                  }
                </button>
              </form>

              <p className="login-hint">
                Mot de passe oublié ?{" "}
                <a onClick={() => { setShowForgot(true); setForgotMsg(""); }} style={{ cursor: "pointer", color: "var(--green)", fontWeight: 600 }}>
                  Réinitialiser
                </a>
              </p>
              <p className="login-hint" style={{ marginTop:8 }}>
                Pas encore de compte ?{" "}
                <a href="/inscription" style={{ color:"var(--green)", fontWeight:600 }}>Créer un compte</a>
              </p>
            </>
          ) : (
            <>
              <h1 className="login-title">Mot de passe oublié</h1>
              <p className="login-subtitle">
                Renseignez l'adresse e-mail de votre compte : si elle est reconnue, un lien de réinitialisation valable 30 minutes vous sera envoyé par e-mail.
              </p>

              <form onSubmit={handleForgotSubmit} noValidate>
                <div className="field">
                  <label className="field-label">Adresse e-mail</label>
                  <input
                    className="field-input"
                    type="email"
                    placeholder="vous@exemple.com"
                    value={forgotEmail}
                    onChange={e => setForgotEmail(e.target.value)}
                    autoComplete="email"
                    autoFocus
                    disabled={forgotLoading}
                  />
                </div>

                {forgotMsg && (
                  <div className="alert alert-error" style={{ marginBottom: 16, background: "var(--card)", color: "var(--ink-2)" }}>
                    <i className="ti ti-mail" aria-hidden="true" />
                    {forgotMsg}
                  </div>
                )}

                <button
                  type="submit"
                  className="btn btn-green btn-xl btn-block"
                  disabled={forgotLoading}
                >
                  {forgotLoading
                    ? <><i className="ti ti-loader-2" aria-hidden="true" style={{ animation:"spin 1s linear infinite" }} /> Envoi…</>
                    : <><i className="ti ti-send" aria-hidden="true" /> Envoyer le lien de réinitialisation</>
                  }
                </button>
              </form>

              <p className="login-hint">
                <a onClick={() => { setShowForgot(false); setForgotMsg(""); setForgotEmail(""); }} style={{ cursor: "pointer", color: "var(--green)", fontWeight: 600 }}>
                  ← Retour à la connexion
                </a>
              </p>
            </>
          )}
        </div>
      </div>

      <style>{`@keyframes spin{from{transform:rotate(0)}to{transform:rotate(360deg)}}`}</style>
    </div>
  );
}
