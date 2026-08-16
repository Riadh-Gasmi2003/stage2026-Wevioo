/**
 * Modèle représentant l'utilisateur connecté (citoyen, agent technique
 * ou secrétaire général), tel que renvoyé par POST /api/auth/login.
 */
export class User {
  constructor(data = {}) {
    this.id = data.id;
    this.cin = data.cin;
    this.lastName = data.lastName;
    this.firstName = data.firstName;
    this.email = data.email;
    this.role = data.role;
    this.municipalityId = data.municipalityId;
    this.municipalityName = data.municipalityName;
    this.preferredLanguage = data.preferredLanguage || 'fr';
  }

  static fromJson(data) {
    return data ? new User(data) : null;
  }
}
