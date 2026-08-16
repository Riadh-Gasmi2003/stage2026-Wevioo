import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { api } from '../api'
import StatusBadge from '../components/StatusBadge'

export default function DashboardPage({ user }) {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { t } = useTranslation()

  useEffect(() => {
    api.getMyApplications(user.id)
      .then(setApplications)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [user.id])

  return (
    <div className="page wide">
      <h1>{t('dashboard.title')}</h1>
      <p className="subtitle">{t('dashboard.greeting', { firstName: user.firstName })}</p>

      {error && <div className="error-box">{error}</div>}

      {loading ? (
        <p className="subtitle">{t('dashboard.loading')}</p>
      ) : applications.length === 0 ? (
        <div className="empty">
          <p>{t('dashboard.emptyText')}</p>
          <Link to="/nouveau-dossier">{t('dashboard.emptyCta')}</Link>
        </div>
      ) : (
        <div className="dossier-list">
          {applications.map((a) => (
            <Link key={a.id} to={`/dossiers/${a.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="dossier-row">
                <div>
                  <div className="num">{a.applicationNumber}</div>
                  <div className="meta">{a.municipalityName} — {a.workDescription}</div>
                </div>
                <StatusBadge status={a.status} />
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
