package vn.dichvuangia.management.exception;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String from, String to) {
        super(String.format("Không thể chuyển trạng thái từ '%s' sang '%s'", from, to));
    }
}
