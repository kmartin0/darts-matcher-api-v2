package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01AverageStatistics {
    private int pointsThrown;
    private int dartsThrown;
    private int average;
    private int pointsThrownFirstNine;
    private int dartsThrownFirstNine;
    private int averageFirstNine;

    public void reset() {
        this.pointsThrown = 0;
        this.dartsThrown = 0;
        this.average = 0;
        this.pointsThrownFirstNine = 0;
        this.dartsThrownFirstNine = 0;
        this.averageFirstNine = 0;
    }
}