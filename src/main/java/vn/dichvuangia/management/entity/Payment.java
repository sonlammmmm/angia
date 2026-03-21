package vn.dichvuangia.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.dichvuangia.management.common.enums.PaymentMethod;
import vn.dichvuangia.management.common.enums.PaymentReferenceType;
import vn.dichvuangia.management.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 20)
    private PaymentReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @Column(name = "amount_vnd", precision = 12, scale = 2)
    private BigDecimal amountVnd;

    @Column(name = "amount_usd", precision = 12, scale = 2)
    private BigDecimal amountUsd;

    @Column(name = "currency", length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paypal_payment_id", length = 120)
    private String paypalPaymentId;

    @Column(name = "paypal_payer_id", length = 120)
    private String paypalPayerId;

    @Column(name = "approval_url", length = 512)
    private String approvalUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment other = (Payment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}
