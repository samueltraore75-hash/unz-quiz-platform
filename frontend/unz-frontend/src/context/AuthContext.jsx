import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { api, tokenStore } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  // Appelé au démarrage de l'app pour restaurer la session
  const loadCurrentUser = useCallback(async () => {
    const { accessToken } = tokenStore.getTokens();
    if (!accessToken) {
      setLoading(false);
      return;
    }
    try {
      const me = await api.get("/auth/me");
      setUser({ ...me, role: me.role });
    } catch {
      tokenStore.clearTokens();
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadCurrentUser(); }, [loadCurrentUser]);

  async function login(username, password) {
    const data = await api.post("/auth/login", { username, password });
    // Stocker les tokens AVANT de mettre à jour le state
    tokenStore.setTokens({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
    });
    // Construire l'objet user depuis la réponse du login directement
    // sans rappeler /auth/me (évite la race condition)
    const u = {
      ...(data.user || {}),
      role: data.role || data.user?.role,
    };
    setUser(u);
    setLoading(false);
    return u;
  }

  async function logout() {
    const { refreshToken } = tokenStore.getTokens();
    try { await api.post("/auth/logout", { refreshToken }); } catch { /* nettoyage local quoi qu'il arrive */ }
    tokenStore.clearTokens();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, reload: loadCurrentUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth doit être utilisé dans AuthProvider");
  return ctx;
}
