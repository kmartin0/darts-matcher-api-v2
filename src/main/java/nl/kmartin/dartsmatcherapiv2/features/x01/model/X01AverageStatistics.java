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
}
