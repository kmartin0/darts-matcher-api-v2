package nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception;

import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {
	// the type of the requested resource.
	private final String resourceType;

	// the field that causes the exception.
	private final String target;

	// the value that caused the exception.
	private final Object value;

	public ResourceAlreadyExistsException(Class<?> resourceType, String target, Object value) {
		this.resourceType = resourceType.getSimpleName();
		this.target = target;
		this.value = value;
	}
}