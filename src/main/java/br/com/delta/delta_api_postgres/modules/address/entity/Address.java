package br.com.delta.delta_api_postgres.modules.address.entity;

import br.com.delta.delta_api_postgres.modules.region.entity.Region;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_address")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    @Column(name = "cep", nullable = false, length = 8)
    private String cep;
    @Column(name = "city", nullable = false, length = 60)
    private String city;
    @Column(name = "state", nullable = false, length = 30)
    private String state;
    @Column(name = "latitude", precision = 8, scale = 6)
    private BigDecimal latitude;
    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;
}
