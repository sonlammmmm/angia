package vn.dichvuangia.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;  // 'ADMIN', 'SALE', 'MANAGEMENT', 'TECHNICIAN'

    @Column(name = "description")
    private String description;
}
