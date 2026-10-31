package br.com.delta.delta_api_postgres.modules.region.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_municipality")
@Getter
@NoArgsConstructor
public class Municipality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name", nullable = false, length = 60)
    private String name;
    @Column(name = "ibge_code", nullable = false)
    private Integer ibgeCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    public Municipality(String name, Integer ibgeCode, Region region) {
        this.name = name;
        this.ibgeCode = ibgeCode;
        this.region = region;
    }
}
