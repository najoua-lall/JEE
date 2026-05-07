import React from "react";
import { Navigate } from "react-router-dom";
import AuthService from "../services/auth.service";

const PrivateRoute = ({ children }) => {
  const user = AuthService.getCurrentUser();
  return user ? children : <Navigate to="/login" replace />;
};

export default PrivateRoute;