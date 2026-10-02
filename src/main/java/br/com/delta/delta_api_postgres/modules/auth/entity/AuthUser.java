package br.com.delta.delta_api_postgres.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_user")
@Getter @Setter @NoArgsConstructor
public class AuthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, unique = true, length = 255) private String email;
    @Column(name = "password", nullable = false, length = 255) private String passwordHash;
    @Column(length = 15) private String phone;
    @Column(name = "birth_date", nullable = false) private java.time.LocalDate birthDate;
    @Column(name = "is_active", nullable = false) private boolean enabled = true;
}
