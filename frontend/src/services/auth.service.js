import axios from "axios";

// Grâce au proxy dans vite.config.js, on peut utiliser /api directement
const API_URL = "/api/auth/";

const register = (username, email, password, roles) => {
  return axios.post(API_URL + "signup", {
    username,
    email,
    password,
    roles,
  });
};

const login = (username, password) => {
  return axios
    .post(API_URL + "signin", { username, password })
    .then((response) => {
      if (response.data.accessToken) {
        // On stocke l'utilisateur et son JWT dans le navigateur
        localStorage.setItem("user", JSON.stringify(response.data));
      }
      return response.data;
    });
};

const logout = () => {
  localStorage.removeItem("user");
};

const getCurrentUser = () => {
  return JSON.parse(localStorage.getItem("user"));
};

export default {
  register,
  login,
  logout,
  getCurrentUser,
};