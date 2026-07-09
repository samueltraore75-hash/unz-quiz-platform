import { api } from "./client";

export const quizApi = {
  list: () => api.get("/quizzes"),
  detail: (id) => api.get(`/quizzes/${id}`),
  start: (id) => api.post(`/quizzes/${id}/start`),
  enCours: (tentativeId) => api.get(`/tentatives/${tentativeId}`),
  sauvegarderReponse: (tentativeId, reponse) => api.post(`/tentatives/${tentativeId}/reponses`, reponse),
  submit: (tentativeId) => api.post(`/tentatives/${tentativeId}/submit`),
  mine: () => api.get("/tentatives/mine"),
};
