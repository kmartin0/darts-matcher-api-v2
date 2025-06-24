package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class X01Leg {
    @Min(1)
    private int leg;

    private ObjectId winner;

    @NotNull
    private ObjectId throwsFirst;

    @Min(1)
    @Max(3)
    private Integer checkoutDartsUsed;

    @Valid
    private ArrayList<X01LegRound> rounds = new ArrayList<>();

    public X01Leg(int leg, ObjectId winner, ObjectId throwsFirst, ArrayList<X01LegRound> rounds) {
        this.leg = leg;
        this.winner = winner;
        this.throwsFirst = throwsFirst;
        this.setRounds(rounds);
    }

    public void setRounds(@Valid ArrayList<X01LegRound> rounds) {
        this.rounds = rounds != null ? rounds : new ArrayList<>();
    }
}
