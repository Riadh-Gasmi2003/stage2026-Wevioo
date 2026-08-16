package tn.econstruction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.econstruction.enums.DocumentType;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;     // chemin sur le serveur (ex: uploads/DOS-2026-000001/plan.pdf)

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    @Column(nullable = false)
    private boolean compliant = false; // validé par l'agent technique

    @Column(length = 500)
    private String comment;       // remarque de l'agent si non conforme

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private PermitApplication permitApplication;
}
