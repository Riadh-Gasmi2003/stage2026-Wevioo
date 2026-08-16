package tn.econstruction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.econstruction.enums.Role;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String cin;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    /** Langue préférée de l'utilisateur ("fr" ou "ar"), mémorisée d'une connexion à l'autre. */
    @Column(nullable = false, length = 5)
    private String preferredLanguage = "fr";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commune_id", nullable = false)
    private Municipality municipality;
}
