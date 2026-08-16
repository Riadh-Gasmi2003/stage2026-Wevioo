import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import fr from './locales/fr.json'
import ar from './locales/ar.json'

// Langues RTL gérées par l'application. Pour l'instant seul l'arabe,
// mais la liste est isolée ici pour pouvoir en ajouter d'autres facilement.
const RTL_LANGUAGES = ['ar']

export function applyDirection(lng) {
  const dir = RTL_LANGUAGES.includes(lng) ? 'rtl' : 'ltr'
  document.documentElement.setAttribute('dir', dir)
  document.documentElement.setAttribute('lang', lng)
}

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      fr: { translation: fr },
      ar: { translation: ar },
    },
    fallbackLng: 'fr',
    supportedLngs: ['fr', 'ar'],
    interpolation: { escapeValue: false },
    detection: {
      // Ordre de détection : préférence déjà mémorisée par le navigateur en premier,
      // puis langue du navigateur. La préférence enregistrée sur le compte utilisateur
      // (en base de données) prend le dessus dès la connexion — voir App.jsx.
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
      lookupLocalStorage: 'econstruction_lang',
    },
  })

applyDirection(i18n.resolvedLanguage || i18n.language)

i18n.on('languageChanged', (lng) => {
  applyDirection(lng)
})

export default i18n
