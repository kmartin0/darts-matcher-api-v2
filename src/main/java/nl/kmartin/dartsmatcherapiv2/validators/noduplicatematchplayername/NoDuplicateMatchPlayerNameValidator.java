package nl.kmartin.dartsmatcherapiv2.validators.noduplicatematchplayername;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.utils.MessageKeys;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoDuplicateMatchPlayerNameValidator implements ConstraintValidator<NoDuplicateMatchPlayerName, List<? extends MatchPlayer>> {

    @Override
    public void initialize(NoDuplicateMatchPlayerName constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<? extends MatchPlayer> matchPlayers, ConstraintValidatorContext constraintContext) {
        // null values are handled by other validators (@NotNull)
        if (matchPlayers == null) {
            return true;
        }

        return this.arePlayerNamesUnique(matchPlayers, constraintContext);
    }

    private boolean arePlayerNamesUnique(List<? extends MatchPlayer> matchPlayers, ConstraintValidatorContext constraintContext) {
        // Set to track unique player names.
        Set<String> playerNames = new HashSet<>();

        // Add the player names to the set and use the result to determine if the player name already exists.
        for (MatchPlayer player : matchPlayers) {
            String playerName = player.getPlayerName();

            // null or empty values are handled by other validators.
            if (playerName == null || playerName.isEmpty()) {
                continue;
            }

            // Check if the name is already in the set (duplicate)
            if (!playerNames.add(playerName)) {
                setDuplicateNameViolationMessage(playerName, constraintContext);

                return false;
            }
        }
        return true;
    }

    private void setDuplicateNameViolationMessage(String duplicateName, ConstraintValidatorContext constraintContext) {
        // Unwrap the HibernateConstraintValidatorContext to access Hibernate-specific methods
        HibernateConstraintValidatorContext hibernateContext = constraintContext.unwrap(HibernateConstraintValidatorContext.class);

        // Disable the default violation message
        hibernateContext.disableDefaultConstraintViolation();

        // Build and add the custom violation message with conflicting player names
        hibernateContext
                .addMessageParameter(MessageKeys.Params.NAME, duplicateName)
                .buildConstraintViolationWithTemplate("{" + MessageKeys.MESSAGE_PLAYER_NAME_DUPLICATE + "}")
                .addConstraintViolation();
    }
}