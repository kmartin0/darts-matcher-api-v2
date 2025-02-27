package nl.kmartin.dartsmatcherapiv2.exceptionhandler.response;

/**
 * @param target Key of the field.
 * @param error  Error description describing the error to an end user.
 */
public record TargetError(String target, String error) {
}
