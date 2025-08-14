package nl.kmartin.dartsmatcherapiv2.features.x01.model;

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
    private Integer doublesMissed;

    @Min(0)
    @Max(180)
    private int score;

    @Min(0)
    private int remaining;

    public X01LegRoundScore(X01Turn turn, boolean trackDoubles) {
        this.doublesMissed = trackDoubles
                ? (turn.getDoublesMissed() != null ? turn.getDoublesMissed() : 0)
                : null;
        this.score = turn.getScore();
    }
}
