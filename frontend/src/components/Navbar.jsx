import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Role } from '../enums/Role'
import { api } from '../api'

const DASHBOARD_KEY_BY_ROLE = {
  [Role.CITIZEN]: 'navbar.myApplications.citizen',
  [Role.TECHNICAL_AGENT]: 'navbar.myApplications.technicalAgent',
  [Role.GENERAL_SECRETARY]: 'navbar.myApplications.generalSecretary',
}

const LANGUAGES = [
  { code: 'fr', label: 'FR' },
  { code: 'ar', label: 'عربي' },
]

export default function Navbar({ user, onLogout }) {
  const navigate = useNavigate()
  const { t, i18n } = useTranslation()

  function handleLogout() {
    onLogout()
    navigate('/login')
  }

  async function handleChangeLanguage(code) {
    if (code === i18n.language) return
    await i18n.changeLanguage(code)
    // La préférence est mémorisée par le navigateur (via i18next-browser-languagedetector)
    // et, si l'utilisateur est connecté, sur son compte en base de données.
    if (user?.id) {
      try {
        await api.updateLanguage(user.id, code)
      } catch (_) {
        // Non bloquant pour l'affichage : la langue change côté écran même si
        // l'enregistrement en base échoue (ex. serveur momentanément indisponible).
      }
    }
  }

  return (
    <div className="topbar">
      <div className="brand">
        {t('navbar.brand')}
        <small>{t('navbar.brandSubtitle')}</small>
      </div>
      <nav>
        {user ? (
          <>
            <Link to="/dashboard">{t(DASHBOARD_KEY_BY_ROLE[user.role]) || t('navbar.myApplications.citizen')}</Link>
            {user.role === Role.CITIZEN && <Link to="/nouveau-dossier">{t('navbar.newApplication')}</Link>}
            <button className="link" onClick={handleLogout}>
              {t('navbar.logout', { firstName: user.firstName })}
            </button>
          </>
        ) : (
          <>
            <Link to="/login">{t('navbar.login')}</Link>
            <Link to="/inscription">{t('navbar.register')}</Link>
          </>
        )}
        <div className="lang-switch" role="group" aria-label="Langue / اللغة">
          {LANGUAGES.map(({ code, label }) => (
            <button
              key={code}
              type="button"
              className={code === i18n.language ? 'active' : ''}
              onClick={() => handleChangeLanguage(code)}
            >
              {label}
            </button>
          ))}
        </div>
      </nav>
    </div>
  )
}
