package vn.dichvuangia.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "spec_groups")
public class SpecGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;   // VD: "Thông số chung", "Khả năng lọc"

    @Column(name = "display_order")
    private Integer displayOrder;
}
