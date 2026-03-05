package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.common.AssetStatus;
import vn.dichvuangia.management.entity.CustomerAsset;

import java.time.LocalDate;
import java.util.List;

public interface CustomerAssetRepository extends JpaRepository<CustomerAsset, Long> {

    // Lấy danh sách asset theo customer — cho GET /customers/{id}/assets
    List<CustomerAsset> findAllByCustomerId(Long customerId);

    // Filter theo customerId và/hoặc status — cho GET /customer-assets
    @Query("""
            SELECT ca FROM CustomerAsset ca
            WHERE (:customerId IS NULL OR ca.customer.id = :customerId)
              AND (:status IS NULL OR ca.status = :status)
            """)
    Page<CustomerAsset> findByFilters(
            @Param("customerId") Long customerId,
            @Param("status") AssetStatus status,
            Pageable pageable
    );

    // ── Core query cho Cron Job ───────────────────────────────────────────────
    // Lấy các asset ACTIVE mà (installation_date + lifespan_months) <= NOW() + X ngày tới
    // VÀ chưa có maintenance_log nào với status IN (SCHEDULED, IN_PROGRESS)
    //
    // SQL tương đương:
    //   installation_date + INTERVAL lifespan_months MONTH <= :cutoffDate
    //
    // Dùng JPQL function DATE_ADD để tính ngày đến hạn
    @Query("""
            SELECT ca FROM CustomerAsset ca
            JOIN ca.product p
            WHERE ca.status = 'ACTIVE'
              AND p.lifespanMonths IS NOT NULL
              AND FUNCTION('DATE_ADD', ca.installationDate, FUNCTION('INTERVAL', p.lifespanMonths, 'MONTH')) <= :cutoffDate
              AND ca.id NOT IN (
                  SELECT ml.asset.id FROM MaintenanceLog ml
                  WHERE ml.status IN ('SCHEDULED', 'IN_PROGRESS')
              )
            """)
    List<CustomerAsset> findAssetsNeedingMaintenanceSchedule(@Param("cutoffDate") LocalDate cutoffDate);
}
