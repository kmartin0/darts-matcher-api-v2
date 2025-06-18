package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.PolarCoordinate;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.PiecewiseLinearGraph;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

@Service
public class X01DartBotAccuracyCalculatorImpl implements IX01DartBotAccuracyCalculator {
    private static final NavigableMap<Double, Double> PIECE_WISE_ACCURACY_CURVE_ONE_DART_AVG = new TreeMap<>() {{
        put(0.0, 80.0);
        put(10.0, 45.0);
        put(15.0, 30.0);
        put(20.0, 13.0);
        put(30.0, 10.0);
        put(33.0, 7.0);
        put(40.0, 6.5);
        put(50.0, 6.0);
        put(53.0, 0.0);
    }};

    /**
     * Generates the radial (r) offset associated with a target average. This offset is calibrated to be preciser the further away
     * the current average is from the target.
     *
     * @param targetOneDartAvg  double The target one dart average.
     * @param currentOneDartAvg double The current one dart average.
     * @return double The radial (r) offset in mm.
     */
    @Override
    public double createOffsetR(double targetOneDartAvg, double currentOneDartAvg) {
        // Get the (calibrated) offset based on the target and current avg.
        double offset = calcAccuracy(targetOneDartAvg, currentOneDartAvg);

        // Return a random number within a range from negative offset to positive offset.
        return Math.random() * (offset + offset) - offset;
    }

    /**
     * Generates the angle (theta) offset in radian units associated with a target average. This offset is
     * calibrated to be preciser the further away the current average is from the target.
     *
     * @param targetOneDartAvg  double The expected one dart average.
     * @param currentOneDartAvg double The current one dart average.
     * @return double The angle (theta) offset.
     */
    @Override
    public double createOffsetTheta(double targetOneDartAvg, double currentOneDartAvg) {
        // Get the (calibrated) offset in degrees based on the target and current avg.
        double offset = calcAccuracy(targetOneDartAvg, currentOneDartAvg);

        // Adjust offset to be a random number within a range from negative offset to positive offset.
        offset = Math.random() * (offset + offset) - offset;

        // Return the angle offset in radian units.
        return PolarCoordinate.degreeToRadian(offset);
    }

    /**
     * Creates a baseline accuracy for a given target one dart average. Calibrates it according to the current one dart
     * average performance compared to the target. The accuracy represents how far from a target someone will throw (mm)
     * given their target one dart average.
     *
     * @param targetOneDartAvg  double the target one dart average
     * @param currentOneDartAvg double the current one dart average
     * @return double the accuracy in mm representing how far off a target someone can throw
     */
    private double calcAccuracy(double targetOneDartAvg, double currentOneDartAvg) {
        // Get the baseline accuracy for the target one dart average. When no darts are thrown (avg=0) return the baseline average.
        NavigableMap<Double, Double> accuracyGraphDataPoints = PIECE_WISE_ACCURACY_CURVE_ONE_DART_AVG;
        double baseAccuracy = getBaseAccuracy(targetOneDartAvg, accuracyGraphDataPoints);
        if (currentOneDartAvg == 0) return baseAccuracy;

        // Calibrate the accuracy depending on the performance and return it
        return calibrateAccuracy(targetOneDartAvg, currentOneDartAvg, baseAccuracy, accuracyGraphDataPoints);
    }

    /**
     * Looks up the baseline accuracy for a given target one dart average. The accuracy is determined by interpolating a
     * piece wise linear graph.
     *
     * @param targetOneDartAvg        double the target one dart average
     * @param accuracyGraphDataPoints the data points of the accuracy graph
     * @return double the baseline accuracy for the given one dart average
     */
    private double getBaseAccuracy(double targetOneDartAvg, NavigableMap<Double, Double> accuracyGraphDataPoints) {
        // Create the piece wise linear graph using the piece wise accuracy curve
        PiecewiseLinearGraph baseAccuracyGraph = new PiecewiseLinearGraph(accuracyGraphDataPoints);

        // The graph x-axis represents the target averages and the y-axis represents the accuracies.
        // Interpolate the accuracy (y) for the target average (x).
        return baseAccuracyGraph.interpolateY(targetOneDartAvg);
    }

