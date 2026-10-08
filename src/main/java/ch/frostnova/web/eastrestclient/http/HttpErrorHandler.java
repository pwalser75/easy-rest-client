package ch.frostnova.web.eastrestclient.http;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static jakarta.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static jakarta.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;

public final class HttpErrorHandler {

    private HttpErrorHandler() {

    }

    public static void checkResponse(HttpResponse<?> httpResponse, String responseMessage) {
        var statusCode = httpResponse.statusCode();
        var statusCodeCategory = statusCode / 100;

        if (statusCodeCategory == 4 || statusCodeCategory == 5) {
            Response response = new ResponseAdapter(httpResponse, responseMessage);
            var status = Response.Status.fromStatusCode(statusCode);
            var message = responseMessage;
            if (message != null && message.isBlank()) message = null;

            if (statusCodeCategory == 4) {
                if (status == BAD_REQUEST) {
                    throw new BadRequestException(message, response);
                }
                if (status == UNAUTHORIZED) {
                    throw new NotAuthorizedException(message, response);
                }
                if (status == FORBIDDEN) {
                    throw new ForbiddenException(message, response);
                }
                if (status == NOT_FOUND) {
                    throw new NotFoundException(message, response);
                }
                if (status == METHOD_NOT_ALLOWED) {
                    throw new NotAllowedException(message, response);
                }
                if (status == NOT_ACCEPTABLE) {
                    throw new NotAcceptableException(message, response);
                }
                if (status == UNSUPPORTED_MEDIA_TYPE) {
                    throw new NotSupportedException(message, response);
                }
                throw new ClientErrorException(message, response);
            }
            if (statusCodeCategory == 5) {
                if (status == INTERNAL_SERVER_ERROR) {
                    throw new InternalServerErrorException(message, response);
                }
                if (status == SERVICE_UNAVAILABLE) {
                    throw new ServiceUnavailableException(message, response);
                }
                throw new ServerErrorException(message, response);
            }
        }
    }

    static class ResponseAdapter extends Response {

        private final HttpResponse<?> response;
        private final String message;

        public ResponseAdapter(HttpResponse<?> response, String message) {
            this.response = response;
            this.message = message;
        }

        @Override
        public int getStatus() {
            return response.statusCode();
        }

        @Override
        public StatusType getStatusInfo() {
            var code = response.statusCode();
            var known = Status.fromStatusCode(code);
            if (known != null) {
                return known;
            }
            return new StatusType() {
                @Override
                public int getStatusCode() {
                    return code;
                }

                @Override
                public Status.Family getFamily() {
                    return Status.Family.familyOf(code);
                }

                @Override
                public String getReasonPhrase() {
                    return "";
                }
            };
        }

        @Override
        public Object getEntity() {
            return message;
        }

        @Override
        public <T> T readEntity(Class<T> entityType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T readEntity(GenericType<T> entityType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
            return null;
        }

        @Override
        public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasEntity() {
            return message != null;
        }

        @Override
        public boolean bufferEntity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {

        }

        @Override
        public MediaType getMediaType() {
            return response.headers().firstValue("content-type").map(MediaType::valueOf).orElse(null);
        }

        @Override
        public Locale getLanguage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLength() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getAllowedMethods() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, NewCookie> getCookies() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityTag getEntityTag() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getLastModified() {
            throw new UnsupportedOperationException();
        }

        @Override
        public URI getLocation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Link> getLinks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasLink(String relation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Link getLink(String relation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Link.Builder getLinkBuilder(String relation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MultivaluedMap<String, Object> getMetadata() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MultivaluedMap<String, String> getStringHeaders() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeaderString(String name) {
            throw new UnsupportedOperationException();
        }
    }
}
