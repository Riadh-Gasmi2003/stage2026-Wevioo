package tn.econstruction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "permis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingPermit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String permitNumber;          // ex: PERMIS-TUNIS-2026-000001

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expirationDate;     // issueDate + 3 ans (CATU)

    private Double authorizedArea;

    @Column(length = 500)
    private String specialConditions;

    @Column(length = 500)
    private String pdfPathFr;           // chemin du PDF généré par JasperReports (version française)

    @Column(length = 500)
    private String pdfPathAr;           // chemin du PDF généré par JasperReports (version arabe)

    // true = certificat non-opposition (accord tacite art.74 CATU)
    @Column(nullable = false)
    private boolean tacitApproval = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private PermitApplication permitApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaire_id")
    private GeneralSecretary generalSecretary;
}
