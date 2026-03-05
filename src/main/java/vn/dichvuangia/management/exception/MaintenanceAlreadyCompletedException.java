package vn.dichvuangia.management.exception;

public class MaintenanceAlreadyCompletedException extends RuntimeException {

    public MaintenanceAlreadyCompletedException(Long logId) {
        super(String.format("Lịch bảo trì #%d đã hoàn thành, không thể cập nhật", logId));
    }
}
