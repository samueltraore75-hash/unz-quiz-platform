import { NavLink, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import NotificationBell from "./NotificationBell";

const NAV = {
  ETUDIANT: [
    { to: "/",       label: "Tableau de bord", icon: "ti-home" },
    { to: "/quiz",   label: "Mes devoirs",      icon: "ti-clipboard-list" },
    { to: "/notes",  label: "Notes & Bulletins",icon: "ti-chart-bar" },
    { to: "/profil", label: "Mon profil",       icon: "ti-user" },
  ],
  ENSEIGNANT: [
    { to: "/",             label: "Tableau de bord",  icon: "ti-home" },
    { to: "/quiz",         label: "Mes devoirs",       icon: "ti-clipboard-list" },
    { to: "/questions",    label: "Banque de questions",icon: "ti-database" },
    { to: "/notes/saisie", label: "Saisie de notes",   icon: "ti-pencil" },
    { to: "/profil",       label: "Mon profil",        icon: "ti-user" },
  ],
  ADMIN: [
    { to: "/",             label: "Tableau de bord",   icon: "ti-home" },
    { to: "/utilisateurs", label: "Utilisateurs",       icon: "ti-users" },
    { to: "/referentiel",  label: "Référentiel",        icon: "ti-building" },
    { to: "/bulletins",    label: "Bulletins",          icon: "ti-file-text" },
    { to: "/profil",       label: "Mon profil",         icon: "ti-user" },
  ],
};

const TITLES = {
  "/":             "Tableau de bord",
  "/quiz":         "Mes devoirs",
  "/notes":        "Notes & Bulletins",
  "/questions":    "Banque de questions",
  "/notes/saisie": "Saisie de notes",
  "/utilisateurs": "Utilisateurs",
  "/referentiel":  "Référentiel académique",
  "/bulletins":    "Bulletins",
  "/profil":       "Mon profil",
};

export default function AppShell({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const items = NAV[user?.role] || [];

  const initials = [user?.firstName?.[0], user?.lastName?.[0]]
    .filter(Boolean).join("").toUpperCase() ||
    user?.username?.[0]?.toUpperCase() || "?";

  const title = Object.entries(TITLES)
    .sort((a, b) => b[0].length - a[0].length)
    .find(([p]) => pathname.startsWith(p))?.[1] || "UNZ Quiz";

  async function handleLogout() { await logout(); navigate("/connexion"); }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="sidebar-logo">UNZ<em>Quiz</em></div>

        <nav className="sidebar-nav">
          <p className="sidebar-section">Menu</p>
          {items.map(item => (
            <NavLink
              key={item.to} to={item.to} end={item.to === "/"}
              className={({ isActive }) => `sidebar-link${isActive ? " active" : ""}`}
            >
              <i className={`ti ${item.icon}`} aria-hidden="true" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="s-avatar">{initials}</div>
            <div>
              <p className="s-uname">{user?.firstName ? `${user.firstName} ${user.lastName || ""}` : user?.username}</p>
              <p className="s-urole">{user?.role}</p>
            </div>
          </div>
          <button className="sidebar-logout" onClick={handleLogout}>
            <i className="ti ti-logout" aria-hidden="true" />
            Se déconnecter
          </button>
        </div>
      </aside>

      <div className="main">
        <header className="topbar">
          <h1 className="topbar-title">{title}</h1>
          <div className="topbar-right">
            <NotificationBell />
          </div>
        </header>
        <div>{children}</div>
      </div>

      <nav className="bottomnav">
        {items.slice(0, 4).map(item => (
          <NavLink
            key={item.to} to={item.to} end={item.to === "/"}
            className={({ isActive }) => `bottomnav-link${isActive ? " active" : ""}`}
          >
            <i className={`ti ${item.icon}`} aria-hidden="true" />
            {item.label.split(" ")[0]}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
