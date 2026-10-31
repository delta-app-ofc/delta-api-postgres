package br.com.delta.delta_api_postgres.modules.habit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_user_habit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserHabit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(nullable = false)
    private Integer frequency;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "estimated_liters", precision = 6, scale = 2)
    private BigDecimal estimatedLiters;

    @OneToMany(
            mappedBy = "userHabit",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserHabitDay> days = new ArrayList<>();
}