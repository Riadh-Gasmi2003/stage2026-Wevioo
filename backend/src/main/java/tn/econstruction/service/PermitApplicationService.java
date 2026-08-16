package tn.econstruction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.econstruction.constant.AppConstants;
import tn.econstruction.dto.AdditionalDocumentsRequestDTO;
import tn.econstruction.dto.PermitApplicationCreateDTO;
import tn.econstruction.dto.PermitApplicationDTO;
import tn.econstruction.dto.RejectionDTO;
import tn.econstruction.entity.*;
import tn.econstruction.exception.BusinessException;
import tn.econstruction.enums.ApplicationStatus;
import tn.econstruction.enums.DocumentType;
import tn.econstruction.mapper.PermitApplicationMapper;
import tn.econstruction.notification.PermitApplicationEmailEvent;
import tn.econstruction.notification.EmailType;
import tn.econstruction.repository.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermitApplicationService {

    private final PermitApplicationRepository permitApplicationRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final JasperReportService jasperReportService;
    private final PermitApplicationMapper permitApplicationMapper;
    private final ApplicationEventPublisher eventPublisher;

    public PermitApplicationDTO submitApplication(PermitApplicationCreateDTO dto, Long citizenId,
                                                   Map<DocumentType, MultipartFile> documents) {

        Citizen citizen = (Citizen) userRepository.findById(citizenId)
                .orElseThrow(() -> new BusinessException("error.citizen.notFound"));

        // RM-004
        if (permitApplicationRepository.existsActivePermitForParcel(dto.getCadastralReference())) {
            throw new BusinessException("error.permitApplication.activePermitExists");
        }

        int delayDays = dto.isCollectiveConstruction()
                ? AppConstants.DELAI_COLLECTIF_JOURS
                : AppConstants.DELAI_INDIVIDUEL_JOURS;

        PermitApplication application = permitApplicationMapper.toEntity(dto);
        application.setApplicationNumber(generateApplicationNumber());
        application.setSubmissionDate(LocalDateTime.now());
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setLegalDeadline(LocalDate.now().plusDays(delayDays));
        application.setCitizen(citizen);
        application.setMunicipality(citizen.getMunicipality());

        application = permitApplicationRepository.save(application);

        PermitApplication savedApplication = application;
        if (documents != null) {
            documents.forEach((type, file) -> saveDocument(savedApplication, file, type));
        }

        return permitApplicationMapper.toDTO(application);
    }

    public PermitApplicationDTO startReview(Long applicationId, Long agentId) {
        PermitApplication application = getApplicationOrThrow(applicationId);
        TechnicalAgent agent = (TechnicalAgent) userRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException("error.agent.notFound"));
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setReviewingAgent(agent);
        permitApplicationRepository.save(application);
        return permitApplicationMapper.toDTO(application);
    }

    public PermitApplicationDTO requestAdditionalDocuments(Long applicationId, AdditionalDocumentsRequestDTO dto) {
        PermitApplication application = getApplicationOrThrow(applicationId);

        if (!application.canRequestAdditionalDocuments()) {
            // RM-007 : 3ème fois -> refus automatique
            application.setStatus(ApplicationStatus.REJECTED);
            application.setRejectionReason(AppConstants.RM_007_MAX_COMPLEMENTS_ATTEINT + dto.getComment());
            permitApplicationRepository.save(application);
            generateRejectionNotice(application);
            eventPublisher.publishEvent(
                    new PermitApplicationEmailEvent(this, application.getId(), EmailType.APPLICATION_REJECTED));
            return permitApplicationMapper.toDTO(application);
        }

        application.setStatus(ApplicationStatus.ADDITIONAL_DOCUMENTS_REQUIRED);
        application.setRequestCount(application.getRequestCount() + 1);
        application.setAgentComment(dto.getComment());
        application.setLegalDeadline(
                application.getLegalDeadline().plusDays(AppConstants.DELAI_PROLONGATION_COMPLEMENT_JOURS));
        permitApplicationRepository.save(application);
        eventPublisher.publishEvent(
                new PermitApplicationEmailEvent(this, application.getId(), EmailType.ADDITIONAL_DOCUMENTS_REQUIRED));
        return permitApplicationMapper.toDTO(application);
    }

    // Agent technique : dossier conforme -> transmis au secrétaire général pour décision
    public PermitApplicationDTO markAsCompliant(Long applicationId, Long agentId) {
        PermitApplication application = getApplicationOrThrow(applicationId);
        TechnicalAgent agent = (TechnicalAgent) userRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException("error.agent.notFound"));
        application.setStatus(ApplicationStatus.FORWARDED_TO_SECRETARY);
        application.setReviewingAgent(agent);
        permitApplicationRepository.save(application);
        eventPublisher.publishEvent(
                new PermitApplicationEmailEvent(this, application.getId(), EmailType.APPLICATION_COMPLIANT));
        return permitApplicationMapper.toDTO(application);
    }

    public PermitApplicationDTO provideAdditionalDocuments(Long applicationId, Map<DocumentType, MultipartFile> documents) {
        PermitApplication application = getApplicationOrThrow(applicationId);
        if (application.getStatus() != ApplicationStatus.ADDITIONAL_DOCUMENTS_REQUIRED) {
            throw new BusinessException("error.permitApplication.notAwaitingDocuments");
        }
        if (documents != null) {
            documents.forEach((type, file) -> saveDocument(application, file, type));
        }
        application.setStatus(ApplicationStatus.ADDITIONAL_DOCUMENTS_PROVIDED);
        permitApplicationRepository.save(application);
        return permitApplicationMapper.toDTO(application);
    }

    public PermitApplicationDTO approveApplication(Long applicationId, Long secretaryId) {
        PermitApplication application = getApplicationOrThrow(applicationId);
        // Garde de cohérence : on refuse d'écraser une décision déjà prise sur ce dossier.
        if (application.getStatus() != ApplicationStatus.FORWARDED_TO_SECRETARY) {
            throw new BusinessException("error.permitApplication.notAwaitingSecretaryDecision");
        }
        application.setStatus(ApplicationStatus.APPROVED);
        permitApplicationRepository.save(application);
        return permitApplicationMapper.toDTO(application);
    }

    public PermitApplicationDTO rejectApplication(Long applicationId, RejectionDTO dto) {
        PermitApplication application = getApplicationOrThrow(applicationId);
        // RM-013 : même garde que pour l'approbation, voir commentaire ci-dessus.
        if (application.getStatus() != ApplicationStatus.FORWARDED_TO_SECRETARY) {
            throw new BusinessException("error.permitApplication.notAwaitingSecretaryDecision");
        }
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(dto.getReason());
        permitApplicationRepository.save(application);
        generateRejectionNotice(application);
        eventPublisher.publishEvent(
                new PermitApplicationEmailEvent(this, application.getId(), EmailType.APPLICATION_REJECTED));
        return permitApplicationMapper.toDTO(application);
    }

    @Transactional(readOnly = true)
    public byte[] getRejectionNoticePdf(Long applicationId, String language) {
        PermitApplication application = getApplicationOrThrow(applicationId);
        String path = "ar".equalsIgnoreCase(language) && application.getRejectionNoticePdfPathAr() != null
                ? application.getRejectionNoticePdfPathAr() : application.getRejectionNoticePdfPathFr();
        if (path == null) {
            throw new BusinessException("error.permitApplication.noRejectionNotice");
        }
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (Exception e) {
            throw new BusinessException("error.permitApplication.rejectionNoticeReadFailed");
        }
    }

    private void generateRejectionNotice(PermitApplication application) {
        JasperReportService.GeneratedDocument pdfs = jasperReportService.generateRejectionNoticePdf(application);
        application.setRejectionNoticePdfPathFr(pdfs.pathFr());
        application.setRejectionNoticePdfPathAr(pdfs.pathAr());
        permitApplicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public PermitApplicationDTO getApplicationById(Long id) {
        return permitApplicationMapper.toDTO(getApplicationOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PermitApplicationDTO> getMyApplications(Long citizenId) {
        return permitApplicationRepository.findByCitizenId(citizenId)
                .stream().map(permitApplicationMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermitApplicationDTO> getMunicipalityApplications(Long municipalityId) {
        return permitApplicationRepository.findByMunicipalityId(municipalityId)
                .stream().map(permitApplicationMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermitApplicationDTO> getApplicationsByStatus(ApplicationStatus status) {
        return permitApplicationRepository.findByStatus(status)
                .stream().map(permitApplicationMapper::toDTO).collect(Collectors.toList());
    }

    private PermitApplication getApplicationOrThrow(Long id) {
        return permitApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("error.permitApplication.notFoundWithId", id));
    }

    private String generateApplicationNumber() {
        long count = permitApplicationRepository.count() + 1;
        return String.format(AppConstants.NUMERO_DOSSIER_FORMAT, LocalDate.now().getYear(), count);
    }

    private void saveDocument(PermitApplication application, MultipartFile file, DocumentType type) {
        if (file == null || file.isEmpty()) return;
        try {
            String dir = AppConstants.UPLOAD_DIR + application.getApplicationNumber() + "/";
            Files.createDirectories(Paths.get(dir));
            String path = dir + type.name() + "_" + file.getOriginalFilename();
            Files.write(Paths.get(path), file.getBytes());

            Document doc = new Document();
            doc.setPermitApplication(application);
            doc.setType(type);
            doc.setFileName(file.getOriginalFilename());
            doc.setFilePath(path);
            doc.setSubmissionDate(LocalDateTime.now());
            doc.setCompliant(false);
            documentRepository.save(doc);
        } catch (IOException e) {
            throw new BusinessException("error.file.error", file.getOriginalFilename());
        }
    }
}
