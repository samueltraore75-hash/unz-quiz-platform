// En local : "/api" est redirigé vers localhost:8080 par le proxy Vite (voir vite.config.js).
// En production, le frontend et le backend sont déployés séparément (deux services distincts) :
// il faut alors définir VITE_API_URL au moment du build avec l'URL complète du backend,
// ex: VITE_API_URL=https://unzquiz-backend.up.railway.app/api
const BASE_URL = import.meta.env.VITE_API_URL || "/api";

function getTokens() {
  return {
    accessToken:  localStorage.getItem("unz_access_token"),
    refreshToken: localStorage.getItem("unz_refresh_token"),
  };
}
function setTokens({ accessToken, refreshToken }) {
  if (accessToken)  localStorage.setItem("unz_access_token", accessToken);
  if (refreshToken) localStorage.setItem("unz_refresh_token", refreshToken);
}
function clearTokens() {
  localStorage.removeItem("unz_access_token");
  localStorage.removeItem("unz_refresh_token");
}

let refreshPromise = null;

async function refreshAccessToken() {
  const { refreshToken } = getTokens();
  if (!refreshToken) throw new Error("Pas de refresh token");
  if (!refreshPromise) {
    refreshPromise = fetch(`${BASE_URL}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    }).then(async res => {
      if (!res.ok) throw new Error("Refresh échoué");
      const data = await res.json();
      setTokens(data);
      return data;
    }).finally(() => { refreshPromise = null; });
  }
  return refreshPromise;
}

class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
    this.name = "ApiError";
  }
}

export async function apiFetch(path, options = {}, retry = true) {
  const { accessToken } = getTokens();
  const headers = { ...(options.headers || {}) };
  if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;
  if (options.body && typeof options.body === "string") {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // Ne pas tenter de refresh sur /auth/login ni /auth/refresh eux-mêmes
  if (res.status === 401 && retry && !path.startsWith("/auth/")) {
    try {
      await refreshAccessToken();
      return apiFetch(path, options, false);
    } catch {
      clearTokens();
      window.location.href = "/connexion";
      throw new ApiError("Session expirée", 401);
    }
  }

  if (!res.ok) {
    let message = `Erreur ${res.status}`;
    try {
      const body = await res.json();
      message = body.message || body.error || message;
    } catch { /* body non-JSON */ }
    throw new ApiError(message, res.status);
  }

  if (res.status === 204) return null;
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res;
}

export const api = {
  get:  (path)       => apiFetch(path),
  post: (path, body) => apiFetch(path, { method: "POST", body: body ? JSON.stringify(body) : undefined }),
  put:  (path, body) => apiFetch(path, { method: "PUT",  body: body ? JSON.stringify(body) : undefined }),
  del:  (path)       => apiFetch(path, { method: "DELETE" }),
};

export const tokenStore = { getTokens, setTokens, clearTokens };
