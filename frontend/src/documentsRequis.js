import { DocumentType } from './enums/DocumentType'

// Les libellés affichés à l'écran sont traduits à l'usage via i18next
// (clés "documents.<TYPE>" dans src/i18n/locales/*.json) ; ce fichier ne
// conserve que la structure métier (type de document + caractère obligatoire).
export const REQUIRED_DOCUMENTS = [
  { type: DocumentType.OFFICIAL_FORM, required: true },
  { type: DocumentType.PROPERTY_TITLE, required: true },
  { type: DocumentType.LOCATION_PLAN, required: true },
  { type: DocumentType.SITE_PLAN, required: true },
  { type: DocumentType.ARCHITECTURAL_PLANS, required: true },
  { type: DocumentType.DESCRIPTIVE_NOTE, required: true },
  { type: DocumentType.TAX_RECEIPT, required: true },
  { type: DocumentType.OACA_CERTIFICATE, required: false },
  { type: DocumentType.GEOTECHNICAL_STUDY, required: false },
  { type: DocumentType.UTILITY_PROVIDERS_APPROVAL, required: false },
]
