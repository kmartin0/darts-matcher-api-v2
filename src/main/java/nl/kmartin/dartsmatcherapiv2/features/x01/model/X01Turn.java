package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Turn {
    @NotNull
    private ObjectId matchId;

    @Min(0)
    int score;

    @Min(1)
    @Max(3)
    int dartsUsed;

    @Min(0)
    @Max(3)
    int doublesMissed;
}