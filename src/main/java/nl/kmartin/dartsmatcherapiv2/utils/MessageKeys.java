package nl.kmartin.dartsmatcherapiv2.utils;

public class MessageKeys {
    private MessageKeys() {
    }

    /**
     * Bean Validation Messages (wrapped with {})
     */
    public static final String VALIDATION_NOT_NULL = "{javax.validation.constraints.NotNull.message}";
    public static final String VALIDATION_NOT_BLANK = "{javax.validation.constraints.NotBlank.message}";
    public static final String VALIDATION_NULL = "{javax.validation.constraints.Null.message}";
    public static final String VALIDATION_EMAIL = "{javax.validation.constraints.Email.message}";
    public static final String VALIDATION_NOT_EMPTY = "{javax.validation.constraints.NotEmpty.message}";
    public static final String VALIDATION_MIN = "{javax.validation.constraints.Min.message}";
    public static final String VALIDATION_MAX = "{javax.validation.constraints.Max.message}";
    public static final String VALIDATION_LENGTH = "{org.hibernate.validator.constraints.Length.message}";

    /**
     * User-facing Messages (message.*)
     */
    //Parameters: 0 - resource name
    public static final String MESSAGE_RESOURCE_ALREADY_EXISTS = "message.resource.already.exists";
    public static final String MESSAGE_NO_WHITESPACE_ALLOWED = "message.no.whitespace.allowed";
    // Named parameter: {name}
    public static final String MESSAGE_PLAYER_NAME_NOT_ALLOWED = "message.player.name.not.allowed";
    // Named parameter: {name}
    public static final String MESSAGE_PLAYER_NAME_DUPLICATE = "message.player.name.duplicate";
    // Parameters: 0 (score)
    public static final String MESSAGE_IMPOSSIBLE_CHECKOUT = "message.impossible.checkout";
    // Parameters: 0 (score), 1 (darts)
    public static final String MESSAGE_IMPOSSIBLE_CHECKOUT_MIN_DARTS = "message.impossible.checkout.min.darts";
    public static final String MESSAGE_LEG_ALREADY_WON = "message.leg.already.won";
    public static final String MESSAGE_X01_DART_BOT_SETTINGS_HUMAN = "message.x01.dart.bot.settings.human";
    public static final String MESSAGE_X01_DART_BOT_SETTINGS_BOT_MISSING = "message.x01.dart.bot.settings.bot.missing";

    /**
     * Developer-facing Exceptions (exception.*)
     */
    public static final String EXCEPTION_INTERNAL = "exception.internal";
    public static final String EXCEPTION_INVALID_ARGUMENTS = "exception.invalid.arguments";
    // Parameters: 0 (URI path)
    public static final String EXCEPTION_URI_NOT_FOUND = "exception.uri.not.found";
    // Parameters: 0 (resource type), 1 (identifier)
    public static final String EXCEPTION_RESOURCE_NOT_FOUND = "exception.resource.not.found";
    public static final String EXCEPTION_BODY_NOT_READABLE = "exception.body.not.readable";
    // Parameters: 0 (resource type)
    public static final String EXCEPTION_RESOURCE_ALREADY_EXISTS = "exception.resource.already.exists";
    public static final String EXCEPTION_SERVICE_UNAVAILABLE = "exception.service.unavailable";
    public static final String EXCEPTION_FORBIDDEN = "exception.forbidden";

    public static class Params {
        public static final String NAME = "name";
        public static final String SCORE = "score";
        public static final String DARTS = "darts";
    }
}