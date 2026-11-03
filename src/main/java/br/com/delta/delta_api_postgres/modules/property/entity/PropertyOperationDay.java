package br.com.delta.delta_api_postgres.modules.property.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dia da semana em que um imóvel comercial funciona (referência a tb_day_of_week). Usado só para leitura. */
@Entity
@Table(name = "tb_property_operation_day")
@Getter
@Setter
@NoArgsConstructor
public class PropertyOperationDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "day_of_week_id", nullable = false)
    private Integer dayOfWeekId;
}
