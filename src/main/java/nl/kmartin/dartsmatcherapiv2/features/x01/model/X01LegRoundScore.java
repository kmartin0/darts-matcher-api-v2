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
public class X01LegRoundScore {
    @NotNull
    private ObjectId playerId;

    @Min(0)
    @Max(3)
    private int doublesMissed;

    @Min(1)
    @Max(3)
    private int dartsUsed;

    @Min(0)
    @Max(180)
    private int score;

    @Min(0)
    private int remaining;
}
