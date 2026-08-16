package tn.econstruction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "communes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Municipality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String governorate;

    @Column(nullable = false, length = 10)
    private String postalCode;

    @OneToMany(mappedBy = "municipality", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<TechnicalAgent> agents;
}
