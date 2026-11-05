package br.com.delta.delta_api_postgres.modules.property_goal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Meta mensal de consumo (em litros) de uma instalação. Base do indicador goal_pct das views dw. */
@Entity
@Table(name = "tb_property_goal")
@Getter
@Setter
@NoArgsConstructor
public class PropertyGoal {
    @Id
    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "monthly_target_liters", nullable = false)
    private BigDecimal monthlyTargetLiters;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
