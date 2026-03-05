package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.common.MaintenanceStatus;
import vn.dichvuangia.management.entity.MaintenanceLog;
import vn.dichvuangia.management.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    // ── Query 1 ───────────────────────────────────────────────────────────────
    // Lấy tất cả lịch bảo trì đang SCHEDULED/IN_PROGRESS của một technician
    // Dùng trong: MaintenanceService.getScheduledLogs() khi role = TECHNICIAN
    @Query("""
            SELECT ml FROM MaintenanceLog ml
            WHERE ml.technician = :technician
              AND ml.status IN ('SCHEDULED', 'IN_PROGRESS')
            ORDER BY ml.scheduledDate ASC
            """)
    List<MaintenanceLog> findActiveByTechnician(@Param("technician") User technician);

    // ── Query 2 ───────────────────────────────────────────────────────────────
    // Lấy các asset ACTIVE chưa có lịch SCHEDULED/IN_PROGRESS — dùng cho Cron Job
    // Logic: tìm asset_id không tồn tại trong bảng maintenance_logs với status IN (SCHEDULED, IN_PROGRESS)
    @Query("""
            SELECT ml FROM MaintenanceLog ml
            WHERE ml.asset.id NOT IN (
                SELECT ml2.asset.id FROM MaintenanceLog ml2
                WHERE ml2.status IN ('SCHEDULED', 'IN_PROGRESS')
            )
            AND ml.asset.status = 'ACTIVE'
            ORDER BY ml.scheduledDate ASC
            """)
    List<MaintenanceLog> findLogsForAssetsWithoutActiveSchedule();

    // ── Query 3 ───────────────────────────────────────────────────────────────
    // Lấy lịch sử bảo trì của một asset (cho trang chi tiết)
    @Query("""
            SELECT ml FROM MaintenanceLog ml
            WHERE ml.asset.id = :assetId
            ORDER BY ml.scheduledDate DESC
            """)
    List<MaintenanceLog> findAllByAssetId(@Param("assetId") Long assetId);

    // ── Query 4 ───────────────────────────────────────────────────────────────
    // Filter theo date range + status — dùng cho màn hình lịch làm việc technician
    @Query("""
            SELECT ml FROM MaintenanceLog ml
            WHERE (:technicianId IS NULL OR ml.technician.id = :technicianId)
              AND (:status IS NULL OR ml.status = :status)
              AND (:startDate IS NULL OR ml.scheduledDate >= :startDate)
              AND (:endDate IS NULL OR ml.scheduledDate <= :endDate)
            ORDER BY ml.scheduledDate ASC
            """)
    Page<MaintenanceLog> findByFilters(
            @Param("technicianId") Long technicianId,
            @Param("status") MaintenanceStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // ── Helper ────────────────────────────────────────────────────────────────
    // Kiểm tra asset đã có lịch active chưa — dùng trong DuplicateMaintenanceScheduleException
    @Query("""
            SELECT COUNT(ml) > 0 FROM MaintenanceLog ml
            WHERE ml.asset.id = :assetId
              AND ml.status IN ('SCHEDULED', 'IN_PROGRESS')
            """)
    boolean existsActiveScheduleForAsset(@Param("assetId") Long assetId);
}
