package nl.kmartin.dartsmatcherapiv2.exceptionhandler;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ProcessingLimitReachedException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceAlreadyExistsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.ErrorResponse;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.WebSocketErrorResponse;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.WebsocketDestinations;
import nl.kmartin.dartsmatcherapiv2.utils.ErrorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;

@ControllerAdvice
public class GlobalWebsocketExceptionHandler {
    private final MessageResolver messageResolver;

    @Autowired
    public GlobalWebsocketExceptionHandler(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    //	 Handler for all unhandled exceptions.
    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleRunTimeException(Exception e, StompHeaderAccessor stompHeaderAccessor) {
        e.printStackTrace();

        return new WebSocketErrorResponse(
                ApiErrorCode.INTERNAL,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.internal")
        );
    }

    // Handler for bean validation errors thrown in controllers.
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, StompHeaderAccessor stompHeaderAccessor) {
        ArrayList<TargetError> errors = ErrorUtil.extractFieldErrors(e);

        return new WebSocketErrorResponse(
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage("exception.invalid.arguments"),
                stompHeaderAccessor.getDestination(),
                errors.toArray(new TargetError[0])
        );
    }

    // Handler for bean validation errors in services.
    @MessageExceptionHandler(ConstraintViolationException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleConstraintViolationException(ConstraintViolationException e, StompHeaderAccessor stompHeaderAccessor) {
        ArrayList<TargetError> errors = ErrorUtil.extractTargetErrors(e);

        return new WebSocketErrorResponse(
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage("exception.invalid.arguments"),
                stompHeaderAccessor.getDestination(),
                errors.toArray(new TargetError[0])
        );
    }

    // Handler for custom invalid arguments.
    @MessageExceptionHandler(InvalidArgumentsException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleInvalidArgumentException(InvalidArgumentsException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage("exception.invalid.arguments"),
                stompHeaderAccessor.getDestination(),
                e.getErrors().toArray(new TargetError[0])
        );
    }

    // Handler for resources that are not found.
    @MessageExceptionHandler(ResourceNotFoundException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleResourceNotFoundException(ResourceNotFoundException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.RESOURCE_NOT_FOUND,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.resource.not.found", e.getResourceType(), e.getIdentifier())
        );
    }

    // Handler for trying to create a resource when it already exists.
    @MessageExceptionHandler(ResourceAlreadyExistsException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleResourceAlreadyExistsException(ResourceAlreadyExistsException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.ALREADY_EXISTS,
                messageResolver.getMessage("exception.resource.already.exists", e.getResourceType()),
                stompHeaderAccessor.getDestination(),
                new TargetError(e.getTarget(), messageResolver.getMessage("message.resource.already.exists", e.getValue()))
        );
    }

    /**
     * Handler for reaching a processing limit.
     *
     * @param e ProcessingLimitReachedException The exception that was thrown
     * @return WebSocketErrorResponse containing the error details
     */
    @MessageExceptionHandler(ProcessingLimitReachedException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleProcessingLimitReachedException(ProcessingLimitReachedException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.PROCESSING_LIMIT_REACHED,
                messageResolver.getMessage("exception.processing.limit.reached", e.getResourceType(), e.getIdentifier()),
                stompHeaderAccessor.getDestination()
        );
    }

    /**
     * Handler for reaching a processing limit.
     *
     * @param e ProcessingLimitReachedException The exception that was thrown
     * @return ResponseEntity<ErrorResponse> containing the error details
     */
    @ExceptionHandler({ProcessingLimitReachedException.class})
    public ResponseEntity<ErrorResponse> handleProcessingLimitReachedException(ProcessingLimitReachedException e) {
        ApiErrorCode apiErrorCode = ApiErrorCode.PROCESSING_LIMIT_REACHED;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.processing.limit.reached")
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }

    // Handler for sending malformed data or invalid data types (e.g. invalid json, using array instead of string).
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(MethodArgumentTypeMismatchException e) {
        e.printStackTrace();

        ApiErrorCode apiErrorCode = ApiErrorCode.MESSAGE_NOT_READABLE;
        ErrorResponse responseBody = new ErrorResponse(
                apiErrorCode,
                messageResolver.getMessage("exception.body.not.readable")
        );

        return new ResponseEntity<>(responseBody, apiErrorCode.getHttpStatus());
    }


    //	 Handler for when a Message can't be deserialized to the corresponding object (e.g. object requires int but gets an array).
    @MessageExceptionHandler({MessageConversionException.class, ConversionFailedException.class})
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleMessageConversionException(Exception e, StompHeaderAccessor stompHeaderAccessor) {
        e.printStackTrace();

        return new WebSocketErrorResponse(
                ApiErrorCode.MESSAGE_NOT_READABLE,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.body.not.readable")
        );
    }

    // Handler for when the database is down.
    @MessageExceptionHandler(DataAccessResourceFailureException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleDataAccessResourceFailureException(DataAccessResourceFailureException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.UNAVAILABLE,
                stompHeaderAccessor.getDestination(),
                messageResolver.getMessage("exception.service.unavailable")
        );
    }
}