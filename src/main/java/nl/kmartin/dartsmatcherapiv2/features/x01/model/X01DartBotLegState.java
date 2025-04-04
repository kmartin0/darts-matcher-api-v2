package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotLegState {
    private int x01;
    private int scoredInLeg;
    private int dartsUsedInLeg;
    private int targetNumOfDarts;
    private double targetOneDartAvg;
    private X01LegRoundScore legRoundScore;

    public int getRemainingPoints() {
        return x01 - scoredInLeg - legRoundScore.getScore();
    }

    public int getScoredInLeg() {
        return scoredInLeg + legRoundScore.getScore();
    }

    public int getDartsUsedInLeg() {
        return dartsUsedInLeg + legRoundScore.getDartsUsed();
    }

    public double getCurrentOneDartAvg() {
        // When no darts were thrown, return zero to prevent division by zero
        if (getDartsUsedInLeg() == 0) return 0;

        // Calculate and return the one-dart average.
        return (double) getScoredInLeg() / getDartsUsedInLeg();
    }
}