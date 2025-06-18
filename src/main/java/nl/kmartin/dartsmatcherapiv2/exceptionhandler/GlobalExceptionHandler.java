package nl.kmartin.dartsmatcherapiv2.exceptionhandler;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ForbiddenException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceAlreadyExistsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.ErrorResponse;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.utils.ErrorUtil;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageResolver messageResolver;

    @Autowired
    public GlobalExceptionHandler(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    /**
     * Handler for all unhandled exceptions.
     *
     * @param e Exception The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleRunTimeException(Exception e) {
        e.printStackTrace();
        ApiErrorCode apiErrorCode = ApiErrorCode.INTERNAL;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.internal")
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for all forbidden exceptions.
     *
     * @param e ForbiddenException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.PERMISSION_DENIED;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                e.getDescription()
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for all method arguments invalid exceptions.
     *
     * @param e MethodArgumentNotValidException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentsInvalidException(MethodArgumentNotValidException e) {
        ArrayList<TargetError> errors = ErrorUtil.extractFieldErrors(e);
        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.invalid.arguments"),
                errors.toArray(new TargetError[0])
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for bean validation errors in services.
     *
     * @param e ConstraintViolationException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        ArrayList<TargetError> errors = ErrorUtil.extractTargetErrors(e);
        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.invalid.arguments"),
                errors.toArray(new TargetError[0])
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for custom invalid arguments.
     *
     * @param e InvalidArgumentsException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({InvalidArgumentsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidArgumentException(InvalidArgumentsException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.invalid.arguments"),
                e.getErrors().toArray(new TargetError[0])
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for missing request parameters.
     *
     * @param e MissingServletRequestParameterException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {

        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.invalid.arguments"),
                new TargetError(e.getParameterName(), messageResolver.getMessage("javax.validation.constraints.NotNull.message"))
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for accessing url that don't support the Http media type (e.g. using form url encoded where only application/json is supported).
     *
     * @param e HttpMediaTypeException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({HttpMediaTypeException.class})
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeException(HttpMediaTypeException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.UNSUPPORTED_MEDIA_TYPE;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for accessing url that don't support the Http method (e.g. using HTTP POST where only HTTP GET is supported).
     *
     * @param e HttpRequestMethodNotSupportedException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.METHOD_NOT_ALLOWED;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                e.getMessage()
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for accessing url that doesn't exist
     *
     * @param e NoHandlerFoundException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundExceptionException(NoHandlerFoundException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.URI_NOT_FOUND;
        ErrorResponse responseBody = new ErrorResponse(
                ApiErrorCode.URI_NOT_FOUND,
                messageResolver.getMessage("exception.uri.not.found", e.getRequestURL())
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for accessing url that doesn't exist
     *
     * @param e ResourceNotFoundException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.RESOURCE_NOT_FOUND;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.resource.not.found", e.getResourceType(), e.getIdentifier())
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for sending malformed data or invalid data types (e.g. invalid json, using array instead of string).
     *
     * @param e HttpMessageNotReadableException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(Exception e) {
        e.printStackTrace();

        ApiErrorCode apiErrorCode = ApiErrorCode.MESSAGE_NOT_READABLE;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.body.not.readable")
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for trying to create a resource when it already exists.
     *
     * @param e ResourceAlreadyExistsException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({ResourceAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.ALREADY_EXISTS;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.resource.already.exists", e.getResourceType()),
                new TargetError(e.getTarget(), messageResolver.getMessage("message.resource.already.exists", e.getValue()))
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    /**
     * Handler for when the database is down.
     *
     * @param e DataAccessResourceFailureException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({DataAccessResourceFailureException.class})
    public ResponseEntity<ErrorResponse> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.UNAVAILABLE;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.service.unavailable")
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }
}