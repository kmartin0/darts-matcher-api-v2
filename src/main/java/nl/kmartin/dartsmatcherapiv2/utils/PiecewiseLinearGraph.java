package nl.kmartin.dartsmatcherapiv2.utils;

import java.util.Map;
import java.util.NavigableMap;

/**
 * A class representing a piecewise linear graph, which allows for interpolation
 * between data points defined on a 2D graph.
 */
public class PiecewiseLinearGraph {
    private final NavigableMap<Double, Double> dataPoints;

    /**
     * Constructs a PiecewiseLinearGraph with the specified data points.
     *
     * @param dataPoints A NavigableMap where keys represent the X-values and
     *                   values represent the corresponding Y-values.
     * @throws IllegalArgumentException if the data points map is null or empty.
     */
    public PiecewiseLinearGraph(NavigableMap<Double, Double> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new IllegalArgumentException("Data points cannot be empty");
        }

        this.dataPoints = dataPoints;
    }

    /**
     * Interpolates the Y value for a given X value based on the piecewise linear
     * interpolation between data points.
     *
     * @param valueX The X value for which the Y value is to be interpolated.
     * @return The interpolated Y value for the specified X.
     */
    public double interpolateY(double valueX) {
        // If there's only one data point, return the Y value of that point
        if (dataPoints.size() == 1) {
            return dataPoints.firstEntry().getValue();
        }

        // Find the nearest lower and upper data points
        Map.Entry<Double, Double> lower = dataPoints.floorEntry(valueX);
        Map.Entry<Double, Double> upper = dataPoints.ceilingEntry(valueX);

        // Handle edge cases where valueX is out of range
        if (lower == null) return dataPoints.firstEntry().getValue(); // Below range
        if (upper == null) return dataPoints.lastEntry().getValue();  // Above range

        // If valueX exactly matches a data point, return the corresponding Y value
        if (Double.compare(lower.getKey(), upper.getKey()) == 0) return lower.getValue(); // Exact match

        // Perform interpolation between the lower and upper points
        return interpolateBetween(lower, upper, valueX);
    }

    /**
     * Interpolates a Y value between two data points based on their X and Y values.
     * This is the core formula for linear interpolation.
     *
     * @param lower The entry representing the lower data point (X1, Y1).
     * @param upper The entry representing the upper data point (X2, Y2).
     * @param x     The X value for which the Y value is to be calculated.
     * @return The interpolated Y value for the given X.
     */
    private double interpolateBetween(Map.Entry<Double, Double> lower, Map.Entry<Double, Double> upper, double x) {
        // Get the x1 and y1 values for the lower point
        double x1 = lower.getKey();
        double y1 = lower.getValue();

        // Get the x2 and y2 values for the upper point
        double x2 = upper.getKey();
        double y2 = upper.getValue();

        // Calculate the slope
        double slope = (y2 - y1) / (x2 - x1);

        // Calculate and return the interpolation using the calculated slope
        return y1 + slope * (x - x1);
    }
}