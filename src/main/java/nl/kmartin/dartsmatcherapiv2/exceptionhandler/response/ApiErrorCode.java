package nl.kmartin.dartsmatcherapiv2.exceptionhandler.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum class which maps the api error codes with the corresponding HTTP Status Codes.
 * <p>
 * Reference: https://cloud.google.com/apis/design/errors#error_localization
 */
@AllArgsConstructor
@Getter
public enum ApiErrorCode {
    INVALID_ARGUMENTS(HttpStatus.BAD_REQUEST), // 400
    MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST), // 400
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED), // 401
    PERMISSION_DENIED(HttpStatus.FORBIDDEN), // 403
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND), // 404
    URI_NOT_FOUND(HttpStatus.NOT_FOUND), // 404
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED), // 405
    ALREADY_EXISTS(HttpStatus.CONFLICT), // 409
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE), // 415
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR), // 500
    UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE); // 503
    private final HttpStatus httpStatus;
}
