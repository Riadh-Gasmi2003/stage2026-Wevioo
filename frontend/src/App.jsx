import { useEffect, useState } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import Navbar from './components/Navbar'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import SecretaireDashboardPage from './pages/SecretaireDashboardPage'
import AgentDashboardPage from './pages/AgentDashboardPage'
import NewDossierPage from './pages/NewDossierPage'
import DossierDetailPage from './pages/DossierDetailPage'
import { User } from './models/User'
import { Role } from './enums/Role'

function loadUser() {
  const raw = localStorage.getItem('econstruction_user')
  return raw ? User.fromJson(JSON.parse(raw)) : null
}

export default function App() {
  const [user, setUser] = useState(loadUser())
  const { i18n } = useTranslation()

  // La préférence enregistrée sur le compte (base de données) prend le dessus
  // sur la langue du navigateur dès qu'un utilisateur est identifié.
  useEffect(() => {
    if (user?.preferredLanguage && user.preferredLanguage !== i18n.language) {
      i18n.changeLanguage(user.preferredLanguage)
    }
  }, [user])

  function handleLogin(u) {
    localStorage.setItem('econstruction_user', JSON.stringify(u))
    setUser(u)
  }

  function handleLogout() {
    localStorage.removeItem('econstruction_user')
    setUser(null)
  }

  return (
    <>
      <Navbar user={user} onLogout={handleLogout} />
      <Routes>
        <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
        <Route path="/inscription" element={<RegisterPage />} />
        <Route
          path="/dashboard"
          element={
            !user ? <Navigate to="/login" />
            : user.role === Role.GENERAL_SECRETARY ? <SecretaireDashboardPage user={user} />
            : user.role === Role.TECHNICAL_AGENT ? <AgentDashboardPage user={user} />
            : <DashboardPage user={user} />
          }
        />
        <Route
          path="/nouveau-dossier"
          element={user && user.role === Role.CITIZEN ? <NewDossierPage user={user} /> : <Navigate to="/dashboard" />}
        />
        <Route
          path="/dossiers/:id"
          element={user ? <DossierDetailPage /> : <Navigate to="/login" />}
        />
        <Route path="*" element={<Navigate to={user ? '/dashboard' : '/login'} />} />
      </Routes>
    </>
  )
}
