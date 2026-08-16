import { useTranslation } from 'react-i18next'

const TONE_BY_STATUS = {
  SUBMITTED: 'wait',
  UNDER_REVIEW: 'wait',
  ADDITIONAL_DOCUMENTS_REQUIRED: 'wait',
  ADDITIONAL_DOCUMENTS_PROVIDED: 'wait',
  FORWARDED_TO_SECRETARY: 'wait',
  APPROVED: 'ok',
  REJECTED: 'no',
  CLOSED_WITHOUT_ACTION: 'no',
  TACIT_APPROVAL: 'ok',
}

export default function StatusBadge({ status }) {
  const { t } = useTranslation()
  const tone = TONE_BY_STATUS[status] || 'wait'
  return <span className={`badge ${tone}`}>{t(`status.${status}`, status)}</span>
}
