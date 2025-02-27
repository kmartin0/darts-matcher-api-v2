package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;

@Data
@AllArgsConstructor
public class Dartboard {
    // Sections starting at 6 counting counterclockwise. Reasoning is that polar planes
    // the convention is to start at 0 degrees on the x-axis and then increases counterclockwise.
    private final ArrayList<Integer> sections = new ArrayList<>(
            Arrays.asList(6, 13, 4, 18, 1, 20, 5, 12, 9, 14, 11, 8, 16, 7, 19, 3, 17, 2, 15, 10, 6)
    );

    private final ArrayList<DartboardSectionAreaDimen> areaDimensions = new ArrayList<>(Arrays.asList(
            new DartboardSectionAreaDimen(DartboardSectionArea.DOUBLE_BULL, 0, 7),
            new DartboardSectionAreaDimen(DartboardSectionArea.SINGLE_BULL, 7, 17),
            new DartboardSectionAreaDimen(DartboardSectionArea.INNER_SINGLE, 17, 97),
            new DartboardSectionAreaDimen(DartboardSectionArea.TRIPLE, 97, 107),
            new DartboardSectionAreaDimen(DartboardSectionArea.OUTER_SINGLE, 107, 160),
            new DartboardSectionAreaDimen(DartboardSectionArea.DOUBLE, 160, 170),
            new DartboardSectionAreaDimen(DartboardSectionArea.MISS, 170, Integer.MAX_VALUE)
    ));

    /**
     * @param cartesianCoordinate CartesianCoordinate The cartesian coordinates of which a score needs to be calculated.
     * @return int The score corresponding the polar coordinates.
     */
    public Dart getScoreCartesian(CartesianCoordinate cartesianCoordinate) {

        return getScorePolar(PolarCoordinate.fromCartesian(cartesianCoordinate));
    }

    /**
     * @param polarCoordinate PolarCoordinate the polar coordinates of a score.
     * @return int The score corresponding the polar coordinates.
     */
    public Dart getScorePolar(PolarCoordinate polarCoordinate) {
        int section = getSection(polarCoordinate.getThetaNormalized());
        DartboardSectionArea sectionArea = getSectionArea(polarCoordinate.getR());

        int score;
        switch (sectionArea) {
            case DOUBLE_BULL:
                score = 50;
                break;
            case SINGLE_BULL:
                score = 25;
                break;
            default:
                score = section;
        }

        // Return the score multiplied by the section area multiplier.
        return new Dart(score, sectionArea);
    }

    /**
     * Gets the scoring section for a given angle (theta) in rad. Ties are broken clockwise (i.e. when theta is on the wire between 18 and 4 the score is 4).
     *
     * @param theta double The angle in rad of which the section needs to be determined.
     * @return int The section of the board.
     */
    public int getSection(double theta) {

        for (int i = 0; i < sections.size(); i++) {
            if (theta <= getSectionTheta(i)) {
                return sections.get(i);
            }
        }

        return 0;
    }

    /**
     * @param sectorIndex int the index of the sector counting clockwise starting at 0 = upper half of 6, 1 = 13, 2 = 4 etc.
     * @return double The outer angle of a section in rad.
     */
    public double getSectionTheta(int sectorIndex) {
        double offSet = Math.PI / 20;

        return sectorIndex != 20 ? (sectorIndex * Math.PI) / 10 + offSet : (sectorIndex * Math.PI) / 10;
    }

    /**
     * @param r double Radial coordinate measured from the center of the board.
     * @return The section area r lies in.
     */
    public DartboardSectionArea getSectionArea(double r) {

        for (DartboardSectionAreaDimen areaDimension : areaDimensions) {
            if (r >= areaDimension.getInner() && r < areaDimension.getOuter())
                return areaDimension.getSectionArea();
        }

        return DartboardSectionArea.MISS;
    }

    /**
     * @param section     int section to get the center angle (theta).
     * @param sectionArea BoardSectionArea area within the section to get the radial center.
     * @return PolarCoordinate of the center of the section and section area
     */
    public PolarCoordinate getCenter(int section, DartboardSectionArea sectionArea) {
        if (sectionArea.equals(DartboardSectionArea.DOUBLE_BULL)) return new PolarCoordinate(0, 0);

        return getAreaDimensions().stream()
                .filter(dartBoardSectionAreaDimen -> dartBoardSectionAreaDimen.getSectionArea().equals(sectionArea))
                .findFirst()
                .map(boardSectionAreaDimen -> {
                    // Calculate what the center angle of a section is.
                    double sectionSize = Math.PI / 20;
                    double sectionTheta = getSectionTheta(sections.indexOf(section));
                    double sectionCenterTheta = sectionTheta - sectionSize;

                    // Calculate what the radial of the section area is.
                    double r = (boardSectionAreaDimen.getInner() + boardSectionAreaDimen.getOuter()) / 2.0;

                    return new PolarCoordinate(r, sectionCenterTheta);
                })
                .orElse(new PolarCoordinate(0, 0));
    }
}
