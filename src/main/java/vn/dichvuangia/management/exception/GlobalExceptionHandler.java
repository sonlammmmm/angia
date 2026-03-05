package vn.dichvuangia.management.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.dichvuangia.management.common.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    // ── 400 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleStock(InsufficientStockException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INSUFFICIENT_STOCK"));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_STATUS_TRANSITION"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(),
                        e.getDefaultMessage() == null ? "Không hợp lệ" : e.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .status("error")
                        .message("Dữ liệu không hợp lệ")
                        .data(errors)
                        .errorCode("VALIDATION_ERROR")
                        .build());
    }

    // ── 403 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Tài khoản đã bị khóa", "ACCOUNT_DISABLED"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Bạn không có quyền thực hiện thao tác này", "ACCESS_DENIED"));
    }

    // ── 409 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(BookingAlreadyCompletedException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookingCompleted(BookingAlreadyCompletedException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "BOOKING_ALREADY_COMPLETED"));
    }

    // ── 500 — fallback (KHÔNG lộ stacktrace) ─────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống, vui lòng thử lại sau", "INTERNAL_ERROR"));
    }
}
