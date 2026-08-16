/**
 * Modèle représentant un dossier de permis de bâtir tel que renvoyé par
 * l'API (/api/permit-applications/...). Fait le pendant, côté frontend, du
 * PermitApplicationDTO du backend : centralise la conversion JSON -> objet
 * utilisable par les pages.
 */
export class PermitApplication {
  constructor(data = {}) {
    this.id = data.id;
    this.applicationNumber = data.applicationNumber;
    this.submissionDate = data.submissionDate;
    this.status = data.status;
    this.workDescription = data.workDescription;
    this.floorArea = data.floorArea;
    this.numberOfFloors = data.numberOfFloors;
    this.cadastralReference = data.cadastralReference;
    this.legalDeadline = data.legalDeadline;
    this.remainingDays = data.remainingDays;
    this.requestCount = data.requestCount;
    this.rejectionReason = data.rejectionReason;
    this.rejectionNoticeGenerated = data.rejectionNoticeGenerated;
    this.agentComment = data.agentComment;
    this.municipalityName = data.municipalityName;
    this.governorate = data.governorate;
    this.citizenLastName = data.citizenLastName;
    this.citizenFirstName = data.citizenFirstName;
    this.citizenEmail = data.citizenEmail;
    this.permitGenerated = data.permitGenerated;
  }

  static fromJson(data) {
    return data ? new PermitApplication(data) : null;
  }

  static fromJsonList(list) {
    return (list || []).map((item) => new PermitApplication(item));
  }
}
