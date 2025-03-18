package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01ScoreStatistics {
    private int fortyPlus;
    private int sixtyPlus;
    private int eightyPlus;
    private int tonPlus;
    private int tonFortyPlus;
    private int tonEighty;

    public void incrementFortyPlus() {
        this.fortyPlus++;
    }

    public void incrementSixtyPlus() {
        this.sixtyPlus++;
    }

    public void incrementEightyPlus() {
        this.eightyPlus++;
    }

    public void incrementTonPlus() {
        this.tonPlus++;
    }

    public void incrementTonFortyPlus() {
        this.tonFortyPlus++;
    }

    public void incrementTonEighty() {
        this.tonEighty++;
    }

    public void reset() {
        this.fortyPlus = 0;
        this.sixtyPlus = 0;
        this.eightyPlus = 0;
        this.tonPlus = 0;
        this.tonFortyPlus = 0;
        this.tonEighty = 0;
    }
}