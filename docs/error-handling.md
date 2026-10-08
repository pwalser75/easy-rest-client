# Error handling

When a response has an error status (4xx or 5xx), the `RestAdapter` throws a **JAX-RS exception** instead of
returning a value. The response body, when present, is used as the exception message, and the response is wrapped in
a JAX-RS `Response` (status, headers and entity are accessible).

| Status | Exception |
| --- | --- |
| 400 Bad Request | `jakarta.ws.rs.BadRequestException` |
| 401 Unauthorized | `jakarta.ws.rs.NotAuthorizedException` |
| 403 Forbidden | `jakarta.ws.rs.ForbiddenException` |
| 404 Not Found | `jakarta.ws.rs.NotFoundException` |
| 405 Method Not Allowed | `jakarta.ws.rs.NotAllowedException` |
| 406 Not Acceptable | `jakarta.ws.rs.NotAcceptableException` |
| 415 Unsupported Media Type | `jakarta.ws.rs.NotSupportedException` |
| other 4xx | `jakarta.ws.rs.ClientErrorException` |
| 500 Internal Server Error | `jakarta.ws.rs.InternalServerErrorException` |
| 503 Service Unavailable | `jakarta.ws.rs.ServiceUnavailableException` |
| other 5xx | `jakarta.ws.rs.ServerErrorException` |

Example:

```java
try {
    var note = notesClient.get(id);
} catch (NotFoundException ex) {
    // 404, ex.getResponse().getStatus() == 404
}
```

For streaming downloads, an error response is detected before the body is written to the sink, so the sink stays
empty:

```java
try (var out = Files.newOutputStream(target)) {
    fileClient.download("missing", out); // throws NotFoundException, target remains empty
}
```

## Rules

- A 2xx status is considered successful. Redirects (3xx) and 1xx are not followed or treated as errors by the client.
- An empty successful response body yields `null` (or nothing for `void` methods).
- Unsupported response media types throw an `UnsupportedOperationException`.
