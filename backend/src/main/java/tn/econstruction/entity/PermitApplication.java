package tn.econstruction.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.econstruction.constant.AppConstants;
import tn.econstruction.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermitApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String applicationNumber;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(nullable = false, length = 1000)
    private String workDescription;

    // Renseignés par le service technique pendant l'instruction, pas à la soumission
    private Double floorArea;
    private Integer numberOfFloors;

    // Référence cadastrale — vérifiée contre SIG PAU (RM-005)
    @Column(nullable = false, length = 100)
    private String cadastralReference;

    // Délais légaux CATU
    @Column(nullable = false)
    private LocalDate legalDeadline; // J+30 ou J+60 selon type

    @Column(nullable = false)
    private boolean collectiveConstruction = false; // true = 60j, false = 30j

    // RM-007 : max 2 demandes de compléments
    @Column(nullable = false)
    private int requestCount = 0;

    // Motif de refus (RM-012 : refus motivé obligatoire)
    @Column(length = 2000)
    private String rejectionReason;

    // Chemin du PDF d'avis de refus généré par JasperReports (pas d'envoi), une version par langue
    @Column(length = 500)
    private String rejectionNoticePdfPathFr;

    @Column(length = 500)
    private String rejectionNoticePdfPathAr;

    @Column(length = 2000)
    private String agentComment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commune_id", nullable = false)
    private Municipality municipality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private TechnicalAgent reviewingAgent;

    @OneToMany(mappedBy = "permitApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Document> documents = new ArrayList<>();

    @OneToOne(mappedBy = "permitApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BuildingPermit buildingPermit;

    public int calculateRemainingDays() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), legalDeadline);
    }

    public boolean isDeadlineExpired() {
        return LocalDate.now().isAfter(legalDeadline);
    }

    public boolean canRequestAdditionalDocuments() {
        return requestCount < AppConstants.NOMBRE_MAX_RELANCES;
    }
}
