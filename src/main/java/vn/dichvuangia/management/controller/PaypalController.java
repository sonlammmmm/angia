package vn.dichvuangia.management.controller;

import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.dichvuangia.management.common.ApiResponse;
import vn.dichvuangia.management.dto.request.PaypalCreateRequest;
import vn.dichvuangia.management.dto.response.PaypalCreateResponse;
import vn.dichvuangia.management.dto.response.PaypalExecuteResponse;
import vn.dichvuangia.management.service.PaymentService;

@Tag(name = "PayPal", description = "Thanh toán PayPal cho đơn hàng và lịch hẹn")
@RestController
@RequestMapping("/paypal")
@RequiredArgsConstructor
public class PaypalController {

    private final PaymentService paymentService;

    @Operation(summary = "Tạo thanh toán PayPal")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi PayPal")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaypalCreateResponse>> createPayment(
            @Valid @RequestBody PaypalCreateRequest request) {
        try {
            PaypalCreateResponse response = paymentService.createPaypalPayment(request);
            return ResponseEntity.ok(ApiResponse.success("Tạo thanh toán thành công", response));
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi tạo thanh toán PayPal", "PAYPAL_CREATE_FAILED"));
        }
    }

    @Operation(summary = "Xác nhận thanh toán PayPal")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Thanh toán không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi PayPal")
    })
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<PaypalExecuteResponse>> executePayment(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {
        try {
            PaypalExecuteResponse response = paymentService.executePaypalPayment(paymentId, payerId);
            return ResponseEntity.ok(ApiResponse.success("Xác nhận thanh toán thành công", response));
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Xác nhận thanh toán thất bại", "PAYPAL_EXECUTE_FAILED"));
        }
    }
}
