package com.example.automation;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

final class CurlReporter {

    private static final Path REPORT_PATH = Path.of("build", "reports", "curl-report.txt");
    private static final String PIPELINE_REPO = "https://github.com/prudhviraj55/app-test-pipeline.git";
    private static final String AUTOMATION_SUITE_REPO = "https://github.com/prudhviraj55/api-automation.git";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private CurlReporter() {
    }

    static void resetReport() {
        try {
            Files.createDirectories(REPORT_PATH.getParent());
            Files.writeString(REPORT_PATH, "", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to reset curl report", e);
        }
    }

    static synchronized void log(String testName, HttpRequest request, String requestBody,
                                 HttpResponse<String> response, String outcome) {
        if ("SUCCESS".equalsIgnoreCase(outcome)) {
            return; // Only keep failures in the report.
        }
        try {
            Files.createDirectories(REPORT_PATH.getParent());
            StringBuilder sb = new StringBuilder();

            sb.append("=== Test: ").append(testName).append(" @ ")
                    .append(TIME_FORMAT.format(ZonedDateTime.now())).append(" ===\n");
            sb.append("Outcome: ").append(outcome).append("\n");
            sb.append("Request (curl):\n");
            sb.append(toCurl(request, requestBody)).append("\n\n");

            sb.append("Response:\n");
            sb.append("Status: ").append(response.statusCode()).append("\n");
            sb.append("Headers:\n");
            response.headers().map().forEach((key, values) ->
                    sb.append("  ").append(key).append(": ").append(String.join(", ", values)).append("\n"));
            sb.append("Body:\n").append(response.body()).append("\n");

            // Attach repo reference at end of the test log entry.
            sb.append("Repo: ").append(PIPELINE_REPO).append("\n");
            sb.append("Automation Repo: ").append(AUTOMATION_SUITE_REPO).append("\n\n");

            Files.writeString(REPORT_PATH, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write curl report", e);
        }
    }

    static synchronized void logFailure(String testName, HttpRequest request, String requestBody,
                                        HttpResponse<String> response, Throwable error, FailureDetails failureDetails) {
        try {
            Files.createDirectories(REPORT_PATH.getParent());
            StringBuilder sb = new StringBuilder();

            sb.append("=== Test: ").append(testName).append(" @ ")
                    .append(TIME_FORMAT.format(ZonedDateTime.now())).append(" ===\n");
            sb.append("Outcome: FAILURE").append("\n");
            sb.append("Request (curl):\n");
            sb.append(toCurl(request, requestBody)).append("\n\n");

            if (response != null) {
                sb.append("Response:\n");
                sb.append("Status: ").append(response.statusCode()).append("\n");
                sb.append("Headers:\n");
                response.headers().map().forEach((key, values) ->
                        sb.append("  ").append(key).append(": ").append(String.join(", ", values)).append("\n"));
                sb.append("Body:\n").append(response.body()).append("\n");
            } else {
                sb.append("Response: none (request failed)\n");
            }

            sb.append("Error: ").append(error.getClass().getSimpleName())
                    .append(" - ").append(error.getMessage()).append("\n");
            sb.append("Failure Summary:\n");
sb.append("- Assertion: ").append(valueOrDefault(failureDetails == null ? null : failureDetails.assertion, "unknown")).append(" ==> expected: <").append(failureDetails.expected).append("> but was: <").append(failureDetails.actual).append(">\n");
            sb.append("- Assertion: ").append(valueOrDefault(failureDetails == null ? null : failureDetails.assertion, "unknown")).append("\n");
            sb.append("- Expected: ").append(valueOrDefault(failureDetails == null ? null : failureDetails.expected, "unknown")).append("\n");
            sb.append("- Actual: ").append(valueOrDefault(failureDetails == null ? null : failureDetails.actual, "unknown")).append("\n");
            //sb.append("- Suspect file: ").append(valueOrDefault(failureDetails == null ? null : failureDetails.suspectFile, "unknown")).append("\n");
            //sb.append("- Recent change: ").append(valueOrDefault(failureDetails == null ? null : failureDetails.recentChange, "unknown")).append("\n");
            sb.append("- Stack (top): ").append(stackTop(error, failureDetails)).append("\n");
            
            // AI Fix Instructions - formatted for machine parsing
            if (failureDetails != null && failureDetails.assertion != null) {
                String assertion = failureDetails.assertion.toLowerCase();
                String actual = valueOrDefault(failureDetails.actual, "");
                String expected = valueOrDefault(failureDetails.expected, "");
                
                // Generate contextual fix action based on failure type
                if (assertion.contains("status code") && actual.matches("\\d+")) {
                    int statusCode = Integer.parseInt(actual.replaceAll("\"", ""));
                    if (statusCode >= 500) {
                        sb.append("- Fix action: API returns 500 error. Check server logs and fix the internal server error in ")
                          .append(". Review exception handling and null checks.\n");
                    } else if (statusCode >= 400) {
                        sb.append("- Fix action: API returns ").append(statusCode)
                          .append(" error. Fix request validation or input handling in ")
                          .append(valueOrDefault(failureDetails.suspectFile, "unknown")).append(".\n");
                    }
                } else if (!expected.equals("unknown") && !actual.equals("unknown") 
                          && expected.contains("\"") && actual.contains("\"")) {
                    // String literal mismatch - simple replacement
                    sb.append("- Fix action: Replace ").append(actual)
                      .append(" with ").append(expected)
                      .append(" in ").append(valueOrDefault(failureDetails.suspectFile, "unknown")).append(".\n");
                } else {
                    // Generic fix instruction
                    sb.append("- Fix action: In ").append(valueOrDefault(failureDetails.suspectFile, "unknown"))
                      .append(", ensure the code returns ").append(expected)
                      .append(" instead of ").append(actual).append(".\n");
                }
            }
            
            sb.append("Repo: ").append(PIPELINE_REPO).append("\n");
            sb.append("Automation Repo: ").append(AUTOMATION_SUITE_REPO).append("\n\n");

            Files.writeString(REPORT_PATH, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write curl failure report", e);
        }
    }

    private static String toCurl(HttpRequest request, String requestBody) {
        StringBuilder curl = new StringBuilder("curl -X ")
                .append(request.method())
                .append(" '").append(request.uri()).append("'");

        request.headers().map().forEach((key, values) -> {
            for (String value : values) {
                curl.append(" -H '").append(key).append(": ").append(value).append("'");
            }
        });

        if (requestBody != null && !requestBody.isEmpty()) {
            curl.append(" --data-raw '")
                    .append(requestBody.replace("'", "'\"'\"'"))
                    .append("'");
        }

        return curl.toString();
    }

private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String stackTop(Throwable error, FailureDetails details) {
        if (details != null && details.stackTop != null && !details.stackTop.isBlank()) {
            return details.stackTop;
        }
        StackTraceElement[] stack = error.getStackTrace();
        if (stack != null && stack.length > 0) {
            StackTraceElement top = stack[0];
            return error.getClass().getSimpleName() + " at " + top.getClassName() + ":" + top.getLineNumber();
        }
        return error.getClass().getSimpleName();
    }

    static final class FailureDetails {
        final String assertion;
        final String expected;
        final String actual;
        final String suspectFile;
        final String recentChange;
        final String stackTop;

        FailureDetails(String assertion, String expected, String actual,
                       String suspectFile, String recentChange, String stackTop) {
            this.assertion = assertion;
            this.expected = expected;
            this.actual = actual;
            this.suspectFile = suspectFile;
            this.recentChange = recentChange;
            this.stackTop = stackTop;
        }
    }
}
