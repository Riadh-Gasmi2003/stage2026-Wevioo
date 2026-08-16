import { PermitApplication } from './models/PermitApplication';
import { User } from './models/User';
import { Municipality } from './models/Municipality';
import i18n from './i18n';

const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  let res;
  // La langue active (fr/ar) est envoyée au serveur via Accept-Language à chaque
  // appel, pour que les messages de validation et d'erreur métier renvoyés par le
  // backend soient déjà dans la bonne langue (voir I18nConfig côté serveur).
  const baseHeaders = options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' };
  const headers = {
    ...baseHeaders,
    'Accept-Language': i18n.resolvedLanguage || i18n.language || 'fr',
    ...(options.headers || {}),
  };
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers,
    });
  } catch (_) {
    throw new Error('Impossible de contacter le serveur. Vérifiez qu\'il est démarré (http://localhost:8080).');
  }

  if (!res.ok) {
    let message = `Erreur ${res.status}`;
    try {
      const data = await res.json();
      message = data.message || (data.errors ? data.errors.join(', ') : message);
    } catch (_) {
      // réponse non-JSON, on garde le message générique
    }
    throw new Error(message);
  }

  const contentType = res.headers.get('content-type') || '';
  return contentType.includes('application/json') ? res.json() : res.text();
}

export const api = {
  login: (cin, password) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify({ cin, password }) })
      .then(User.fromJson),

  register: (dto) =>
    request('/auth/register', { method: 'POST', body: JSON.stringify(dto) }),

  getMunicipalities: () => request('/municipalities').then(Municipality.fromJsonList),

  getMyApplications: (citizenId) =>
    request(`/permit-applications/citizen/${citizenId}`).then(PermitApplication.fromJsonList),

  getApplication: (id) => request(`/permit-applications/${id}`).then(PermitApplication.fromJson),

  submitApplication: (formData, citizenId) =>
    request(`/permit-applications?citizenId=${citizenId}`, { method: 'POST', body: formData })
      .then(PermitApplication.fromJson),

  getMunicipalityApplications: (municipalityId) =>
    request(`/permit-applications/municipality/${municipalityId}`).then(PermitApplication.fromJsonList),

  markAsCompliant: (id, agentId) =>
    request(`/permit-applications/${id}/compliance?agentId=${agentId}`, { method: 'POST' })
      .then(PermitApplication.fromJson),

  requestAdditionalDocuments: (id, comment) =>
    request(`/permit-applications/${id}/request-additional-documents`, { method: 'POST', body: JSON.stringify({ comment }) })
      .then(PermitApplication.fromJson),

  provideAdditionalDocuments: (id, formData) =>
    request(`/permit-applications/${id}/additional-documents`, { method: 'PUT', body: formData })
      .then(PermitApplication.fromJson),

  approveApplication: (id, secretaryId) =>
    request(`/permit-applications/${id}/approve?secretaryId=${secretaryId}`, { method: 'POST' })
      .then(PermitApplication.fromJson),

  rejectApplication: (id, reason) =>
    request(`/permit-applications/${id}/reject`, { method: 'POST', body: JSON.stringify({ reason }) })
      .then(PermitApplication.fromJson),

  updateLanguage: (userId, language) =>
    request(`/users/${userId}/language`, { method: 'PATCH', body: JSON.stringify({ language }) }),

  permitPdfUrl: (applicationId, lang = 'fr') =>
    `${BASE_URL}/building-permits/application/${applicationId}/pdf?lang=${lang}`,

  rejectionNoticePdfUrl: (applicationId, lang = 'fr') =>
    `${BASE_URL}/permit-applications/${applicationId}/rejection-notice/pdf?lang=${lang}`,
};
