package br.com.delta.delta_api_postgres.modules.habit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_day_of_week")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DayOfWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;
}