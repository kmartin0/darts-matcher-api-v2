package nl.kmartin.dartsmatcherapiv2.utils;

import java.util.Set;

public class NumberUtils {

    /**
     * Find the next sequential number from a set of numbers.
     *
     * @param numbers The set of numbers to evaluate.
     * @return the smallest missing number, or the next number if no gap exists
     */
    public static int findNextNumber(Set<Integer> numbers) {
        if (numbers == null) return -1;

        return findNextNumber(numbers, Integer.MAX_VALUE);
    }

    /**
     * Find the next sequential number from a set of numbers.
     *
     * @param numbers The set of numbers to evaluate.
     * @param max     the upper bound of the number range (inclusive)
     * @return the smallest missing number, or the next number if no gap exists
     */
    public static int findNextNumber(Set<Integer> numbers, int max) {
        if (numbers == null) return -1;

        // Find the next available number (ensure it doesn't exceed the max)
        int nextNumber = 1;
        while (numbers.contains(nextNumber) && nextNumber <= max) {
            nextNumber++;
        }

        // Return the smallest missing number from the number range
        return nextNumber > max ? -1 : nextNumber; // Return -1 if no valid number is found
    }

    /**
     * Calculates the percentage based on the given numerator and denominator.
     *
     * @param numerator the value representing the successful attempts or relevant quantity.
     * @param denominator the total number of attempts or the total quantity.
     * @return the calculated percentage, or 0 if the denominator is 0 to avoid division by zero.
     */
    public static int calcPercentage(int numerator, int denominator) {
        if (denominator > 0) {
            return (int) Math.round(((double) numerator / denominator) * 100);
        } else {
            return 0; // Avoid division by zero, return 0 if no attempts are made
        }
    }

}
