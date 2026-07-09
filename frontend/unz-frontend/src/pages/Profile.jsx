import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

export default function Profile() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const name = user?.firstName ? `${user.firstName} ${user.lastName || ""}` : user?.username;
  const initials = [user?.firstName?.[0], user?.lastName?.[0]].filter(Boolean).join("").toUpperCase() || user?.username?.[0]?.toUpperCase() || "?";

  const [ancienMotDePasse, setAncien] = useState("");
  const [nouveauMotDePasse, setNouveau] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [pwdMsg, setPwdMsg] = useState(null);
  const [pwdLoading, setPwdLoading] = useState(false);

  async function handleLogout() { await logout(); navigate("/connexion"); }

  async function handleChangePassword(e) {
    e.preventDefault();
    setPwdMsg(null);
    if (nouveauMotDePasse.length < 6) {
      setPwdMsg({ type: "error", text: "Le nouveau mot de passe doit contenir au moins 6 caractères." });
      return;
    }
    if (nouveauMotDePasse !== confirmation) {
      setPwdMsg({ type: "error", text: "Les deux mots de passe ne correspondent pas." });
      return;
    }
    setPwdLoading(true);
    try {
      await api.post("/auth/changer-mot-de-passe", { ancienMotDePasse, nouveauMotDePasse });
      setPwdMsg({ type: "ok", text: "Mot de passe modifié avec succès." });
      setAncien(""); setNouveau(""); setConfirmation("");
    } catch (err) {
      setPwdMsg({ type: "error", text: err?.message || "L'ancien mot de passe est incorrect." });
    } finally {
      setPwdLoading(false);
    }
  }

  return (
    <div className="page">
      <div className="card" style={{ maxWidth: 480, marginBottom: 20 }}>
        <div style={{ display:"flex", alignItems:"center", gap:16, marginBottom:20 }}>
          <div style={{ width:56,height:56,borderRadius:"50%",background:"var(--dark)",color:"var(--white)",fontWeight:700,fontSize:20,display:"flex",alignItems:"center",justifyContent:"center",flexShrink:0 }}>{initials}</div>
          <div>
            <p style={{ fontWeight:700, fontSize:17, color:"var(--ink)" }}>{name}</p>
            <p style={{ fontSize:12, color:"var(--ink-3)", marginTop:2 }}>@{user?.username}</p>
          </div>
        </div>
        <div className="divider" />
        <div style={{ display:"flex", flexDirection:"column", gap:10 }}>
          {user?.email && <div style={{ display:"flex",justifyContent:"space-between",fontSize:13 }}><span style={{color:"var(--ink-3)"}}>Email</span><span style={{fontWeight:500}}>{user.email}</span></div>}
          <div style={{ display:"flex",justifyContent:"space-between",fontSize:13 }}><span style={{color:"var(--ink-3)"}}>Rôle</span><span style={{fontWeight:500}}>{user?.role}</span></div>
          {user?.classeNom && <div style={{ display:"flex",justifyContent:"space-between",fontSize:13 }}><span style={{color:"var(--ink-3)"}}>Classe</span><span style={{fontWeight:500}}>{user.classeNom}</span></div>}
        </div>
        <div className="divider" />
        <button className="btn btn-danger btn-sm btn-round" onClick={handleLogout}>
          <i className="ti ti-logout" aria-hidden="true" /> Se déconnecter
        </button>
      </div>

      <div className="card" style={{ maxWidth: 480 }}>
        <p className="card-title">Changer mon mot de passe</p>
        <form onSubmit={handleChangePassword} noValidate>
          <div className="field">
            <label className="field-label">Mot de passe actuel</label>
            <input className="field-input" type="password" value={ancienMotDePasse} onChange={e => setAncien(e.target.value)} autoComplete="current-password" disabled={pwdLoading} />
          </div>
          <div className="field">
            <label className="field-label">Nouveau mot de passe</label>
            <input className="field-input" type="password" value={nouveauMotDePasse} onChange={e => setNouveau(e.target.value)} autoComplete="new-password" disabled={pwdLoading} />
          </div>
          <div className="field">
            <label className="field-label">Confirmer le nouveau mot de passe</label>
            <input className="field-input" type="password" value={confirmation} onChange={e => setConfirmation(e.target.value)} autoComplete="new-password" disabled={pwdLoading} />
          </div>

          {pwdMsg && (
            <div className={`alert ${pwdMsg.type === "ok" ? "alert-success" : "alert-error"}`} style={{ marginBottom: 16 }}>
              <i className={`ti ${pwdMsg.type === "ok" ? "ti-check" : "ti-alert-circle"}`} aria-hidden="true" />
              {pwdMsg.text}
            </div>
          )}

          <button type="submit" className="btn btn-green btn-sm btn-round" disabled={pwdLoading}>
            {pwdLoading ? "Enregistrement…" : "Enregistrer le nouveau mot de passe"}
          </button>
        </form>
      </div>
    </div>
  );
}
