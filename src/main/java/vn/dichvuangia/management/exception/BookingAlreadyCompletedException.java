package vn.dichvuangia.management.exception;

public class BookingAlreadyCompletedException extends RuntimeException {

    public BookingAlreadyCompletedException(Long bookingId) {
        super(String.format("Lịch bảo trì #%d đã hoàn thành, không thể cập nhật", bookingId));
    }
}
