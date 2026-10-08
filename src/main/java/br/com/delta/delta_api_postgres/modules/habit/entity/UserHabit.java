package br.com.delta.delta_api_postgres.modules.habit.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @OneToMany(
            mappedBy = "userHabit",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserHabitDay> days = new ArrayList<>();
}