package nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
	// the type of the requested resource.
	private final String resourceType;

	// The id of the requested resource.
	private final Object identifier;

	public ResourceNotFoundException(Class<?> resourceType, Object identifier) {
		this.resourceType = resourceType.getSimpleName();
		this.identifier = identifier;
	}
}