package com.vtea.service.payment;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PayOSPaymentClient {

    private static final String BASE_URL = "http://localhost:8080/api/payments";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public PayOSCreateResponse createPayment(BigDecimal amount, String description)
            throws IOException, InterruptedException {

        int amountValue = amount.intValue();

        String jsonBody = """
                {
                  "amount": %d,
                  "description": "%s"
                }
                """.formatted(amountValue, escapeJson(description));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Không thể tạo thanh toán payOS: " + response.body());
        }

        return parseCreateResponse(response.body());
    }

    public String getStatus(long orderCode)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + orderCode + "/status"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Không thể kiểm tra trạng thái payOS: " + response.body());
        }

        return extractJsonString(response.body(), "status");
    }

    public void openCheckoutUrl(String checkoutUrl) {
        try {
            if (checkoutUrl == null || checkoutUrl.trim().isEmpty()) {
                throw new RuntimeException("Link thanh toán payOS bị trống");
            }

            System.out.println("payOS checkoutUrl = " + checkoutUrl);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(checkoutUrl));
                return;
            }

            // Fallback cho Linux/Arch nếu Desktop API không mở được browser.
            new ProcessBuilder("xdg-open", checkoutUrl).start();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể mở trang thanh toán payOS. Link: " + checkoutUrl,
                    e
            );
        }
    }

    private PayOSCreateResponse parseCreateResponse(String json) {
        long orderCode = Long.parseLong(extractJsonValue(json, "orderCode"));
        String checkoutUrl = extractJsonString(json, "checkoutUrl");
        String status = extractJsonString(json, "status");

        return new PayOSCreateResponse(orderCode, checkoutUrl, status);
    }

    private String extractJsonString(String json, String key) {
        return extractJsonValue(json, key);
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);

        if (start == -1) {
            throw new RuntimeException("Không tìm thấy key `" + key + "` trong response: " + json);
        }

        start += pattern.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }

        int end = json.indexOf(",", start);
        if (end == -1) {
            end = json.indexOf("}", start);
        }

        return json.substring(start, end).trim();
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }
}