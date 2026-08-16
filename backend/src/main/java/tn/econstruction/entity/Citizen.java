package tn.econstruction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "citoyens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Citizen extends User {

    @Column(length = 500)
    private String address;

    @Column(length = 50)
    private String taxRegistrationNumber;   // matricule fiscal (facultatif pour promoteurs)

    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<PermitApplication> applications;
}
