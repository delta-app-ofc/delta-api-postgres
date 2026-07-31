package br.com.delta.delta_api_postgres.entity;

import jakarta.persistence.*;

@Entity
@Table(name="tb_property")
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
}
