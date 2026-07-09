import { api } from "./client";

export const gradesApi = {
  bulletinsMine: () => api.get("/bulletins/mine"),
  progression: () => api.get("/progression"),
};
