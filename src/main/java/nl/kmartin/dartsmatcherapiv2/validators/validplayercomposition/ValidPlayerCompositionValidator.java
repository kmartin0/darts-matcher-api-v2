package nl.kmartin.dartsmatcherapiv2.validators.validplayercomposition;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapiv2.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ValidPlayerCompositionValidator implements ConstraintValidator<ValidPlayerComposition, List<? extends MatchPlayer>> {

    /**
     * Validates the composition of the player list based on the following rules:
     * - A match can have a maximum of one bot.
     * - If a match contains a bot, it must also contain at least one human player.
     *
     * The validation passes if the list is null or empty, as these cases are handled by other annotations
     *
     * @param matchPlayers      The list of players to validate.
     * @param constraintContext The context in which the constraint is evaluated.
     * @return true if the player composition is valid, false otherwise.
     */
    @Override
    public boolean isValid(List<? extends MatchPlayer> matchPlayers, ConstraintValidatorContext constraintContext) {
        if (CollectionUtils.isEmpty(matchPlayers)) return true;

        long botCount = matchPlayers.stream()
                .filter(matchPlayer -> PlayerType.DART_BOT.equals(matchPlayer.getPlayerType()))
                .count();

        if (botCount > 1) {
            setViolationMessage(constraintContext, MessageKeys.MESSAGE_TOO_MANY_BOTS);
            return false;
        }

        if (botCount == 1 && matchPlayers.size() == 1) {
            setViolationMessage(constraintContext, MessageKeys.MESSAGE_BOT_REQUIRES_HUMAN);
            return false;
        }

        return true;
    }

    private void setViolationMessage(ConstraintValidatorContext constraintContext, String messageKey) {
        // Unwrap the HibernateConstraintValidatorContext to access Hibernate-specific methods
        HibernateConstraintValidatorContext hibernateContext = constraintContext.unwrap(HibernateConstraintValidatorContext.class);

        // Disable the default violation message
        hibernateContext.disableDefaultConstraintViolation();

        // Build and add the custom violation message with conflicting player names
        hibernateContext
                .buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addConstraintViolation();
    }
}
