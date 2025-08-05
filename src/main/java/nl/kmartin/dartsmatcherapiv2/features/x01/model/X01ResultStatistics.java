package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01ResultStatistics {
    private int setsWon;
    private int legsWon;

    public void reset() {
        this.setsWon = 0;
        this.legsWon = 0;
    }
}
