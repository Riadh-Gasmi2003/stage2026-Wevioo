package tn.econstruction.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agents_techniques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TechnicalAgent extends User {

    @Column(nullable = false, unique = true)
    private String agentNumber;
}
