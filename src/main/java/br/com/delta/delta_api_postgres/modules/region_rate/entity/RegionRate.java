package br.com.delta.delta_api_postgres.modules.region_rate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_region_rate")
@Getter
@NoArgsConstructor
public class RegionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "m3_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal m3Value;

    @Column(name = "initial_validity", nullable = false)
    private LocalDate initialValidity;

    @Column(name = "final_validity")
    private LocalDate finalValidity;
}
