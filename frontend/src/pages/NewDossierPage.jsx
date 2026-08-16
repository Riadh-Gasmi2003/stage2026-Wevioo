import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { api } from '../api'
import { REQUIRED_DOCUMENTS } from '../documentsRequis'
import { AppConstants } from '../constants/AppConstants'

const EMPTY = {
  workDescription: '',
  cadastralReference: '',
  collectiveConstruction: false,
}

export default function NewDossierPage({ user }) {
  const [form, setForm] = useState(EMPTY)
  const [documents, setDocuments] = useState({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { t } = useTranslation()

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  function updateDocument(type, file) {
    setDocuments((d) => ({ ...d, [type]: file }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    const missing = REQUIRED_DOCUMENTS.find((d) => d.required && !documents[d.type])
    if (missing) {
      setError(t('newDossier.missingDocumentError', { label: t(`documents.${missing.type}`) }))
      return
    }

    setLoading(true)
    try {
      const formData = new FormData()
      formData.append('dossier', new Blob([JSON.stringify(form)], { type: 'application/json' }))
      Object.entries(documents).forEach(([type, file]) => {
        if (file) formData.append(type, file)
      })

      const application = await api.submitApplication(formData, user.id)
      navigate(`/dossiers/${application.id}`)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1>{t('newDossier.title')}</h1>
      <p className="subtitle">
        {t('newDossier.subtitle', { municipality: user.municipalityName || t('newDossier.defaultMunicipality') })}
      </p>

      <div className="card">
        {error && <div className="error-box">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="description">{t('newDossier.descriptionLabel')}</label>
            <textarea id="description" value={form.workDescription}
                      onChange={(e) => update('workDescription', e.target.value)} required />
          </div>

          <div className="field">
            <label htmlFor="cadastre">{t('newDossier.cadastreLabel')}</label>
            <input id="cadastre" value={form.cadastralReference}
                   onChange={(e) => update('cadastralReference', e.target.value)} required />
          </div>

          <div className="field">
            <label>
              <input type="checkbox" style={{ width: 'auto', marginRight: 8 }}
                     checked={form.collectiveConstruction}
                     onChange={(e) => update('collectiveConstruction', e.target.checked)} />
              {t('newDossier.collectiveLabel')}
            </label>
            <div className="hint">
              {t('newDossier.collectiveHint', {
                collectif: AppConstants.DELAI_COLLECTIF_JOURS,
                individuel: AppConstants.DELAI_INDIVIDUEL_JOURS,
              })}
            </div>
          </div>

          <h3>{t('newDossier.documentsTitle')}</h3>
          {REQUIRED_DOCUMENTS.map((doc) => (
            <div className="field" key={doc.type}>
              <label htmlFor={doc.type}>
                {t(`documents.${doc.type}`)} {doc.required && <span style={{ color: 'var(--red)' }}>*</span>}
              </label>
              <input id={doc.type} type="file"
                     onChange={(e) => updateDocument(doc.type, e.target.files[0])}
                     required={doc.required} />
            </div>
          ))}

          <button className="primary" type="submit" disabled={loading}>
            {loading ? t('newDossier.submitting') : t('newDossier.submit')}
          </button>
        </form>
      </div>
    </div>
  )
}
