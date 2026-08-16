import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { api } from '../api'
import StatusBadge from '../components/StatusBadge'
import { ApplicationStatus } from '../enums/ApplicationStatus'

const TO_REVIEW = [ApplicationStatus.SUBMITTED, ApplicationStatus.ADDITIONAL_DOCUMENTS_PROVIDED]

export default function AgentDashboardPage({ user }) {
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

  async function markAsCompliant(id) {
    setBusyId(id)
    setError('')
    try {
      await api.markAsCompliant(id, user.id)
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusyId(null)
    }
  }

  async function requestAdditionalDocuments(id) {
    const comment = window.prompt(t('agentDashboard.requestDocsPrompt'))
    if (!comment) return
    setBusyId(id)
    setError('')
    try {
      await api.requestAdditionalDocuments(id, comment)
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusyId(null)
    }
  }

  const toReview = applications.filter((a) => TO_REVIEW.includes(a.status))
  const others = applications.filter((a) => !TO_REVIEW.includes(a.status))

  return (
    <div className="page wide">
      <h1>{t('agentDashboard.title')}</h1>
      <p className="subtitle">
        {t('agentDashboard.greeting', { firstName: user.firstName, municipality: user.municipalityName })}
      </p>

      {error && <div className="error-box">{error}</div>}

      {loading ? (
        <p className="subtitle">{t('agentDashboard.loading')}</p>
      ) : applications.length === 0 ? (
        <div className="empty"><p>{t('agentDashboard.empty')}</p></div>
      ) : (
        <>
          <h3>{t('agentDashboard.toReviewTitle', { count: toReview.length })}</h3>
          <div className="dossier-list">
            {toReview.map((a) => (
              <div className="dossier-row" key={a.id} style={{ alignItems: 'flex-start' }}>
                <div>
                  <div className="num">{a.applicationNumber} — {a.citizenFirstName} {a.citizenLastName}</div>
                  <div className="meta">{a.workDescription}</div>
                  <div className="meta">{t('agentDashboard.cadastralAndDelay', { ref: a.cadastralReference, days: a.remainingDays })}</div>
                  <StatusBadge status={a.status} />
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button className="primary" disabled={busyId === a.id} onClick={() => markAsCompliant(a.id)}>
                    {busyId === a.id ? t('agentDashboard.marking') : t('agentDashboard.markCompliant')}
                  </button>
                  <button disabled={busyId === a.id} onClick={() => requestAdditionalDocuments(a.id)}>{t('agentDashboard.requestDocs')}</button>
                </div>
              </div>
            ))}
            {toReview.length === 0 && <p className="subtitle">{t('agentDashboard.nothingToReview')}</p>}
          </div>

          <h3 style={{ marginTop: 32 }}>{t('agentDashboard.otherApplicationsTitle', { count: others.length })}</h3>
          <div className="dossier-list">
            {others.map((a) => (
              <div className="dossier-row" key={a.id}>
                <div>
                  <div className="num">{a.applicationNumber} — {a.citizenFirstName} {a.citizenLastName}</div>
                  <div className="meta">{a.workDescription}</div>
                </div>
                <StatusBadge status={a.status} />
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
