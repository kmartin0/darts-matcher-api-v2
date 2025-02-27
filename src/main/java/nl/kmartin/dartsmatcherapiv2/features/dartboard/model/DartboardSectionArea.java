package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

public enum DartboardSectionArea {
    DOUBLE_BULL, SINGLE_BULL, INNER_SINGLE, TRIPLE, OUTER_SINGLE, DOUBLE, MISS;

    public int getMultiplier() {
        switch (this) {
            case INNER_SINGLE:
            case OUTER_SINGLE:
            case SINGLE_BULL:
            case DOUBLE_BULL:
                return 1;

            case DOUBLE:
                return 2;

            case TRIPLE:
                return 3;

            default:
                return 0;
        }
    }
}
