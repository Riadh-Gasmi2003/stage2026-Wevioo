package tn.econstruction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.econstruction.entity.Municipality;

import java.util.List;
import java.util.Optional;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, Long> {

    Optional<Municipality> findByNameAndGovernorate(String name, String governorate);

    List<Municipality> findByGovernorate(String governorate);
}
