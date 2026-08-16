package tn.econstruction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.econstruction.entity.PermitApplication;
import tn.econstruction.enums.ApplicationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermitApplicationRepository extends JpaRepository<PermitApplication, Long> {

    Optional<PermitApplication> findByApplicationNumber(String applicationNumber);

    List<PermitApplication> findByCitizenId(Long citizenId);

    List<PermitApplication> findByMunicipalityId(Long municipalityId);

    List<PermitApplication> findByStatus(ApplicationStatus status);

    List<PermitApplication> findByMunicipalityIdAndStatus(Long municipalityId, ApplicationStatus status);

    List<PermitApplication> findByReviewingAgentId(Long agentId);

    @Query("SELECT d FROM PermitApplication d WHERE d.legalDeadline = :dateAlert " +
           "AND d.status NOT IN ('APPROVED', 'REJECTED', 'CLOSED_WITHOUT_ACTION', 'TACIT_APPROVAL')")
    List<PermitApplication> findApplicationsNearingDeadline(@Param("dateAlert") LocalDate dateAlert);

    @Query("SELECT d FROM PermitApplication d WHERE d.legalDeadline < :today " +
           "AND d.status NOT IN ('APPROVED', 'REJECTED', 'CLOSED_WITHOUT_ACTION', 'TACIT_APPROVAL')")
    List<PermitApplication> findApplicationsWithExpiredDeadline(@Param("today") LocalDate today);

    @Query("SELECT COUNT(d) > 0 FROM PermitApplication d WHERE d.cadastralReference = :ref " +
           "AND d.status NOT IN ('REJECTED', 'CLOSED_WITHOUT_ACTION')")
    boolean existsActivePermitForParcel(@Param("ref") String cadastralReference);

    @Query("SELECT d.status, COUNT(d) FROM PermitApplication d WHERE d.municipality.id = :municipalityId GROUP BY d.status")
    List<Object[]> countByStatusForMunicipality(@Param("municipalityId") Long municipalityId);
}
