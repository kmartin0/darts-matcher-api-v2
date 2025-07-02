package nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception;

import lombok.Getter;

@Getter
public class ProcessingLimitReachedException extends RuntimeException {
    // The resource type the limit was reached on
    private final String resourceType;

    // The id of the object the limit was reached on.
    private final String identifier;

    public ProcessingLimitReachedException(Class<?> resourceType, String identifier) {
        this.resourceType = resourceType.getSimpleName();
        this.identifier = identifier;
    }
}
