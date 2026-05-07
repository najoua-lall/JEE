import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Register             from "./pages/Register";
import Login                from "./pages/Login";
import OffresList           from "./pages/OffresList";
import OffreForm            from "./pages/OffreForm";
import PrivateRoute         from "./pages/PrivateRoute";
import MesCandidatures      from "./pages/MesCandidatures";
import GererCandidatures    from "./pages/GererCandidatures";
import PostulerForm         from "./pages/PostulerForm";
import StatistiquesDashboard from "./pages/StatistiquesDashboard";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/offres" replace />} />

        <Route path="/login"    element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/offres" element={
          <PrivateRoute><OffresList /></PrivateRoute>
        }/>
        <Route path="/offres/create" element={
          <PrivateRoute><OffreForm /></PrivateRoute>
        }/>
        <Route path="/offres/edit/:id" element={
          <PrivateRoute><OffreForm /></PrivateRoute>
        }/>
        <Route path="/offres/:offreId/postuler" element={
          <PrivateRoute><PostulerForm /></PrivateRoute>
        }/>
        <Route path="/mes-candidatures" element={
          <PrivateRoute><MesCandidatures /></PrivateRoute>
        }/>
        <Route path="/offres/:offreId/candidatures" element={
          <PrivateRoute><GererCandidatures /></PrivateRoute>
        }/>
        <Route path="/statistiques" element={
          <PrivateRoute><StatistiquesDashboard /></PrivateRoute>
        }/>
      </Routes>
    </Router>
  );
}

export default App;