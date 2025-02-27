package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRoundScore {
    private ObjectId playerId;
    private int doublesMissed;
    private int dartsUsed;
    private int score;
    private int remaining;
}
