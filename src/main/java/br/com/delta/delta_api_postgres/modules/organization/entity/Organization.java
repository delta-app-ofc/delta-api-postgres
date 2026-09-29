package br.com.delta.delta_api_postgres.modules.organization.entity;

import br.com.delta.delta_api_postgres.modules.organization.enums.OrganizationSegment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tb_organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "corporate_name", nullable = false, length = 150)
    private String corporateName;

    @Column(name = "trade_name", nullable = false, length = 150)
    private String tradeName;

    @Column(nullable = false, length = 14, unique = true)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_segment", nullable = false, length = 20)
    private OrganizationSegment businessSegment;

    @Column(name = "declared_unit_count")
    private Integer declaredUnitCount;

    @Column(name = "registration_date", nullable = false, updatable = false)
    private LocalDate registrationDate;

    @PrePersist
    private void prePersist() {
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
    }
}
