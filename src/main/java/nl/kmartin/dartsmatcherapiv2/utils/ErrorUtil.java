package nl.kmartin.dartsmatcherapiv2.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;

public class ErrorUtil {
    private ErrorUtil() {
    }

    public static ArrayList<TargetError> extractTargetErrors(ConstraintViolationException e) {
        ArrayList<TargetError> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            ArrayList<Path.Node> violationPropertyPaths = new ArrayList<>();
            violation.getPropertyPath().iterator().forEachRemaining(violationPropertyPaths::add);

            StringBuilder errorPath = new StringBuilder();
            int startIndex = violationPropertyPaths.size() > 2 ? 2 : 0;

            for (int i = startIndex; i < violationPropertyPaths.size(); i++) {
                errorPath.append(violationPropertyPaths.get(i));
                if (i < violationPropertyPaths.size() - 1) errorPath.append(".");
            }

            errors.add(new TargetError(errorPath.toString(), violation.getMessage()));
        }

        return errors;
    }

    // For REST exceptions
    public static ArrayList<TargetError> extractFieldErrors(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        return extractErrors(ex.getBindingResult());
    }

    // For WebSocket exceptions
    public static ArrayList<TargetError> extractFieldErrors(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException ex) {
        return ex.getBindingResult() != null ? extractErrors(ex.getBindingResult()) : new ArrayList<>();
    }

    private static ArrayList<TargetError> extractErrors(BindingResult bindingResult) {
        ArrayList<TargetError> errors = new ArrayList<>();
        if (bindingResult != null) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError) {
                    errors.add(new TargetError(((FieldError) error).getField(), error.getDefaultMessage()));
                } else {
                    errors.add(new TargetError(error.getCode(), error.getDefaultMessage()));
                }
            }
        }

        return errors;
    }
}
