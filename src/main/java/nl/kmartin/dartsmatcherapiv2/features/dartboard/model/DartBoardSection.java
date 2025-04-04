package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

public enum DartBoardSection {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    ELEVEN(11),
    TWELVE(12),
    THIRTEEN(13),
    FOURTEEN(14),
    FIFTEEN(15),
    SIXTEEN(16),
    SEVENTEEN(17),
    EIGHTEEN(18),
    NINETEEN(19),
    TWENTY(20),
    BULL(25),
    MISS(0);

    private final int sectionNumber;

    DartBoardSection(int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public int getSectionNumber() {
        return sectionNumber;
    }

    public int getScore(DartboardSectionArea area) {
        return sectionNumber * area.getMultiplier();
    }
}