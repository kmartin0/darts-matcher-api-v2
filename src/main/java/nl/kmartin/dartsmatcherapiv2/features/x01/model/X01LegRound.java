package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class X01LegRound {
    @Min(0)
    private int round;

    @Valid
    private Map<ObjectId, X01LegRoundScore> scores = new HashMap<>();

    public X01LegRound(int round, Map<ObjectId, X01LegRoundScore> scores) {
        this.round = round;
        this.setScores(scores);
    }

    public void setScores(@Valid Map<ObjectId, X01LegRoundScore> scores) {
        this.scores = scores != null ? scores : new HashMap<>();
    }
}
