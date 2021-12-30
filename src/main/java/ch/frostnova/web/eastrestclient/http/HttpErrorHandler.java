package ch.frostnova.web.eastrestclient.http;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;

public final class HttpErrorHandler {

    private HttpErrorHandler() {

    }

    public static void checkResponse(HttpResponse<?> httpResponse) {
        int statusCode = httpResponse.statusCode();
        int statusCodeCategory = statusCode / 100;

        if (statusCodeCategory == 4 || statusCodeCategory == 5) {
            Response response = new ResponseAdapter(httpResponse);
            Response.Status status = Response.Status.fromStatusCode(statusCode);

            if (statusCodeCategory == 4) {
                if (status == BAD_REQUEST) {
                    throw new BadRequestException(response);
                }
                if (status == UNAUTHORIZED) {
                    throw new NotAuthorizedException(response);
                }
                if (status == FORBIDDEN) {
                    throw new ForbiddenException(response);
                }
                if (status == NOT_FOUND) {
                    throw new NotFoundException(response);
                }
                if (status == METHOD_NOT_ALLOWED) {
                    throw new NotAllowedException(response);
                }
                if (status == NOT_ACCEPTABLE) {
                    throw new NotAcceptableException(response);
                }
                if (status == UNSUPPORTED_MEDIA_TYPE) {
                    throw new NotSupportedException(response);
                }
                throw new ClientErrorException(response);
            }
            if (statusCodeCategory == 5) {
                if (status == INTERNAL_SERVER_ERROR) {
                    throw new InternalServerErrorException(response);
                }
                if (status == SERVICE_UNAVAILABLE) {
                    throw new ServiceUnavailableException(response);
                }
                throw new ServerErrorException(response);
            }
        }
    }

    static class ResponseAdapter extends Response {

        private final HttpResponse<?> response;

        public ResponseAdapter(HttpResponse<?> response) {
            this.response = response;
        }

        @Override
        public int getStatus() {
            return response.statusCode();
        }

        @Override
        public StatusType getStatusInfo() {
            return Status.fromStatusCode(response.statusCode());
        }

        @Override
        public Object getEntity() {
            return response.body();
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
            return response.body() != null;
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
