import api from "../api/axios";

const API = "/competences";

const getAll = () => api.get(API);
const create = (data) => api.post(API, data);
const update = (id, data) => api.put(`${API}/${id}`, data);
const remove = (id) => api.delete(`${API}/${id}`);

export default { getAll, create, update, remove };