package br.com.delta.delta_api_postgres.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="tb_device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "device_id", nullable = false, unique = true)
    private Integer deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;
}
