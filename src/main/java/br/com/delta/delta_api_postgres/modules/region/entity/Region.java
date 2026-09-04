package br.com.delta.delta_api_postgres.modules.region.entity;

import br.com.delta.delta_api_postgres.modules.region.enums.RegionName;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "tb_region")
@Getter
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private RegionName name;
}
