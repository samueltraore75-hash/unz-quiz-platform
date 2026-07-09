import { api } from "./client";

export const adminApi = {
  // Utilisateurs
  users: (role) => api.get(`/admin/users${role ? `?role=${role}` : ""}`),
  createUser: (payload) => api.post("/admin/users", payload),
  desactiverUser: (id) => api.del(`/admin/users/${id}`),
  deactivateUser: (id) => api.del(`/admin/users/${id}`),
  reinitialiserMdp: (id) => api.post(`/admin/users/${id}/reinitialiser-mot-de-passe`),
  reinitialiserMotDePasse: (id) => api.post(`/admin/users/${id}/reinitialiser-mot-de-passe`),

  // Demandes de compte en attente (v3.3)
  enAttente: () => api.get("/admin/users/en-attente"),
  validerCompte: (id) => api.post(`/admin/users/${id}/valider`),
  rejeterCompte: (id) => api.post(`/admin/users/${id}/rejeter`),

  // Référentiel
  filieres: () => api.get("/admin/filieres"),
  createFiliere: (payload) => api.post("/admin/filieres", payload),
  deleteFiliere: (id) => api.del(`/admin/filieres/${id}`),

  niveaux: (filiereId) => api.get(`/admin/niveaux${filiereId ? `?filiereId=${filiereId}` : ""}`),
  createNiveau: (payload) => api.post("/admin/niveaux", payload),

  annees: () => api.get("/admin/annees"),
  createAnnee: (payload) => api.post("/admin/annees", payload),
  activerAnnee: (id) => api.post(`/admin/annees/${id}/activer`),

  classes: (params) => api.get(`/admin/classes${params ? `?${params}` : ""}`),
  createClasse: (payload) => api.post("/admin/classes", payload),

  semestres: (niveauId, anneeId) => api.get(`/admin/semestres?niveauId=${niveauId}&anneeId=${anneeId}`),
  createSemestre: (payload) => api.post("/admin/semestres", payload),

  ues: (semestreId) => api.get(`/admin/ues?semestreId=${semestreId}`),
  createUe: (payload) => api.post("/admin/ues", payload),

  matieres: (ueId) => api.get(`/admin/matieres?ueId=${ueId}`),
  createMatiere: (payload) => api.post("/admin/matieres", payload),
  affecterEnseignant: (matiereId, payload) => api.post(`/admin/matieres/${matiereId}/affecter-enseignant`, payload),

  inscrire: (payload) => api.post("/admin/inscriptions", payload),
  historiqueInscriptions: (etudiantId) => api.get(`/admin/inscriptions/etudiant/${etudiantId}`),

  // Bulletins
  genererBulletin: (payload) => api.post("/bulletins/generer", payload),
  publierBulletin: (id) => api.post(`/bulletins/${id}/publier`),
  bulletinsEtudiant: (etudiantId) => api.get(`/bulletins/etudiant/${etudiantId}`),

  // Statistiques
  statistiques: () => api.get("/admin/statistiques"),
};
