package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerCreateRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String address;

    // ── Optional: tạo tài khoản đăng nhập cho khách hàng ───────────────────
    @Size(min = 3, message = "Tên đăng nhập phải có ít nhất 3 ký tự")
    private String username;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    private String confirmPassword;
}
