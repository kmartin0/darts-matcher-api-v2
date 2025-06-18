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
    public X01EditTurn(int score, int dartsUsed, int doublesMissed, ObjectId playerId, int set, int leg, int round) {
        super(score, dartsUsed, doublesMissed);
        this.playerId = playerId;
        this.set = set;
        this.leg = leg;
        this.round = round;
    }

    @NotNull
    private ObjectId playerId;

    @Min(1)
    private int set;

    @Min(1)
    private int leg;

    @Min(1)
    private int round;
}