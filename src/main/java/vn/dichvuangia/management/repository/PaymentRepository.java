package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.common.enums.PaymentReferenceType;
import vn.dichvuangia.management.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaypalPaymentId(String paypalPaymentId);

    Optional<Payment> findTopByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
            PaymentReferenceType referenceType, Long referenceId);
}
