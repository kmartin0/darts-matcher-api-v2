package nl.kmartin.dartsmatcherapiv2.validators;

import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapiv2.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.validators.x01dartsbotsettings.ValidX01DartBotSettingsValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidX01DartBotSettingsValidatorTests {
    private ValidX01DartBotSettingsValidator validX01DartBotSettingsValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private HibernateConstraintValidatorContext hibernateContext;

    @Mock
    private HibernateConstraintViolationBuilder hibernateConstraintViolationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderContext;

    @BeforeEach
    void setup() {
        validX01DartBotSettingsValidator = new ValidX01DartBotSettingsValidator();
    }

    @Test
    public void testDartBotWithSettings_returnTrue() {
        // Given
        X01MatchPlayer x01MatchPlayer = new X01MatchPlayer(
                null, null, PlayerType.DART_BOT,
                null,
                new X01DartBotSettings(26), null
        );

        // When
        boolean result = validX01DartBotSettingsValidator.isValid(x01MatchPlayer, constraintValidatorContext);

        // Then
        assertTrue(result);
    }

    @Test
    public void testDartBotWithoutSettings_returnFalse() {
        // Given
        X01MatchPlayer x01MatchPlayer = new X01MatchPlayer(
                null, null, PlayerType.DART_BOT,
                null,
                null, null
        );

        when(constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(hibernateContext);
        when(hibernateContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(hibernateConstraintViolationBuilder);
        when(hibernateConstraintViolationBuilder.addConstraintViolation()).thenReturn(hibernateContext);

        // When
        boolean result = validX01DartBotSettingsValidator.isValid(x01MatchPlayer, constraintValidatorContext);

        // Then
        assertFalse(result);
        verify(hibernateContext).buildConstraintViolationWithTemplate("{" + MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_BOT_MISSING + "}");
        verify(hibernateConstraintViolationBuilder).addConstraintViolation();
    }

    @Test
    public void testDartHumanWithoutSettings_returnTrue() {
        // Given
        X01MatchPlayer x01MatchPlayer = new X01MatchPlayer(
                null, null, PlayerType.HUMAN,
                null,
                null, null
        );

        // When
        boolean result = validX01DartBotSettingsValidator.isValid(x01MatchPlayer, constraintValidatorContext);

        // Then
        assertTrue(result);
    }

    @Test
    public void testHumanWithSettings_returnFalse() {
        // Given
        X01MatchPlayer x01MatchPlayer = new X01MatchPlayer(
                null, null, PlayerType.HUMAN,
                null,
                new X01DartBotSettings(26), null
        );

        when(constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(hibernateContext);
        when(hibernateContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(hibernateConstraintViolationBuilder);
        when(hibernateConstraintViolationBuilder.addConstraintViolation()).thenReturn(hibernateContext);

        // When
        boolean result = validX01DartBotSettingsValidator.isValid(x01MatchPlayer, constraintValidatorContext);

        // Then
        assertFalse(result);
        verify(hibernateContext).buildConstraintViolationWithTemplate("{" + MessageKeys.MESSAGE_X01_DART_BOT_SETTINGS_HUMAN + "}");
        verify(hibernateConstraintViolationBuilder).addConstraintViolation();
    }

}
