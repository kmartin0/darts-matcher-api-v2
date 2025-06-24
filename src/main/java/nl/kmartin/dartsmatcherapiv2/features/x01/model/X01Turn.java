package nl.kmartin.dartsmatcherapiv2.features.x01.model;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.validators.validdartscore.ValidDartScore;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Turn {
    @ValidDartScore
    int score;

    @Min(1)
    @Max(3)
    Integer checkoutDartsUsed;

    @Min(0)
    @Max(3)
    Integer doublesMissed;
}