package br.com.delta.delta_api_postgres.modules.user_property.entity;

import br.com.delta.delta_api_postgres.modules.property.entity.Property;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tb_user_property")
@Getter
@Setter
@NoArgsConstructor
public class UserProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "association_date", nullable = false, updatable = false)
    private LocalDate associationDate;

    @PrePersist
    private void prePersist() {
        if (associationDate == null) {
            associationDate = LocalDate.now();
        }
    }
}
