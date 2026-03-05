package vn.dichvuangia.management.exception;

public class DuplicateMaintenanceScheduleException extends RuntimeException {

    public DuplicateMaintenanceScheduleException(Long assetId) {
        super(String.format("Tài sản #%d đã có lịch bảo trì đang chờ hoặc đang thực hiện", assetId));
    }
}
