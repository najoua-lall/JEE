import api from "../api/axios";

const API = "/offres";

const getAllOffres = () => api.get(API);
const getOffre = (id) => api.get(`${API}/${id}`);
const createOffre = (data) => api.post(API, data);
const updateOffre = (id, data) => api.put(`${API}/${id}`, data);
const deleteOffre = (id) => api.delete(`${API}/${id}`);

// Pour les étudiants : toutes les offres de la plateforme
const searchOffres = (params) => api.get(`${API}/search`, { params });

// Pour les recruteurs : uniquement leurs propres offres
const searchMesOffres = (params) => api.get(`${API}/mes-offres`, { params });

// Associer compétences à une offre
const associerCompetences = (id, competenceIds) =>
  api.put(`${API}/${id}/competences`, competenceIds);

export default {
  getAllOffres,
  getOffre,
  createOffre,
  updateOffre,
  deleteOffre,
  searchOffres,
  searchMesOffres,
  associerCompetences,
};