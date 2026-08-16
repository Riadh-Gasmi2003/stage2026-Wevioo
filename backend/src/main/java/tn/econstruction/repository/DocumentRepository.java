package tn.econstruction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.econstruction.entity.Document;
import tn.econstruction.enums.DocumentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByPermitApplicationId(Long applicationId);

    Optional<Document> findByPermitApplicationIdAndType(Long applicationId, DocumentType type);

    List<Document> findByPermitApplicationIdAndCompliant(Long applicationId, boolean compliant);
}
