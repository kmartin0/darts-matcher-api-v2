package nl.kmartin.dartsmatcherapiv2.validators.validdartscore;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class ValidDartScoreValidator implements ConstraintValidator<ValidDartScore, Integer> {

    private static final Set<Integer> IMPOSSIBLE_SCORES = Set.of(179, 178, 176, 175, 173, 172, 169, 166, 163);

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // Allow null scores to be handled by @NotNull
        if (value == null) return true;

        if (value < 0 || value > 180) return false;
        return !IMPOSSIBLE_SCORES.contains(value);
    }
}
