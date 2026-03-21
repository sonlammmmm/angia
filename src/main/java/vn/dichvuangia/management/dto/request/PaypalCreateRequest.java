package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.dichvuangia.management.common.enums.PaymentReferenceType;

@Getter
@Setter
public class PaypalCreateRequest {

    @NotNull
    private PaymentReferenceType referenceType;

    @NotNull
    private Long referenceId;
}
