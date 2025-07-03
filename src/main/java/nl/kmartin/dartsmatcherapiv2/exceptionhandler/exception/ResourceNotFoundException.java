package nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
	// the type of the requested resource.
	private final Class<?> resourceClass;

	// The id of the requested resource.
	private final Object identifier;

	public ResourceNotFoundException(Class<?> resourceClass, Object identifier) {
		this.resourceClass = resourceClass;
		this.identifier = identifier;
	}
}