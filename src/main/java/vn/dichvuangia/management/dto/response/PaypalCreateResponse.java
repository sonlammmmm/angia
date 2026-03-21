package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.dichvuangia.management.common.enums.PaymentReferenceType;
import vn.dichvuangia.management.common.enums.PaymentStatus;

import java.math.BigDecimal;

@Getter
@Builder
public class PaypalCreateResponse {

    private Long paymentId;
    private String paypalPaymentId;
    private PaymentStatus status;
    private String approvalUrl;
    private PaymentReferenceType referenceType;
    private Long referenceId;
    private String referenceCode;
    private BigDecimal amountVnd;
    private BigDecimal amountUsd;
    private String message;
}
