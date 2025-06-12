package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRoundScore {
    @Min(0)
    @Max(3)
    private int doublesMissed;

    @Min(1)
    @Max(3)
    private int dartsUsed;

    @Min(0)
    @Max(180)
    private int score;

    @JsonIgnore
    public int getDartsLeft() {
        return Math.max(0, 3 - this.getDartsUsed());
    }
}
