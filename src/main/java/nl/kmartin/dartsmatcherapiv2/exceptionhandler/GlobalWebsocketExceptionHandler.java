package nl.kmartin.dartsmatcherapiv2.exceptionhandler;

import jakarta.validation.ConstraintViolationException;
import nl.kmartin.dartsmatcherapiv2.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.common.WebsocketDestinations;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceAlreadyExistsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.ApiErrorCode;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.WebSocketErrorResponse;
import nl.kmartin.dartsmatcherapiv2.utils.ErrorUtil;
import nl.kmartin.dartsmatcherapiv2.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
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
                messageResolver.getMessage(MessageKeys.EXCEPTION_INTERNAL),
                stompHeaderAccessor.getDestination()
        );
    }

    // Handler for bean validation errors thrown in controllers.
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, StompHeaderAccessor stompHeaderAccessor) {
        ArrayList<TargetError> errors = ErrorUtil.extractFieldErrors(e);

        return new WebSocketErrorResponse(
                ApiErrorCode.INVALID_ARGUMENTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
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
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
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
                messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS),
                stompHeaderAccessor.getDestination(),
                e.getErrors().toArray(new TargetError[0])
        );
    }

    // Handler for resources that are not found.
    @MessageExceptionHandler(ResourceNotFoundException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleResourceNotFoundException(ResourceNotFoundException e, StompHeaderAccessor stompHeaderAccessor) {
        String resourceSimpleName = e.getResourceClass().getSimpleName();
        String userResourceType = messageResolver.getMessage(MessageKeys.forResourceType(e.getResourceClass()));
        String userMessage = messageResolver.getMessage(MessageKeys.MESSAGE_RESOURCE_NOT_FOUND, userResourceType);

        return new WebSocketErrorResponse(
                ApiErrorCode.RESOURCE_NOT_FOUND,
                messageResolver.getMessage(MessageKeys.EXCEPTION_RESOURCE_NOT_FOUND, resourceSimpleName, e.getIdentifier()),
                stompHeaderAccessor.getDestination(),
                new TargetError(StringUtils.pascalToCamelCase(resourceSimpleName), userMessage)
        );
    }

    // Handler for trying to create a resource when it already exists.
    @MessageExceptionHandler(ResourceAlreadyExistsException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleResourceAlreadyExistsException(ResourceAlreadyExistsException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.ALREADY_EXISTS,
                messageResolver.getMessage(MessageKeys.EXCEPTION_RESOURCE_ALREADY_EXISTS, e.getResourceType()),
                stompHeaderAccessor.getDestination(),
                new TargetError(e.getTarget(), messageResolver.getMessage(MessageKeys.MESSAGE_RESOURCE_ALREADY_EXISTS, e.getValue()))
        );
    }

    // Handler for sending malformed data or invalid data types (e.g. invalid json, using array instead of string).
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleHttpMessageNotReadableException(MethodArgumentTypeMismatchException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.MESSAGE_NOT_READABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                stompHeaderAccessor.getDestination()
        );
    }

    //	 Handler for when a Message can't be deserialized to the corresponding object (e.g. object requires int but gets an array).
    @MessageExceptionHandler({MessageConversionException.class, ConversionFailedException.class})
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleMessageConversionException(Exception e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.MESSAGE_NOT_READABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_BODY_NOT_READABLE),
                stompHeaderAccessor.getDestination()
        );
    }

    // Handler for when the database is down.
    @MessageExceptionHandler(DataAccessResourceFailureException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleDataAccessResourceFailureException(DataAccessResourceFailureException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.UNAVAILABLE,
                messageResolver.getMessage(MessageKeys.EXCEPTION_SERVICE_UNAVAILABLE),
                stompHeaderAccessor.getDestination()
        );
    }

    @MessageExceptionHandler(OptimisticLockingFailureException.class)
    @SendToUser(destinations = WebsocketDestinations.ERROR_QUEUE, broadcast = false)
    public WebSocketErrorResponse handleOptimisticLockingFailureException(OptimisticLockingFailureException e, StompHeaderAccessor stompHeaderAccessor) {

        return new WebSocketErrorResponse(
                ApiErrorCode.CONFLICT,
                messageResolver.getMessage(MessageKeys.EXCEPTION_CONFLICT),
                stompHeaderAccessor.getDestination()
        );
    }
}