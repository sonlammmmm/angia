package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.common.enums.BookingStatus;
import vn.dichvuangia.management.entity.MaintenanceBooking;

import java.time.LocalDateTime;

public interface MaintenanceBookingRepository extends JpaRepository<MaintenanceBooking, Long> {

    Page<MaintenanceBooking> findAllByTechnicianIdAndStatus(
            Long technicianId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM MaintenanceBooking b WHERE b.bookingDate BETWEEN :from AND :to")
    Page<MaintenanceBooking> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    Page<MaintenanceBooking> findAllByCustomerIdOrderByCreatedAtDesc(
            Long customerId, Pageable pageable);
}
