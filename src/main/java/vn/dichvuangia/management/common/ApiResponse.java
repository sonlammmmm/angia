package vn.dichvuangia.management.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;
    private String errorCode;

    // ── Success factories ─────────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message("Thành công")
                .data(data)
                .errorCode(null)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .errorCode(null)
                .build();
    }

    // ── Error factories ───────────────────────────────────────────────────────

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .data(null)
                .errorCode(errorCode)
                .build();
    }
}
