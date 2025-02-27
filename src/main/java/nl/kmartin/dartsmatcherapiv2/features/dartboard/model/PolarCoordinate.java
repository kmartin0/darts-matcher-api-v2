package nl.kmartin.dartsmatcherapiv2.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PolarCoordinate {

    private double r;
    private double theta;

    public static PolarCoordinate fromCartesian(CartesianCoordinate cartesianCoordinate) {
        double x = cartesianCoordinate.getX();
        double y = cartesianCoordinate.getY();

        // Convert from cartesian to polar coordinate.
        double r = Math.sqrt((x * x) + (y * y));
        double theta = Math.atan2(y, x);

        return new PolarCoordinate(r, theta);
    }

    public double getThetaNormalized() {
        return normalizeTheta(theta);
    }

    public static double normalizeTheta(double theta) {
        return (theta + (Math.PI * 2)) % (Math.PI * 2);
    }

    public static double degreeToRadian(double degree) {
        return degree * (Math.PI / 180);
    }

}
