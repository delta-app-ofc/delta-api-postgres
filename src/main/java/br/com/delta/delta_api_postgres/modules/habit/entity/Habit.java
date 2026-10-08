package br.com.delta.delta_api_postgres.modules.habit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_habit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;
}