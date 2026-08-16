package tn.econstruction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.econstruction.entity.BuildingPermit;

import java.util.Optional;

@Repository
public interface BuildingPermitRepository extends JpaRepository<BuildingPermit, Long> {

    Optional<BuildingPermit> findByPermitNumber(String permitNumber);

    Optional<BuildingPermit> findByPermitApplicationId(Long applicationId);
}
