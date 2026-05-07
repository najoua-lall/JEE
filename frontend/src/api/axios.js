import axios from "axios";

const api = axios.create({
  baseURL: "/api",
});

api.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (user && user.accessToken) {
      // On injecte le jeton pour que le Backend nous reconnaisse
      config.headers["Authorization"] = 'Bearer ' + user.accessToken;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;