    /**
     * Calibrate the accuracy based on the target and current one dart averages.
     * This method adjusts the given accuracy value using a performance ratio
     * which is calculated based on the difference between the target and current averages.
     * It ensures the final accuracy is within a defined range (min to max accuracy).
     *
     * @param targetOneDartAvg        double the target one dart average
     * @param currentOneDartAvg       double the current one dart average
     * @param accuracyToCalibrate     double the accuracy value to be calibrated.
     * @param accuracyGraphDataPoints the data points of the accuracy graph
     * @return double The calibrated accuracy value, constrained between the minimum and maximum allowable accuracy.
     */
    private double calibrateAccuracy(double targetOneDartAvg, double currentOneDartAvg, double accuracyToCalibrate, NavigableMap<Double, Double> accuracyGraphDataPoints) {
        // Calculate the performance ratio as the difference between the current and target averages, divided by the target average.
        double performanceRatio = (currentOneDartAvg - targetOneDartAvg) / targetOneDartAvg;

        // Calibration factor that influences how much the accuracy is adjusted.
        double calibrationFactor = 5.0;

        // Adjust the accuracy based on the performance ratio and the calibration factor.
        double adjustedAccuracy = accuracyToCalibrate * (1.0 + calibrationFactor * performanceRatio);

        // Get the minimum and maximum accuracy based on the target one dart average.
        double minAccuracy = getMinAccuracy(targetOneDartAvg, accuracyGraphDataPoints);
        double maxAccuracy = getMaxAccuracy(targetOneDartAvg, accuracyGraphDataPoints);

        // Return the adjusted accuracy, ensuring it is within the range of min and max accuracy.
        return Math.max(maxAccuracy, Math.min(minAccuracy, adjustedAccuracy));
    }

    /**
     * Gets the minimum accuracy value for the given target one dart average by finding the neighboring entry
     * below the floor entry in the piecewise accuracy curve. If no such entry exists, the first entry in the curve is returned.
     *
     * @param targetOneDartAvg        double the target one dart average
     * @param accuracyGraphDataPoints the data points of the accuracy graph
     * @return double the minimum accuracy value associated with the given target one dart average.
     */
    private double getMinAccuracy(double targetOneDartAvg, NavigableMap<Double, Double> accuracyGraphDataPoints) {
        // Get the floor entry (closest key less than or equal to targetOneDartAvg)
        Map.Entry<Double, Double> floor = accuracyGraphDataPoints.floorEntry(targetOneDartAvg);
        if (floor == null) return accuracyGraphDataPoints.firstEntry().getValue();

        // Get the neighboring entry below the floor entry
        Map.Entry<Double, Double> lower = accuracyGraphDataPoints.lowerEntry(floor.getKey());
        if (lower == null) return accuracyGraphDataPoints.firstEntry().getValue();

        // return the value of the lower key below the floor of the target average
        return lower.getValue();
    }

    /**
     * Gets the maximum accuracy value for the given target one dart average by finding the neighboring entry
     * above the ceiling entry in the piecewise accuracy curve. If no such entry exists, the last entry in the curve is returned.
     *
     * @param targetOneDartAvg        double the target one dart average
     * @param accuracyGraphDataPoints the data points of the accuracy graph
     * @return double the maximum accuracy value associated with the given target one dart average.
     */
    private double getMaxAccuracy(double targetOneDartAvg, NavigableMap<Double, Double> accuracyGraphDataPoints) {
        // Get the ceiling entry (closest key higher than or equal to targetOneDartAvg)
        Map.Entry<Double, Double> ceiling = accuracyGraphDataPoints.ceilingEntry(targetOneDartAvg);
        if (ceiling == null) return accuracyGraphDataPoints.lastEntry().getValue();

        // Get the neighboring entry above the ceiling entry
        Map.Entry<Double, Double> upper = accuracyGraphDataPoints.higherEntry(ceiling.getKey());
        if (upper == null) return accuracyGraphDataPoints.lastEntry().getValue();

        // return the value of the upper key above the ceiling of the target average
        return upper.getValue();
    }
}