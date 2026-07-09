import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { api } from "../api/client";

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token") || "";

  const [password, setPassword] = useState("");
  const [confirm, setConfirm]   = useState("");
  const [showPwd, setShow]      = useState(false);
  const [error, setError]       = useState("");
  const [done, setDone]         = useState(false);
  const [loading, setLoading]   = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (!token) { setError("Lien invalide : aucun token trouvé dans l'URL."); return; }
    if (password.length < 6) { setError("Le mot de passe doit contenir au moins 6 caractères."); return; }
    if (password !== confirm) { setError("Les deux mots de passe ne correspondent pas."); return; }

    setLoading(true);
    try {
      await api.post("/auth/reinitialiser-mot-de-passe", { token, nouveauMotDePasse: password });
      setDone(true);
    } catch (err) {
      if (err?.status === 400) {
        setError(err.message || "Ce lien est invalide, expiré, ou a déjà été utilisé. Refaites une demande depuis la page de connexion.");
      } else {
        setError("Une erreur est survenue. Réessayez dans un instant.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="login-left">
          <div>
            <p className="login-brand-name">UNZ<em>Quiz</em></p>
            <p className="login-brand-sub">Plateforme d'évaluation en ligne<br />Université Norbert Zongo</p>
          </div>
        </div>

        <div className="login-right">
          <p className="login-eyebrow">Réinitialisation</p>

          {done ? (
            <>
              <h1 className="login-title">Mot de passe modifié ✅</h1>
              <p className="login-subtitle">
                Votre mot de passe a bien été réinitialisé. Vos anciennes sessions ont été déconnectées par sécurité.
              </p>
              <button
                type="button"
                className="btn btn-green btn-xl btn-block"
                onClick={() => navigate("/connexion", { replace: true })}
              >
                <i className="ti ti-login" aria-hidden="true" /> Aller à la connexion
              </button>
            </>
          ) : (
            <>
              <h1 className="login-title">Nouveau mot de passe</h1>
              <p className="login-subtitle">
                Choisissez un nouveau mot de passe pour votre compte.
              </p>

              {!token && (
                <div className="alert alert-error" style={{ marginBottom: 16 }}>
                  <i className="ti ti-alert-circle" aria-hidden="true" />
                  Aucun token trouvé dans le lien. Vérifiez que vous avez copié l'URL complète reçue par e-mail.
                </div>
              )}

              <form onSubmit={handleSubmit} noValidate>
                <div className="field">
                  <label className="field-label">Nouveau mot de passe</label>
                  <div style={{ position: "relative" }}>
                    <input
                      className="field-input"
                      type={showPwd ? "text" : "password"}
                      placeholder="••••••••"
                      value={password}
                      onChange={e => setPassword(e.target.value)}
                      autoComplete="new-password"
                      autoFocus
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

                <div className="field">
                  <label className="field-label">Confirmer le mot de passe</label>
                  <input
                    className="field-input"
                    type={showPwd ? "text" : "password"}
                    placeholder="••••••••"
                    value={confirm}
                    onChange={e => setConfirm(e.target.value)}
                    autoComplete="new-password"
                    disabled={loading}
                  />
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
                  disabled={loading || !token}
                >
                  {loading
                    ? <><i className="ti ti-loader-2" aria-hidden="true" style={{ animation:"spin 1s linear infinite" }} /> Enregistrement…</>
                    : <><i className="ti ti-lock" aria-hidden="true" /> Réinitialiser le mot de passe</>
                  }
                </button>
              </form>

              <p className="login-hint">
                <a href="/connexion" style={{ color: "var(--green)", fontWeight: 600 }}>
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
