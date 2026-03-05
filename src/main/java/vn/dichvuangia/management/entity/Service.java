package vn.dichvuangia.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "services") // Bảng dịch vụ
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_code", unique = true, length = 50)
    private String serviceCode;

    @Column(name = "name", nullable = false)
    private String name;   // VD: 'Vệ sinh máy', 'Thay lõi định kỳ'

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
