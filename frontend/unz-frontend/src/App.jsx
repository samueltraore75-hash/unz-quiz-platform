import { Routes, Route } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import Profile from "./pages/Profile";
import NotFound from "./pages/NotFound";
import ComingSoon from "./pages/ComingSoon";

import StudentDashboard from "./pages/student/StudentDashboard";
import QuizList from "./pages/student/QuizList";
import QuizTake from "./pages/student/QuizTake";
import QuizResult from "./pages/student/QuizResult";
import Notes from "./pages/student/Notes";

import TeacherDashboard from "./pages/teacher/TeacherDashboard";
import QuizCreate from "./pages/teacher/QuizCreate";
import QuizManage from "./pages/teacher/QuizManage";
import QuizStats from "./pages/teacher/QuizStats";
import QuizNotes from "./pages/teacher/QuizNotes";
import QuestionBank from "./pages/teacher/QuestionBank";
import GradeEntry from "./pages/teacher/GradeEntry";

import AdminDashboard from "./pages/admin/AdminDashboard";
import Referential from "./pages/admin/Referential";
import Users from "./pages/admin/Users";
import Bulletins from "./pages/admin/Bulletins";
import Statistiques from "./pages/admin/Statistiques";

function Home() {
  const { user } = useAuth();
  if (user?.role === "ETUDIANT")   return <StudentDashboard />;
  if (user?.role === "ENSEIGNANT") return <TeacherDashboard />;
  if (user?.role === "ADMIN")      return <AdminDashboard />;
  return null;
}

function QuizIndex() {
  const { user } = useAuth();
  return user?.role === "ETUDIANT" ? <QuizList /> : <TeacherDashboard />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/connexion" element={<LoginPage />} />
      <Route path="/inscription" element={<RegisterPage />} />
      <Route path="/reinitialiser-mot-de-passe" element={<ResetPasswordPage />} />

      <Route path="/"       element={<ProtectedRoute><Home /></ProtectedRoute>} />
      <Route path="/profil" element={<ProtectedRoute><Profile /></ProtectedRoute>} />

      {/* Étudiant */}
      <Route path="/quiz"             element={<ProtectedRoute roles={["ETUDIANT","ENSEIGNANT"]}><QuizIndex /></ProtectedRoute>} />
      <Route path="/quiz/:id"         element={<ProtectedRoute roles={["ETUDIANT"]}><QuizTake /></ProtectedRoute>} />
      <Route path="/quiz/:id/resultat"element={<ProtectedRoute roles={["ETUDIANT"]}><QuizResult /></ProtectedRoute>} />
      <Route path="/notes"            element={<ProtectedRoute roles={["ETUDIANT"]}><Notes /></ProtectedRoute>} />

      {/* Enseignant */}
      <Route path="/quiz/nouveau"     element={<ProtectedRoute roles={["ENSEIGNANT"]}><QuizCreate /></ProtectedRoute>} />
      <Route path="/quiz/:id/gerer"   element={<ProtectedRoute roles={["ENSEIGNANT"]}><QuizManage /></ProtectedRoute>} />
      <Route path="/quiz/:id/stats"   element={<ProtectedRoute roles={["ENSEIGNANT"]}><QuizStats /></ProtectedRoute>} />
      <Route path="/quiz/:id/notes"   element={<ProtectedRoute roles={["ENSEIGNANT"]}><QuizNotes /></ProtectedRoute>} />
      <Route path="/questions"        element={<ProtectedRoute roles={["ENSEIGNANT"]}><QuestionBank /></ProtectedRoute>} />
      <Route path="/notes/saisie"     element={<ProtectedRoute roles={["ENSEIGNANT","ADMIN"]}><GradeEntry /></ProtectedRoute>} />

      {/* Admin */}
      <Route path="/referentiel"      element={<ProtectedRoute roles={["ADMIN"]}><Referential /></ProtectedRoute>} />
      <Route path="/utilisateurs"     element={<ProtectedRoute roles={["ADMIN"]}><Users /></ProtectedRoute>} />
      <Route path="/bulletins"        element={<ProtectedRoute roles={["ADMIN"]}><Bulletins /></ProtectedRoute>} />
      <Route path="/statistiques"     element={<ProtectedRoute roles={["ADMIN"]}><Statistiques /></ProtectedRoute>} />

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
