import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { api } from '../api'
import { REQUIRED_DOCUMENTS } from '../documentsRequis'
import StatusBadge from '../components/StatusBadge'
import { ApplicationStatus } from '../enums/ApplicationStatus'
import { AppConstants } from '../constants/AppConstants'

const DATE_LOCALE_BY_LANG = { fr: 'fr-FR', ar: 'ar-TN' }

export default function DossierDetailPage() {
  const { id } = useParams()
  const [application, setApplication] = useState(null)
  const [error, setError] = useState('')
  const [documents, setDocuments] = useState({})
  const [sending, setSending] = useState(false)
  const [sendError, setSendError] = useState('')
  const { t, i18n } = useTranslation()

  function load() {
    api.getApplication(id).then(setApplication).catch((err) => setError(err.message))
  }

  useEffect(load, [id])

  function updateDocument(type, file) {
    setDocuments((d) => ({ ...d, [type]: file }))
  }

  async function sendAdditionalDocuments(e) {
    e.preventDefault()
    setSendError('')

    const files = Object.entries(documents).filter(([, file]) => file)
    if (files.length === 0) {
      setSendError(t('dossierDetail.missingDocumentError'))
      return
    }

    setSending(true)
    try {
      const formData = new FormData()
      files.forEach(([type, file]) => formData.append(type, file))
      await api.provideAdditionalDocuments(id, formData)
      setDocuments({})
      load()
    } catch (err) {
      setSendError(err.message)
    } finally {
      setSending(false)
    }
  }

  if (error) return <div className="page"><div className="error-box">{error}</div></div>
  if (!application) return <div className="page"><p className="subtitle">{t('dossierDetail.loading')}</p></div>

  const dateLocale = DATE_LOCALE_BY_LANG[i18n.language] || 'fr-FR'

  return (
    <div className="page">
      <Link className="top-link" to="/dashboard">{t('common.back')}</Link>

      <h1>{application.applicationNumber}</h1>
      <p className="subtitle">
        {t('dossierDetail.filedOn', { date: new Date(application.submissionDate).toLocaleDateString(dateLocale) })}
        {' — '}{application.municipalityName} ({application.governorate})
      </p>

      <div className="card">
        <h2>{t('dossierDetail.statusTitle')}</h2>
        <StatusBadge status={application.status} />

        {application.status === ApplicationStatus.REJECTED && application.rejectionReason && (
          <div className="motif-box">
            <strong>{t('dossierDetail.rejectionReasonLabel')}</strong>
            <p style={{ margin: '6px 0 0' }}>{application.rejectionReason}</p>
            <p className="hint" style={{ marginTop: 8 }}>
              {t('dossierDetail.appealHint', { jours: AppConstants.DELAI_RECOURS_JOURS })}
            </p>
            {application.rejectionNoticeGenerated && (
              <p className="hint" style={{ marginTop: 8 }}>
                {t('dossierDetail.downloadRejection')}{' '}
                <a href={api.rejectionNoticePdfUrl(application.id, 'fr')} target="_blank" rel="noreferrer">{t('dossierDetail.downloadFr')}</a>
                {' · '}
                <a href={api.rejectionNoticePdfUrl(application.id, 'ar')} target="_blank" rel="noreferrer">{t('dossierDetail.downloadAr')}</a>
              </p>
            )}
          </div>
        )}

        {application.status === ApplicationStatus.ADDITIONAL_DOCUMENTS_REQUIRED && application.agentComment && (
          <div className="motif-box" style={{ background: '#FBF0DE', borderColor: '#EAD3A0' }}>
            <strong style={{ color: 'var(--amber)' }}>{t('dossierDetail.additionalRequestedLabel')}</strong>
            <p style={{ margin: '6px 0 0' }}>{application.agentComment}</p>
          </div>
        )}

        {application.status === ApplicationStatus.ADDITIONAL_DOCUMENTS_REQUIRED && (
          <div style={{ marginTop: 16 }}>
            <h2>{t('dossierDetail.provideAdditionalTitle')}</h2>
            {sendError && <div className="error-box">{sendError}</div>}
            <form onSubmit={sendAdditionalDocuments}>
              {REQUIRED_DOCUMENTS.map((doc) => (
                <div className="field" key={doc.type}>
                  <label htmlFor={doc.type}>{t(`documents.${doc.type}`)}</label>
                  <input id={doc.type} type="file"
                         onChange={(e) => updateDocument(doc.type, e.target.files[0])} />
                </div>
              ))}
              <button className="primary" type="submit" disabled={sending}>
                {sending ? t('dossierDetail.sending') : t('dossierDetail.send')}
              </button>
            </form>
          </div>
        )}

        {application.permitGenerated && (
          <p className="hint" style={{ marginTop: 12 }}>
            {t('dossierDetail.permitGeneratedText')}{' '}
            <a href={api.permitPdfUrl(application.id, 'fr')} target="_blank" rel="noreferrer">{t('dossierDetail.downloadFr')}</a>
            {' · '}
            <a href={api.permitPdfUrl(application.id, 'ar')} target="_blank" rel="noreferrer">{t('dossierDetail.downloadAr')}</a>
          </p>
        )}
      </div>

      <div className="card">
        <h2>{t('dossierDetail.detailsTitle')}</h2>
        <p><strong>{t('dossierDetail.descriptionLabel')}</strong> {application.workDescription}</p>
        <p><strong>{t('dossierDetail.cadastralReferenceLabel')}</strong> {application.cadastralReference}</p>
        {application.floorArea != null && (
          <p><strong>{t('dossierDetail.floorAreaLabel')}</strong> {application.floorArea} {t('dossierDetail.floorAreaUnit')}</p>
        )}
        {application.numberOfFloors != null && (
          <p><strong>{t('dossierDetail.numberOfFloorsLabel')}</strong> {application.numberOfFloors}</p>
        )}
        <p><strong>{t('dossierDetail.remainingDaysLabel')}</strong> {application.remainingDays} {t('dossierDetail.remainingDaysUnit')}</p>
        {application.requestCount > 0 && (
          <p><strong>{t('dossierDetail.requestCountLabel')}</strong> {application.requestCount} / {AppConstants.NOMBRE_MAX_RELANCES}</p>
        )}
      </div>
    </div>
  )
}
