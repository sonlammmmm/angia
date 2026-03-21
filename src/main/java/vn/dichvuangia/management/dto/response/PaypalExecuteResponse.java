package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.dichvuangia.management.common.enums.PaymentReferenceType;
import vn.dichvuangia.management.common.enums.PaymentStatus;

@Getter
@Builder
public class PaypalExecuteResponse {

    private PaymentStatus status;
    private PaymentReferenceType referenceType;
    private Long referenceId;
    private String referenceCode;
    private String message;
}
