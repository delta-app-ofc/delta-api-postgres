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
import java.time.LocalTime;

/** Turno de funcionamento de um imóvel comercial (início e fim do dia). Usado só para leitura. */
@Entity
@Table(name = "tb_property_shift")
@Getter
@Setter
@NoArgsConstructor
public class PropertyShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
