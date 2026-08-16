import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api'
import StatusBadge from '../components/StatusBadge'
import { ApplicationStatus } from '../enums/ApplicationStatus'

const VISIBLE_STATUSES = [ApplicationStatus.FORWARDED_TO_SECRETARY, ApplicationStatus.APPROVED, ApplicationStatus.REJECTED]
const PENDING = [ApplicationStatus.FORWARDED_TO_SECRETARY]

export default function SecretaireDashboardPage({ user }) {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)
  const { t } = useTranslation()

  function load() {
    setLoading(true)
    api.getMunicipalityApplications(user.municipalityId)
      .then(setApplications)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [user.municipalityId])

  async function approve(id) {
    setBusyId(id)
    setError('')
    try {
      await api.approveApplication(id, user.id)
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusyId(null)
    }
  }

  async function reject(id) {
    const reason = window.prompt(t('secretaireDashboard.rejectPrompt'))
    if (!reason) return
    setBusyId(id)
    setError('')
    try {
      await api.rejectApplication(id, reason)
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusyId(null)
    }
  }

  const visible = applications.filter((a) => VISIBLE_STATUSES.includes(a.status))
  const pending = visible.filter((a) => PENDING.includes(a.status))
  const processed = visible.filter((a) => !PENDING.includes(a.status))

  return (
    <div className="page wide">
      <h1>{t('secretaireDashboard.title')}</h1>
      <p className="subtitle">
        {t('secretaireDashboard.greeting', { firstName: user.firstName, municipality: user.municipalityName })}
      </p>

      {error && <div className="error-box">{error}</div>}

      {loading ? (
        <p className="subtitle">{t('secretaireDashboard.loading')}</p>
      ) : visible.length === 0 ? (
        <div className="empty"><p>{t('secretaireDashboard.empty')}</p></div>
      ) : (
        <>
          <h3>{t('secretaireDashboard.pendingTitle', { count: pending.length })}</h3>
          <div className="dossier-list">
            {pending.map((a) => (
              <div className="dossier-row" key={a.id} style={{ alignItems: 'flex-start' }}>
                <div>
                  <div className="num">{a.applicationNumber} — {a.citizenFirstName} {a.citizenLastName}</div>
                  <div className="meta">{a.workDescription}</div>
                  <div className="meta">{t('secretaireDashboard.cadastralAndDelay', { ref: a.cadastralReference, days: a.remainingDays })}</div>
                  <StatusBadge status={a.status} />
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button className="primary" disabled={busyId === a.id} onClick={() => approve(a.id)}>
                    {busyId === a.id ? t('secretaireDashboard.approving') : t('secretaireDashboard.approve')}
                  </button>
                  <button disabled={busyId === a.id} onClick={() => reject(a.id)}>{t('secretaireDashboard.reject')}</button>
                </div>
              </div>
            ))}
            {pending.length === 0 && <p className="subtitle">{t('secretaireDashboard.nothingPending')}</p>}
          </div>

          <h3 style={{ marginTop: 32 }}>{t('secretaireDashboard.processedTitle', { count: processed.length })}</h3>
          <div className="dossier-list">
            {processed.map((a) => (
              <div className="dossier-row" key={a.id}>
                <div>
                  <div className="num">{a.applicationNumber} — {a.citizenFirstName} {a.citizenLastName}</div>
                  <div className="meta">{a.workDescription}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <StatusBadge status={a.status} />
                  {a.permitGenerated && (
                    <span>
                      {t('secretaireDashboard.viewPdf')}{' '}
                      <a href={api.permitPdfUrl(a.id, 'fr')} target="_blank" rel="noreferrer">{t('dossierDetail.downloadFr')}</a>
                      {' · '}
                      <a href={api.permitPdfUrl(a.id, 'ar')} target="_blank" rel="noreferrer">{t('dossierDetail.downloadAr')}</a>
                    </span>
                  )}
                  {a.rejectionNoticeGenerated && (
                    <a href={api.rejectionNoticePdfUrl(a.id)} target="_blank" rel="noreferrer">{t('secretaireDashboard.viewRejection')}</a>
                  )}
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
