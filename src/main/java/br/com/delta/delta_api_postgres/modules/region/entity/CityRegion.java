package br.com.delta.delta_api_postgres.modules.region.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_city_region")
@Getter
@NoArgsConstructor
public class CityRegion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "city", nullable = false, length = 60)
    private String city;
    @Column(name = "state", nullable = false, length = 2)
    private String state;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    public CityRegion(String city, String state, Region region) {
        this.city = city;
        this.state = state;
        this.region = region;
    }
}
