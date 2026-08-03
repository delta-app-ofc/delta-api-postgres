package br.com.delta.delta_api_postgres.entity;

import br.com.delta.delta_api_postgres.enums.PropertyClassification;
import br.com.delta.delta_api_postgres.enums.PropertyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name="tb_property")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyClassification classification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;
}
