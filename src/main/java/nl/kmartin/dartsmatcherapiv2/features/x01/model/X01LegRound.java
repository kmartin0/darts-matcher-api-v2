package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.LinkedHashMap;

@Data
@NoArgsConstructor
public class X01LegRound {
    @Valid
    private LinkedHashMap<ObjectId, X01LegRoundScore> scores = new LinkedHashMap<>();

    public X01LegRound(LinkedHashMap<ObjectId, X01LegRoundScore> scores) {
        this.setScores(scores);
    }

    public void setScores(@Valid LinkedHashMap<ObjectId, X01LegRoundScore> scores) {
        this.scores = scores != null ? scores : new LinkedHashMap<>();
    }
}
