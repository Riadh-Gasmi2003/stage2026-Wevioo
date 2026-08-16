package tn.econstruction.enums;

public enum ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    ADDITIONAL_DOCUMENTS_REQUIRED,
    ADDITIONAL_DOCUMENTS_PROVIDED,
    FORWARDED_TO_SECRETARY,  // conformité validée par l'agent technique, en attente de décision
    APPROVED,
    REJECTED,
    CLOSED_WITHOUT_ACTION,
    TACIT_APPROVAL          // art. 74 CATU — délai légal expiré sans décision
}
