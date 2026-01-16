package com.example.automation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

class OrderApiTest {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String baseUrl = Optional.ofNullable(System.getenv("API_BASE_URL"))
            .orElse("http://localhost:9090");

    @BeforeAll
    static void resetCurlReport() {
        CurlReporter.resetReport();
    }

@BeforeAll
    static void createOrderForTest() throws Exception {
        String orderId = "abcd-12345";
        Map<String, Object> payload = Map.of(
            "customerId", "cust-123",
            "productSku", "sku-456",
            "quantity", 2,
            "shippingAddress", "123 Main St, Springfield",
            "unitPrice", 19.99,
            "deliveryNotes", "Leave at porch",
            "promoCode", "SPRING10",
            "giftWrap", true,
            "requestedDeliveryDate", "2024-05-30"
        );

        String body = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orders"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            CurlReporter.logFailure("createOrderForTest", request, body, null, e, null);
            throw e;
        }

        assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
                "Expected 200/201 from POST /api/orders but got " + response.statusCode() + " with body: " + response.body());
        assertFalse(response.body().isEmpty(), "Response body should contain order info or error details.");
    }
    void createOrder(TestInfo testInfo) throws Exception {
        Map<String, Object> payload = Map.of(
"customerId", "cust-123",
                "productSku", "sku-456",
                "quantity", 2,
                "shippingAddress", "123 Main St, Springfield",
                "unitPrice", 19.99,
                "deliveryNotes", "Leave at porch",
                "promoCode", "SPRING10",
                "giftWrap", true,
                "requestedDeliveryDate", "2024-05-30"
        );

        String body = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orders"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            CurlReporter.logFailure(testInfo.getDisplayName(), request, body, null, e, null);
            throw e;
        }

        try {
            int status = response.statusCode();
            assertTrue(status == 200 || status == 201,
                    "Expected 200/201 from POST /api/orders but got " + status + " with body: " + response.body());
            assertFalse(response.body().isEmpty(), "Response body should contain order info or error details.");
            CurlReporter.log(testInfo.getDisplayName(), request, body, response, "SUCCESS");
        } catch (AssertionError e) {
            String stackTop = "AssertionFailedError at createOrder(TestInfo)::assertStatusOrBody";
            CurlReporter.FailureDetails details = new CurlReporter.FailureDetails(
                    "status in [200,201] and body not empty",
                    "HTTP 200/201 with non-empty body",
                    response != null ? "HTTP " + response.statusCode() + " with body: " + response.body() : "unknown",
                    "src/main/java/com/example/apptestpipeline/order/OrderController.java:createOrder",
                    null,
                    stackTop
            );
            CurlReporter.logFailure(testInfo.getDisplayName(), request, body, response, e, details);
            throw e;
        }
    }

