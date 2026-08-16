package tn.econstruction.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secretaires_generaux")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GeneralSecretary extends User {

    @Column(length = 100)
    private String decreeNumber;
}
