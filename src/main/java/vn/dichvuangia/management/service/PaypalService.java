package vn.dichvuangia.management.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Service wrapper cho PayPal SDK.
 * - Tạo payment
 * - Lấy approval URL
 * - Execute payment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaypalService {

    private final APIContext apiContext;
    
    @Value("${paypal.frontend-success-url}")
    private String allowedSuccessUrl;
    
    @Value("${paypal.frontend-cancel-url}")
    private String allowedCancelUrl;

    /**
     * ⚠️ SECURITY: Validate redirect URLs against whitelist to prevent open redirect attacks
     */
    private void validateRedirectUrl(String urlString, String allowedUrl) {
        if (urlString == null || urlString.isBlank()) {
            throw new IllegalArgumentException("Redirect URL không được để trống");
        }
        
        try {
            URL url = new URL(urlString);
            URL allowed = new URL(allowedUrl);
            
            // Check protocol
            if (!url.getProtocol().equals(allowed.getProtocol())) {
                throw new IllegalArgumentException("Redirect URL protocol không hợp lệ");
            }
            
            // Check host
            if (!url.getHost().equals(allowed.getHost())) {
                throw new IllegalArgumentException("Redirect URL host không hợp lệ");
            }
            
            // Check port
            if (url.getPort() != allowed.getPort()) {
                throw new IllegalArgumentException("Redirect URL port không hợp lệ");
            }
        } catch (Exception e) {
            log.warn("Invalid redirect URL: {}", urlString);
            throw new IllegalArgumentException("Redirect URL không hợp lệ: " + e.getMessage());
        }
    }

    public Payment createPayment(
            Double total,
            String currency,
            String description,
            String cancelUrl,
            String successUrl) throws PayPalRESTException {

        // ⚠️ SECURITY: Validate redirect URLs before creating payment
        validateRedirectUrl(successUrl, allowedSuccessUrl);
        validateRedirectUrl(cancelUrl, allowedCancelUrl);

        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total).replace(",", "."));

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public String getApprovalUrl(Payment payment) {
        for (Links link : payment.getLinks()) {
            if ("approval_url".equals(link.getRel())) {
                return link.getHref();
            }
        }
        return "";
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecute);
    }
}
