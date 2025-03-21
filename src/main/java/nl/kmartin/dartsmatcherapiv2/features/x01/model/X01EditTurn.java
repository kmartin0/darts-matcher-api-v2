package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class X01EditTurn extends X01Turn {
    public X01EditTurn(ObjectId matchId, int score, int dartsUsed, int doublesMissed, ObjectId playerId, int set, int leg, int round) {
        super(matchId, score, dartsUsed, doublesMissed);
        this.playerId = playerId;
        this.set = set;
        this.leg = leg;
        this.round = round;
    }

    @NotNull
    private ObjectId playerId;

    @Min(0)
    private int set;

    @Min(0)
    private int leg;

    @Min(0)
    private int round;
}