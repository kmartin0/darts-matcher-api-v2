package nl.kmartin.dartsmatcherapiv2.validators;

import jakarta.validation.ConstraintValidatorContext;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.utils.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.validators.noduplicatematchplayername.NoDuplicateMatchPlayerNameValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NoDuplicateMatchPlayerNameValidatorTests {
    private NoDuplicateMatchPlayerNameValidator noDuplicateMatchPlayerNameValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private HibernateConstraintValidatorContext hibernateContext;

    @Mock
    private HibernateConstraintViolationBuilder hibernateConstraintViolationBuilder;

    @BeforeEach
    void setup() {
        noDuplicateMatchPlayerNameValidator = new NoDuplicateMatchPlayerNameValidator();
    }

    @Test
    void testUniquePlayerNames_returnTrue() {
        List<MatchPlayer> players = new ArrayList<>();
        players.add(new MatchPlayer(null, "John Doe", null, null));
        players.add(new MatchPlayer(null, "Jane Doe", null, null));
        players.add(new MatchPlayer(null, "Joe Doe", null, null));

        Assertions.assertTrue(noDuplicateMatchPlayerNameValidator.isValid(players, null));
    }

    @Test
    void testDuplicatePlayerNames_returnFalse() {
        // Given
        List<MatchPlayer> players = new ArrayList<>(Arrays.asList(
                createMatchPlayer("John Doe"),
                createMatchPlayer("Jane Doe"),
                createMatchPlayer("John Doe")
        ));

        when(constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(hibernateContext);
        when(hibernateContext.addMessageParameter(anyString(), anyString())).thenReturn(hibernateContext);
        when(hibernateContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(hibernateConstraintViolationBuilder);
        when(hibernateConstraintViolationBuilder.addConstraintViolation()).thenReturn(hibernateContext);

        // When
        boolean result = noDuplicateMatchPlayerNameValidator.isValid(players, constraintValidatorContext);

        // Then
        assertFalse(result);
        verify(hibernateContext).addMessageParameter(MessageKeys.Params.NAME, "John Doe");
        verify(hibernateContext).buildConstraintViolationWithTemplate("{" + MessageKeys.MESSAGE_PLAYER_NAME_DUPLICATE + "}");
        verify(hibernateConstraintViolationBuilder).addConstraintViolation();
    }

    private MatchPlayer createMatchPlayer(String playerName) {
        return new MatchPlayer(null, playerName, null, null);
    }

}