@BeforeAll
    static void createOrderForTest() throws Exception {
        String orderId = "abcd-12345";
        Map<String, Object> payload = Map.of(
            "customerId", "cust-123",
            "productSku", "sku-456",
            "quantity", 2,
            "shippingAddress", "123 Main St, Springfield",
            "unitPrice", 19.99,
            "deliveryNotes", "Leave at porch",
            "promoCode", "SPRING10",
            "giftWrap", true,
            "requestedDeliveryDate", "2024-05-30"
        );

        String body = MAPPER.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orders"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            CurlReporter.logFailure("createOrderForTest", request, body, null, e, null);
            throw e;
        }

        assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
                "Expected 200/201 from POST /api/orders but got " + response.statusCode() + " with body: " + response.body());
        assertFalse(response.body().isEmpty(), "Response body should contain order info or error details.");
    }
    void getOrderStatus(TestInfo testInfo) throws Exception {
        String orderId = "abcd-12345";
        HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create(baseUrl + "/api/orders/" + orderId + "/status"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            CurlReporter.logFailure(testInfo.getDisplayName(), request, null, null, e, null);
            throw e;
        }

        try {
            int status = response.statusCode();
            try {
                assertTrue(status == 200 || status == 201,
                        "Expected 200/201 from GET /api/orders/{id}/status but got " + status + " with body: " + response.body());
            } catch (AssertionError e) {
                String recentChange = null;
                if (status >= 500) {
                    recentChange = "Server error - check for recent code changes causing exceptions, null pointer errors, or unhandled edge cases";
                } else if (status >= 400) {
                    recentChange = "Client error - check for validation issues or incorrect request handling";
                }
                
                CurlReporter.FailureDetails details = new CurlReporter.FailureDetails(
                        "status code in [200, 201]",
                        "200 or 201",
                        String.valueOf(status),
                        "src/main/java/com/example/apptestpipeline/order/OrderController.java:getOrderStatus",
                        recentChange,
                        "AssertionFailedError at getOrderStatus(TestInfo)::assertStatusCode"
                );
                CurlReporter.logFailure(testInfo.getDisplayName(), request, null, response, e, details);
                throw e;
            }

            var root = MAPPER.readTree(response.body());
            
            // Check overallStatus
            String actualOverallStatus = root.get("overallStatus").asText();
            try {
                assertTrue(actualOverallStatus.equals("ACTIVE"), "overallStatus should be ACTIVE");
            } catch (AssertionError e) {
                CurlReporter.FailureDetails details = new CurlReporter.FailureDetails(
                        "overallStatus == \"ACTIVE\"",
                        "\"ACTIVE\"",
                        "\"" + actualOverallStatus + "\"",
                        "src/main/java/com/example/apptestpipeline/order/OrderController.java",
                        null,
                        "AssertionFailedError at getOrderStatus(TestInfo)::assertOverallStatus"
                );
                CurlReporter.logFailure(testInfo.getDisplayName(), request, null, response, e, details);
                throw e;
            }
            
            // Check stage
            String actualStage = root.get("stage").asText();
            try {
                assertTrue(actualStage.equals("PACKING"), "stage should be PACKING");
            } catch (AssertionError e) {
                CurlReporter.FailureDetails details = new CurlReporter.FailureDetails(
                        "stage == \"PACKING\"",
                        "\"PACKING\"",
                        "\"" + actualStage + "\"",
                        "src/main/java/com/example/apptestpipeline/order/OrderController.java:getOrderStatus",
                        "commit fcf5a72c modified OrderController.java",
                        "AssertionFailedError at getOrderStatus(TestInfo)::assertStage"
                );
                CurlReporter.logFailure(testInfo.getDisplayName(), request, null, response, e, details);
                throw e;
            }
            
            // Check progressPercent
            int actualProgress = root.get("progressPercent").asInt();
            try {
                assertTrue(actualProgress == 50, "progressPercent should be 50");
            } catch (AssertionError e) {
                CurlReporter.FailureDetails details = new CurlReporter.FailureDetails(
                        "progressPercent == 50",
                        "50",
                        String.valueOf(actualProgress),
                        "src/main/java/com/example/apptestpipeline/order/OrderController.java",
                        null,
                        "AssertionFailedError at getOrderStatus(TestInfo)::assertProgressPercent"
                );
                CurlReporter.logFailure(testInfo.getDisplayName(), request, null, response, e, details);
                throw e;
            }

            // Check payment status
            var payment = root.get("payment");
            assertTrue(payment != null && !payment.isNull(), "payment object should be present");
            String actualPaymentStatus = payment.get("status").asText();
            try {
                assertTrue(actualPaymentStatus.equals("CLEARED"), "payment.status should be CLEARED");
            } catch (AssertionError e) {
                CurlReporter.FailureDetails details = new CurlReporter.FailureDetails(
                        "payment.status == \"CLEARED\"",
                        "\"CLEARED\"",
                        "\"" + actualPaymentStatus + "\"",
                        "src/main/java/com/example/apptestpipeline/order/OrderController.java:133 (PaymentStatus status literal)",
                        "commit c926a9357e0762d9 touched OrderController.java",
                        "AssertionFailedError at getOrderStatus(TestInfo)::assertPaymentCleared"
                );
                CurlReporter.logFailure(testInfo.getDisplayName(), request, null, response, e, details);
                throw e;
            }

            CurlReporter.log(testInfo.getDisplayName(), request, null, response, "SUCCESS");
        } catch (AssertionError e) {
            // Re-throw if already logged
            throw e;
        } catch (Exception e) {
            // Catch any other exceptions (JSON parsing, etc.) and log them
            CurlReporter.FailureDetails details = new CurlReporter.FailureDetails(
                    "test execution",
                    "successful test execution",
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    "src/main/java/com/example/apptestpipeline/order/OrderController.java:getOrderStatus",
                    null,
                    e.getClass().getSimpleName() + " at getOrderStatus(TestInfo)"
            );
            CurlReporter.logFailure(testInfo.getDisplayName(), request, null, response, e, details);
            throw e;
        }
    }
}
