import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { api } from '../api'

export default function LoginPage({ onLogin }) {
  const [cin, setCin] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { t } = useTranslation()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await api.login(cin, password)
      onLogin(user)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1>{t('login.title')}</h1>
      <p className="subtitle">{t('login.subtitle')}</p>

      <div className="card">
        {error && <div className="error-box">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="cin">{t('login.cinLabel')}</label>
            <input id="cin" value={cin} onChange={(e) => setCin(e.target.value)} required />
          </div>
          <div className="field">
            <label htmlFor="mdp">{t('login.passwordLabel')}</label>
            <input id="mdp" type="password" value={password}
                   onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button className="primary" type="submit" disabled={loading}>
            {loading ? t('login.submitting') : t('login.submit')}
          </button>
        </form>
      </div>

      <p className="subtitle">
        {t('login.noAccount')} <Link to="/inscription">{t('login.createAccount')}</Link>
      </p>
    </div>
  )
}
