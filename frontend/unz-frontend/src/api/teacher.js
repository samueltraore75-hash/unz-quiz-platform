import { api } from "./client";

export const teacherApi = {
  myMatieres: () => api.get("/matieres/mine"),
  classes: () => api.get("/classes"),
  etudiantsDeClasse: (classeId) => api.get(`/classes/${classeId}/etudiants`),

  createQuiz: (payload) => api.post("/quizzes", payload),
  addQuestions: (quizId, questionIds) => api.post(`/quizzes/${quizId}/questions`, { questionIds }),
  cloturerQuiz: (quizId) => api.post(`/quizzes/${quizId}/cloturer`),
  deleteQuiz: (quizId) => api.del(`/quizzes/${quizId}`),
  quizStats: (quizId) => api.get(`/quizzes/${quizId}/stats`),
  quizNotes: (quizId) => api.get(`/quizzes/${quizId}/notes`),

  questions: (matiereId) => api.get(`/questions?matiereId=${matiereId}`),
  createQuestion: (payload) => api.post("/questions", payload),
  updateQuestion: (id, payload) => api.put(`/questions/${id}`, payload),
  deleteQuestion: (id) => api.del(`/questions/${id}`),

  saisirNote: (matiereId, payload) => api.post(`/notes/matieres/${matiereId}`, payload),
};
