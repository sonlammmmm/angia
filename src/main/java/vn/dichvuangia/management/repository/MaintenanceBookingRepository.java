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

    // Lấy lịch của một technician (có thể lọc thêm status nếu != null)
    Page<MaintenanceBooking> findAllByTechnicianIdAndStatus(
            Long technicianId, BookingStatus status, Pageable pageable);

    Page<MaintenanceBooking> findAllByTechnicianId(Long technicianId, Pageable pageable);

    // Lịch trong khoảng thời gian (cho màn hình lịch làm việc)
    @Query("SELECT b FROM MaintenanceBooking b WHERE b.bookingDate BETWEEN :from AND :to")
    Page<MaintenanceBooking> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    // Lịch sử bảo trì của một khách hàng
    Page<MaintenanceBooking> findAllByCustomerIdOrderByCreatedAtDesc(
            Long customerId, Pageable pageable);

    // Filter nâng cao: status + khoảng ngày (dùng cho màn hình danh sách ADMIN/MANAGEMENT)
    @Query("""
            SELECT b FROM MaintenanceBooking b
            WHERE (:status IS NULL OR b.status = :status)
              AND (:from IS NULL OR b.bookingDate >= :from)
              AND (:to IS NULL OR b.bookingDate <= :to)
              AND (:technicianId IS NULL OR b.technician.id = :technicianId)
              AND (:customerId IS NULL OR b.customer.id = :customerId)
            """)
    Page<MaintenanceBooking> findAllWithFilter(
            @Param("status") BookingStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("technicianId") Long technicianId,
            @Param("customerId") Long customerId,
            Pageable pageable);

    // Lịch của technician trong khoảng ngày (kiểm tra trùng lịch)
    @Query("""
            SELECT b FROM MaintenanceBooking b
            WHERE b.technician.id = :technicianId
              AND b.bookingDate BETWEEN :from AND :to
              AND b.status NOT IN ('CANCELLED')
            """)
    Page<MaintenanceBooking> findByTechnicianAndDateRange(
            @Param("technicianId") Long technicianId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
