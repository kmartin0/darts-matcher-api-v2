package nl.kmartin.dartsmatcherapiv2.validators.validx01dartbotsettings;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapiv2.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class ValidX01DartBotSettingsValidator implements ConstraintValidator<ValidX01DartBotSettings, X01MatchPlayer> {
    @Override
    public boolean isValid(X01MatchPlayer x01MatchPlayer, ConstraintValidatorContext constraintValidatorContext) {
        // null values are handled by other validators (@NotNull)
        if (x01MatchPlayer == null) {
            return true;
        }

        return validateDartBotSettings(x01MatchPlayer, constraintValidatorContext);
    }

    private boolean validateDartBotSettings(X01MatchPlayer x01MatchPlayer, ConstraintValidatorContext constraintValidatorContext) {
        PlayerType playerType = x01MatchPlayer.getPlayerType();
        X01DartBotSettings dartBotSettings = x01MatchPlayer.getX01DartBotSettings();

        // Null player type should be handled by other validators (@NotNull)
        if (playerType == null) return true;

        return switch (playerType) {
            case HUMAN -> {
                if (dartBotSettings != null) {
                    setDartBotSettingsConstraintViolation("{" + MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_HUMAN + "}", constraintValidatorContext);
                    yield false;
                }
                yield true;
            }
            case DART_BOT -> {
                if (dartBotSettings == null) {
                    setDartBotSettingsConstraintViolation("{" + MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_BOT_MISSING + "}", constraintValidatorContext);
                    yield false;
                }
                yield true;
            }
        };
    }

    private void setDartBotSettingsConstraintViolation(String message, ConstraintValidatorContext constraintContext) {
        // Unwrap the HibernateConstraintValidatorContext to access Hibernate-specific methods
        HibernateConstraintValidatorContext hibernateContext = constraintContext.unwrap(HibernateConstraintValidatorContext.class);

        // Disable the default violation message
        hibernateContext.disableDefaultConstraintViolation();

        // Build and add the custom violation message to the dart bot settings property
        hibernateContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}