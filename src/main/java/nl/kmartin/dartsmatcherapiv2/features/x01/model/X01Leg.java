package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.NavigableMap;
import java.util.TreeMap;

@Data
@NoArgsConstructor
public class X01Leg {
    private ObjectId winner;

    @NotNull
    private ObjectId throwsFirst;

    @Min(1)
    @Max(3)
    private Integer checkoutDartsUsed;

    @Valid
    private NavigableMap<Integer, X01LegRound> rounds = new TreeMap<>();

    public X01Leg(ObjectId winner, ObjectId throwsFirst, NavigableMap<Integer, X01LegRound> rounds) {
        this.winner = winner;
        this.throwsFirst = throwsFirst;
        this.setRounds(rounds);
    }

    public void setRounds(@Valid NavigableMap<Integer, X01LegRound> rounds) {
        this.rounds = rounds != null ? rounds : new TreeMap<>();
    }
}
