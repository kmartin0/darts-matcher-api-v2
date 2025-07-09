package nl.kmartin.dartsmatcherapiv2.exceptionhandler;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapiv2.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceAlreadyExistsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.ErrorResponse;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.utils.ErrorUtil;
import nl.kmartin.dartsmatcherapiv2.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
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
        logger.error("handleRunTimeException", e);
        ApiErrorCode apiErrorCode = ApiErrorCode.INTERNAL;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL)
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
        logger.error("handleMethodArgumentsInvalidException", e);

        ArrayList<TargetError> errors = ErrorUtil.extractFieldErrors(e);
        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
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
        logger.error("handleConstraintViolationException", e);

        ArrayList<TargetError> errors = ErrorUtil.extractTargetErrors(e);
        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
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
        logger.error("handleInvalidArgumentException", e);

        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
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
        logger.error("handleMissingServletRequestParameterException", e);

        ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ARGUMENTS;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                new TargetError(e.getParameterName(), messageResolver.getMessage(MessageKeys.VALIDATION_NOT_NULL))
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
        logger.error("handleHttpMediaTypeException", e);

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
        logger.error("handleHttpRequestMethodNotSupportedException", e);

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
     * @param e Exception The exception that was thrown: NoHandlerFoundException or NoResourceFoundException
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoMappingFoundException(Exception e) {
        logger.error("handleNoMappingFoundException", e);

        // Get request URL from exception
        String requestUrl = (e instanceof NoHandlerFoundException ex) ? ex.getRequestURL()
                : (e instanceof NoResourceFoundException ex) ? ex.getResourcePath()
                : "";

        // noHandler will prefix with forward slash, check for consistency.
        if (!requestUrl.startsWith("/")) requestUrl = "/" + requestUrl;

        ApiErrorCode apiErrorCode = ApiErrorCode.URI_NOT_FOUND;
        ErrorResponse responseBody = new ErrorResponse(
                ApiErrorCode.URI_NOT_FOUND,
                messageResolver.getMessage(MessageKeys.EXCEPTION_URI_NOT_FOUND, requestUrl)
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
        logger.error("handleResourceNotFoundException", e);

        ApiErrorCode apiErrorCode = ApiErrorCode.RESOURCE_NOT_FOUND;
        String resourceSimpleName = e.getResourceClass().getSimpleName();

        String userResourceType = messageResolver.getMessage(MessageKeys.forResourceType(e.getResourceClass()));
        String userMessage = messageResolver.getMessage(MessageKeys.MESSAGE_RESOURCE_NOT_FOUND, userResourceType);

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND, resourceSimpleName, e.getIdentifier()),
                new TargetError(StringUtils.pascalToCamelCase(resourceSimpleName), userMessage)
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
        logger.error("handleHttpMessageNotReadableException", e);

        ApiErrorCode apiErrorCode = ApiErrorCode.MESSAGE_NOT_READABLE;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE)
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
        logger.error("handleResourceAlreadyExistsException", e);

        ApiErrorCode apiErrorCode = ApiErrorCode.ALREADY_EXISTS;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_RESOURCE_ALREADY_EXISTS, e.getResourceType()),
                new TargetError(e.getTarget(), messageResolver.getMessage(MessageKeys.MESSAGE_RESOURCE_ALREADY_EXISTS, e.getValue()))
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
        logger.error("handleDataAccessResourceFailureException", e);

        ApiErrorCode apiErrorCode = ApiErrorCode.UNAVAILABLE;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_SERVICE_UNAVAILABLE)
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        logger.error("handleOptimisticLockingFailureException", e);

        ApiErrorCode apiErrorCode = ApiErrorCode.CONFLICT;

        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage(MessageKeys.EXCEPTION_CONFLICT)
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }
}