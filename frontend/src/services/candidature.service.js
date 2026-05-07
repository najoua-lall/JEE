import api from "../api/axios";

const API = "/candidatures";

const postuler = (offreId, formData) =>
  api.post(`${API}/postuler/${offreId}`, formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });

const getMesCandidatures = () =>
  api.get(`${API}/mes-candidatures`);

const getCandidaturesParOffre = (offreId) =>
  api.get(`${API}/offre/${offreId}`);

const downloadCv = (id) =>
  api.get(`${API}/${id}/cv`, { responseType: "blob" });

const changerStatut = (id, statut) =>
  api.put(`${API}/${id}/statut`, { statut });

const annuler = (id) =>
  api.delete(`${API}/${id}`);

// ajouter dans le service existant

const accepterDetail = (candidatureId, data) => {
  // data contient: societeAccueil, dateDebut, dateFin, mode, remunere, descriptionSujet, conditionsComplementaires
  return api.post(`${API}/accepter`, {
    candidatureId,
    ...data
  });
};

const telechargerInvitation = (candidatureId) =>
  api.get(`${API}/invitation/${candidatureId}/pdf`, { responseType: "blob" });

export default {
  postuler,
  getMesCandidatures,
  getCandidaturesParOffre,
  downloadCv,
  changerStatut,   // gardé pour refuser
  annuler,
  accepterDetail,  // nouvelle
  telechargerInvitation, // nouvelle
};

