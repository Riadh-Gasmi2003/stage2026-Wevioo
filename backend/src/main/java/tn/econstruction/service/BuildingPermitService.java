package tn.econstruction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.econstruction.constant.AppConstants;
import tn.econstruction.entity.*;
import tn.econstruction.exception.BusinessException;
import tn.econstruction.enums.ApplicationStatus;
import tn.econstruction.notification.PermitApplicationEmailEvent;
import tn.econstruction.notification.EmailType;
import tn.econstruction.repository.*;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BuildingPermitService {

    private final PermitApplicationRepository permitApplicationRepository;
    private final BuildingPermitRepository buildingPermitRepository;
    private final UserRepository userRepository;
    private final JasperReportService jasperReportService;
    private final ApplicationEventPublisher eventPublisher;

    public BuildingPermit generatePermit(Long applicationId, Long secretaryId) {
        PermitApplication application = permitApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("error.permitApplication.notFoundWithId", applicationId));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("error.permit.applicationNotApproved");
        }

        GeneralSecretary secretary = (GeneralSecretary) userRepository.findById(secretaryId)
                .orElseThrow(() -> new BusinessException("error.generalSecretary.notFound"));

        BuildingPermit permit = new BuildingPermit();
        permit.setPermitNumber(generatePermitNumber(application));
        permit.setIssueDate(LocalDate.now());
        permit.setExpirationDate(LocalDate.now().plusYears(AppConstants.VALIDITE_PERMIS_ANNEES));
        permit.setAuthorizedArea(application.getFloorArea());
        permit.setPermitApplication(application);
        permit.setGeneralSecretary(secretary);
        permit.setTacitApproval(false);

        // Génération PDF via JasperReports (RM-015), en français et en arabe
        JasperReportService.GeneratedDocument pdfs = jasperReportService.generatePermitPdf(application, permit, secretary);
        permit.setPdfPathFr(pdfs.pathFr());
        permit.setPdfPathAr(pdfs.pathAr());

        permit = buildingPermitRepository.save(permit);
        application.setBuildingPermit(permit);
        permitApplicationRepository.save(application);

        eventPublisher.publishEvent(
                new PermitApplicationEmailEvent(this, application.getId(), EmailType.PERMIT_ISSUED));

        log.info("Permis {} généré pour dossier {}", permit.getPermitNumber(), application.getApplicationNumber());

        return permit;
    }

    // Certificat de non-opposition (RM-013, accord tacite art. 74 CATU)
    public BuildingPermit generateNonOppositionCertificate(Long applicationId) {
        PermitApplication application = permitApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("error.permitApplication.notFound"));

        BuildingPermit permit = new BuildingPermit();
        permit.setPermitNumber(AppConstants.PREFIXE_CERTIFICAT_NON_OPPOSITION + application.getApplicationNumber());
        permit.setIssueDate(LocalDate.now());
        permit.setExpirationDate(LocalDate.now().plusYears(AppConstants.VALIDITE_PERMIS_ANNEES));
        permit.setAuthorizedArea(application.getFloorArea());
        permit.setPermitApplication(application);
        permit.setTacitApproval(true);
        permit.setSpecialConditions(AppConstants.CONDITIONS_ACCORD_TACITE);

        JasperReportService.GeneratedDocument pdfs = jasperReportService.generateNonOppositionCertificatePdf(application, permit);
        permit.setPdfPathFr(pdfs.pathFr());
        permit.setPdfPathAr(pdfs.pathAr());

        permit = buildingPermitRepository.save(permit);
        application.setStatus(ApplicationStatus.TACIT_APPROVAL);
        application.setBuildingPermit(permit);
        permitApplicationRepository.save(application);

        eventPublisher.publishEvent(
                new PermitApplicationEmailEvent(this, application.getId(), EmailType.NON_OPPOSITION_CERTIFICATE));

        log.info("Certificat non-opposition généré pour dossier {} (art. 74 CATU)",
                application.getApplicationNumber());

        return permit;
    }

    @Transactional(readOnly = true)
    public byte[] getPermitPdf(Long applicationId, String language) {
        BuildingPermit permit = buildingPermitRepository.findByPermitApplicationId(applicationId)
                .orElseThrow(() -> new BusinessException("error.permit.notFoundForApplication", applicationId));
        String path = "ar".equalsIgnoreCase(language) && permit.getPdfPathAr() != null
                ? permit.getPdfPathAr() : permit.getPdfPathFr();
        try {
            return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
        } catch (Exception e) {
            throw new BusinessException("error.permit.pdfReadFailed");
        }
    }

    private String generatePermitNumber(PermitApplication application) {
        String municipality = application.getMunicipality().getName().toUpperCase().replace(" ", "-");
        String year = String.valueOf(LocalDate.now().getYear());
        long count = buildingPermitRepository.count() + 1;
        return String.format(AppConstants.NUMERO_PERMIS_FORMAT, municipality, year, count);
    }
}
