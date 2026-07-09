import { api } from "./client";

export const notificationsApi = {
  list: () => api.get("/notifications"),
  count: () => api.get("/notifications/count"),
  lire: (id) => api.post(`/notifications/${id}/lire`),
  lireTout: () => api.post("/notifications/lire-tout"),
};
