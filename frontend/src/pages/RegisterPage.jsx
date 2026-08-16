import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { api } from '../api'

const EMPTY = { cin: '', lastName: '', firstName: '', email: '', password: '', phone: '', address: '', municipalityId: '' }

export default function RegisterPage() {
  const [form, setForm] = useState(EMPTY)
  const [municipalities, setMunicipalities] = useState([])
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { t } = useTranslation()

  useEffect(() => {
    api.getMunicipalities().then(setMunicipalities).catch((err) => setError(err.message))
  }, [])

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    if (!form.municipalityId) {
      setError(t('register.chooseMunicipalityError'))
      return
    }

    setLoading(true)
    try {
      const message = await api.register({ ...form, municipalityId: Number(form.municipalityId) })
      setSuccess(message)
      setTimeout(() => navigate('/login'), 1500)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1>{t('register.title')}</h1>
      <p className="subtitle">{t('register.subtitle')}</p>

      <div className="card">
        {error && <div className="error-box">{error}</div>}
        {success && <div className="error-box" style={{ background: '#E4F2E9', borderColor: '#B7DDC3', color: '#2E7D4F' }}>{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="field">
              <label htmlFor="nom">{t('register.lastNameLabel')}</label>
              <input id="nom" value={form.lastName} onChange={(e) => update('lastName', e.target.value)} required />
            </div>
            <div className="field">
              <label htmlFor="prenom">{t('register.firstNameLabel')}</label>
              <input id="prenom" value={form.firstName} onChange={(e) => update('firstName', e.target.value)} required />
            </div>
          </div>

          <div className="field">
            <label htmlFor="cin">{t('register.cinLabel')}</label>
            <input id="cin" value={form.cin} onChange={(e) => update('cin', e.target.value)} required />
          </div>

          <div className="field">
            <label htmlFor="email">{t('register.emailLabel')}</label>
            <input id="email" type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />
          </div>

          <div className="field">
            <label htmlFor="tel">{t('register.phoneLabel')}</label>
            <input id="tel" value={form.phone} onChange={(e) => update('phone', e.target.value)} />
          </div>

          <div className="field">
            <label htmlFor="adresse">{t('register.addressLabel')}</label>
            <input id="adresse" value={form.address} onChange={(e) => update('address', e.target.value)} />
          </div>

          <div className="field">
            <label htmlFor="commune">{t('register.municipalityLabel')}</label>
            <select id="commune" value={form.municipalityId} onChange={(e) => update('municipalityId', e.target.value)} required>
              <option value="">{t('register.municipalityPlaceholder')}</option>
              {municipalities.map((m) => (
                <option key={m.id} value={m.id}>{m.name} ({m.governorate})</option>
              ))}
            </select>
            <div className="hint">{t('register.municipalityHint')}</div>
          </div>

          <div className="field">
            <label htmlFor="mdp">{t('register.passwordLabel')}</label>
            <input id="mdp" type="password" value={form.password}
                   onChange={(e) => update('password', e.target.value)} required />
          </div>

          <button className="primary" type="submit" disabled={loading}>
            {loading ? t('register.submitting') : t('register.submit')}
          </button>
        </form>
      </div>

      <p className="subtitle">
        {t('register.alreadyRegistered')} <Link to="/login">{t('register.login')}</Link>
      </p>
    </div>
  )
}
