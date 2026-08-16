/**
 * Modèle représentant une commune, tel que renvoyé par GET /api/municipalities.
 */
export class Municipality {
  constructor(data = {}) {
    this.id = data.id;
    this.name = data.name;
    this.governorate = data.governorate;
    this.postalCode = data.postalCode;
  }

  static fromJson(data) {
    return data ? new Municipality(data) : null;
  }

  static fromJsonList(list) {
    return (list || []).map((item) => new Municipality(item));
  }
}
