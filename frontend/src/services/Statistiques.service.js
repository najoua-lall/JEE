import api from "../api/axios";

const getStatistiques = () => api.get("/statistiques");

export default { getStatistiques };