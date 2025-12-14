# API automation (Gradle)

Simple Gradle project that exercises the `POST /api/orders` endpoint.

## Prerequisites
- Java 17+
- No need for a local Gradle install; the wrapper downloads Gradle 8.10.2 automatically.

## Running the tests
```bash
# Default base URL: http://localhost:8080
./gradlew test

# Override API base URL
API_BASE_URL="http://staging.my-api.com" ./gradlew test
```

The API payload matches the `curl` example from the prompt and asserts a `200/201` response code plus a non-empty body. Adjust assertions as your API behavior becomes concrete.
