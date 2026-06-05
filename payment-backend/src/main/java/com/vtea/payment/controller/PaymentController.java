package com.vtea.payment.controller;
/**
 * Controller xử lý thanh toán payOS.
 * Cung cấp API tạo link thanh toán, kiểm tra trạng thái và nhận kết quả thanh toán.
 */
import com.vtea.payment.dto.CreatePaymentRequest;
import com.vtea.payment.dto.CreatePaymentResponse;
import com.vtea.payment.dto.PaymentStatusResponse;
import com.vtea.payment.service.PaymentStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PayOS payOS;
    private final PaymentStore paymentStore;

    public PaymentController(PayOS payOS, PaymentStore paymentStore) {
        this.payOS = payOS;
        this.paymentStore = paymentStore;
    }



    /**
     * Tạo link thanh toán payOS cho đơn hàng.
     * Backend gửi số tiền và mô tả đơn hàng sang payOS, sau đó trả checkoutUrl cho app JavaFX.
     */
    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @RequestBody CreatePaymentRequest request
    ) throws Exception {

        long orderCode = System.currentTimeMillis() / 1000;

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("VTea order")
                .quantity(1)
                .price(request.getAmount())
                .build();

        CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(request.getAmount())
                .description(request.getDescription())
                .items(List.of(item))
                .returnUrl("http://localhost:8080/api/payments/return/" + orderCode)
                .cancelUrl("http://localhost:8080/api/payments/cancel/" + orderCode)
                .build();

        CreatePaymentLinkResponse paymentLink = payOS.paymentRequests().create(paymentRequest);

        paymentStore.createPending(orderCode);

        return ResponseEntity.ok(
                new CreatePaymentResponse(
                        orderCode,
                        paymentLink.getCheckoutUrl(),
                        "PENDING"
                )
        );
    }


    /**
     * Nhận webhook từ payOS khi thanh toán thành công.
     * Bản demo dùng payload để cập nhật trạng thái giao dịch thành PAID.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            System.out.println("Webhook received:");
            System.out.println(payload);

            Object codeObj = payload.get("code");
            Object successObj = payload.get("success");
            Object dataObj = payload.get("data");

            boolean isSuccess = Boolean.TRUE.equals(successObj);
            boolean isCodeSuccess = "00".equals(String.valueOf(codeObj));

            if (isSuccess && isCodeSuccess && dataObj instanceof Map<?, ?> data) {
                Object orderCodeObj = data.get("orderCode");

                if (orderCodeObj != null) {
                    long orderCode = Long.parseLong(String.valueOf(orderCodeObj));
                    paymentStore.markPaid(orderCode);

                    System.out.println("Payment marked as PAID: " + orderCode);
                }
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Webhook error");
        }
    }


    /**
     * Trả về trạng thái thanh toán hiện tại của giao dịch.
     * App JavaFX gọi API này liên tục để biết đơn đã PAID hay chưa.
     */
    @GetMapping("/{orderCode}/status")
    public ResponseEntity<PaymentStatusResponse> getStatus(
            @PathVariable("orderCode") long orderCode
    ) {
        return ResponseEntity.ok(
                new PaymentStatusResponse(
                        orderCode,
                        paymentStore.getStatus(orderCode)
                )
        );
    }


    /**
     * URL payOS chuyển về sau khi khách thanh toán thành công.
     * Dùng trong môi trường local để đánh dấu giao dịch PAID.
     */
    @GetMapping({"/return", "/return/{orderCode}"})
    public ResponseEntity<String> returnUrl(
            @PathVariable(value = "orderCode", required = false) Long orderCode,
            @RequestParam Map<String, String> params
    ) {
        try {
            Long resolvedOrderCode = orderCode;

            if (resolvedOrderCode == null && params.containsKey("orderCode")) {
                resolvedOrderCode = Long.parseLong(params.get("orderCode"));
            }

            System.out.println("payOS return params = " + params);
            System.out.println("payOS return orderCode = " + resolvedOrderCode);

            if (resolvedOrderCode != null) {
                paymentStore.markPaid(resolvedOrderCode);
                System.out.println("Payment marked as PAID from returnUrl: " + resolvedOrderCode);
            }

            return ResponseEntity.ok("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Thanh toán thành công</title>
                </head>
                <body style="font-family: Arial; text-align: center; margin-top: 80px;">
                    <h2>Thanh toán thành công</h2>
                    <p>Bạn có thể quay lại app VTea.</p>
                </body>
                </html>
                """);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    "Lỗi xử lý returnUrl: " + e.getMessage()
            );
        }
    }


    /**
     * URL payOS chuyển về khi khách hủy thanh toán.
     * Đánh dấu giao dịch là CANCELLED.
     */
    @GetMapping({"/cancel", "/cancel/{orderCode}"})
    public ResponseEntity<String> cancelUrl(
            @PathVariable(value = "orderCode", required = false) Long orderCode,
            @RequestParam Map<String, String> params
    ) {
        try {
            Long resolvedOrderCode = orderCode;

            if (resolvedOrderCode == null && params.containsKey("orderCode")) {
                resolvedOrderCode = Long.parseLong(params.get("orderCode"));
            }

            System.out.println("payOS cancel params = " + params);
            System.out.println("payOS cancel orderCode = " + resolvedOrderCode);

            if (resolvedOrderCode != null) {
                paymentStore.markCancelled(resolvedOrderCode);
                System.out.println("Payment marked as CANCELLED from cancelUrl: " + resolvedOrderCode);
            }

            return ResponseEntity.ok("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Thanh toán đã hủy</title>
                </head>
                <body style="font-family: Arial; text-align: center; margin-top: 80px;">
                    <h2>Thanh toán đã bị hủy</h2>
                    <p>Bạn có thể quay lại app VTea.</p>
                </body>
                </html>
                """);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    "Lỗi xử lý cancelUrl: " + e.getMessage()
            );
        }
    }
}