package nl.kmartin.dartsmatcherapiv2.features.dartboard;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.*;
import org.springframework.stereotype.Service;

@Service
public class DartboardServiceImpl implements IDartboardService {

    private final Dartboard dartboard;

    public DartboardServiceImpl(Dartboard dartboard) {
        this.dartboard = dartboard;
    }

    /**
     * Virtually throws a dart at a target on the dartboard and returns a Dart with the result. Generates the result of
     * the center of the target area + the deviation angle and radius.
     *
     * @param target      Dart the target that will be aimed for.
     * @param offsetR     double the radial deviation in mm.
     * @param offsetTheta double the theta (angle) deviation in radian between -pi and pi.
     * @return Dart The result of where the dart landed on the board.
     */
    @Override
    public Dart getScore(Dart target, double offsetR, double offsetTheta) {
        // Create a polar coordinate from the center of the section area.
        PolarCoordinate polarTarget = getCenter(target.getSection(), target.getArea());

        // Add the deviation radial and angle to the target's polar coordinate.
        double r = polarTarget.getR() + offsetR;
        double theta = PolarCoordinate.normalizeTheta(polarTarget.getTheta() + offsetTheta);

        // Return the Dart containing the result of the polar coordinate with deviation.
        return getScorePolar(new PolarCoordinate(r, theta));
    }

    /**
     * @param cartesianCoordinate CartesianCoordinate The cartesian coordinates of which a score needs to be calculated.
     * @return int The score corresponding the cartesian coordinates.
     */
    private Dart getScoreCartesian(CartesianCoordinate cartesianCoordinate) {

        return getScorePolar(PolarCoordinate.fromCartesian(cartesianCoordinate));
    }

    /**
     * @param polarCoordinate PolarCoordinate the polar coordinates of a score.
     * @return int The score corresponding the polar coordinates.
     */
    private Dart getScorePolar(PolarCoordinate polarCoordinate) {
        DartBoardSection section = getSection(polarCoordinate.getThetaNormalized());
        DartboardSectionArea sectionArea = getSectionArea(polarCoordinate.getR());

        // Return the score multiplied by the section area multiplier.
        return new Dart(section, sectionArea);
    }

    /**
     * Gets the scoring section for a given angle (theta) in rad. Ties are broken clockwise (i.e. when theta is on the wire between 18 and 4 the score is 4).
     *
     * @param theta double The angle in rad of which the section needs to be determined.
     * @return int The section of the board.
     */
    private DartBoardSection getSection(double theta) {

        for (int i = 0; i < dartboard.getSections().size(); i++) {
            if (theta <= getSectionTheta(i)) {
                return dartboard.getSections().get(i);
            }
        }

        return DartBoardSection.MISS;
    }

    /**
     * @param sectorIndex int the index of the sector counting clockwise starting at 0 = upper half of 6, 1 = 13, 2 = 4 etc.
     * @return double The outer angle of a section in rad.
     */
    private double getSectionTheta(int sectorIndex) {
        double offSet = Math.PI / 20;

        return sectorIndex != 20 ? (sectorIndex * Math.PI) / 10 + offSet : (sectorIndex * Math.PI) / 10;
    }

    /**
     * @param r double Radial coordinate measured from the center of the board.
     * @return The section area r lies in.
     */
    private DartboardSectionArea getSectionArea(double r) {

        for (DartboardSectionAreaDimen areaDimension : dartboard.getAreaDimensions()) {
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
    private PolarCoordinate getCenter(DartBoardSection section, DartboardSectionArea sectionArea) {
        if (sectionArea.equals(DartboardSectionArea.DOUBLE_BULL)) return new PolarCoordinate(0, 0);

        return dartboard.getAreaDimensions().stream()
                .filter(dartBoardSectionAreaDimen -> dartBoardSectionAreaDimen.getSectionArea().equals(sectionArea))
                .findFirst()
                .map(boardSectionAreaDimen -> {
                    // Calculate what the center angle of a section is.
                    double sectionSize = Math.PI / 20;
                    double sectionTheta = getSectionTheta(dartboard.getSections().indexOf(section));
                    double sectionCenterTheta = sectionTheta - sectionSize;

                    // Calculate what the radial of the section area is.
                    double r = (boardSectionAreaDimen.getInner() + boardSectionAreaDimen.getOuter()) / 2.0;

                    return new PolarCoordinate(r, sectionCenterTheta);
                })
                .orElse(new PolarCoordinate(0, 0));
    }
}
