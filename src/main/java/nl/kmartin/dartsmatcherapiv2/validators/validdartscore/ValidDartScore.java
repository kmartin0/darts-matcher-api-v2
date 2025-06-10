package nl.kmartin.dartsmatcherapiv2.validators.validdartscore;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidDartScoreValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDartScore {
    String message() default "{message.x01.invalid.score}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
