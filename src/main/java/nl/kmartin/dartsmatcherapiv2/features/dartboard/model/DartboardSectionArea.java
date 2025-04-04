package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

import lombok.Getter;

@Getter
public enum DartboardSectionArea {
    DOUBLE_BULL(2), SINGLE_BULL(1), INNER_SINGLE(1), TRIPLE(3), OUTER_SINGLE(1), DOUBLE(2), MISS(0);

    private final int multiplier;

    DartboardSectionArea(int multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isSingle() {
        return this == INNER_SINGLE || this == OUTER_SINGLE || this == SINGLE_BULL;
    }

    public boolean isDouble() {
        return this == DOUBLE || this == DOUBLE_BULL;
    }

    public boolean isTriple() {
        return this == TRIPLE;
    }
}
