package nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01CheckoutStatistics;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import org.springframework.stereotype.Service;

@Service
public class X01CheckoutServiceImplService implements IX01CheckoutStatisticsService {
    /**
     * Updates the player's checkout statistics based on the current round's score and checkout attempt.
     *
     * @param playerCheckoutStats {@link X01CheckoutStatistics} object to update for the player's checkout data.
     * @param playerScore         {@link X01LegRoundScore} object containing the details of the player's round score and missed doubles.
     * @param isCheckout          boolean indicating if the player has attempted a checkout.
     * @param trackDoubles        boolean indicating if the player wants to track their checkout percentage
     */
    public void updateCheckoutStatistics(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore,
                                         boolean isCheckout) {
        if (playerCheckoutStats == null || playerScore == null) return;

        // If the player attempted to make a checkout. Update the relevant statistics
        if (isCheckout) {
            // Increment the number of successful checkouts
            playerCheckoutStats.incrementCheckoutsHit();

            // Update the highest checkout
            updateHighestCheckout(playerCheckoutStats, playerScore);

            // Update the ton plus checkout
            updateTonPlusCheckout(playerCheckoutStats, playerScore);
        }

        // Update missed checkouts based on doubles missed in the round
        updateCheckoutsMissed(playerCheckoutStats, playerScore);

        // Calculate and set the checkout percentage based on attempts and hits
        updateCheckoutPercentage(playerCheckoutStats);
    }

    /**
     * Updates the highest checkout score if the current score is higher than the previous highest.
     *
     * @param playerCheckoutStats {@link X01CheckoutStatistics} object to update with the highest checkout score.
     * @param playerScore         {@link X01LegRoundScore} object containing the player's score for the round.
     */
    private void updateHighestCheckout(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore) {
        if (playerCheckoutStats == null || playerScore == null) return;

        int checkoutScore = playerScore.getScore();

        // Update the highest checkout if current checkout is higher
        if (checkoutScore > playerCheckoutStats.getCheckoutHighest()) {
            playerCheckoutStats.setCheckoutHighest(checkoutScore);
        }
    }

    /**
     * Increments the "ton plus" checkout statistic if the player's checkout score is 100 or more.
     *
     * @param playerCheckoutStats {@link X01CheckoutStatistics} object to update with the "ton plus" checkout count.
     * @param playerScore         {@link X01LegRoundScore} object containing the player's score for the round.
     */
    private void updateTonPlusCheckout(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore) {
        if (playerCheckoutStats == null || playerScore == null) return;

        int checkoutScore = playerScore.getScore();

        // Increment the ton plus statistic if the checkout is a score of 100 or more
        if (checkoutScore >= 100) {
            playerCheckoutStats.incrementCheckoutTonPlus();
        }
    }

    /**
     * Updates the missed checkouts based on the number of missed doubles in the current round.
     *
     * @param playerCheckoutStats {@link X01CheckoutStatistics} object to update with the missed checkouts count.
     * @param playerScore         {@link X01LegRoundScore} object containing the player's score and missed doubles.
     */
    private void updateCheckoutsMissed(X01CheckoutStatistics playerCheckoutStats, X01LegRoundScore playerScore) {
        if (playerCheckoutStats == null || playerScore == null) return;

        // Update the missed checkouts by adding the number of missed doubles
        playerCheckoutStats.setCheckoutsMissed(playerCheckoutStats.getCheckoutsMissed() + playerScore.getDoublesMissed());
    }

    /**
     * Calculates and updates the checkout percentage based on the number of successful and missed checkouts.
     *
     * @param playerCheckoutStats {@link X01CheckoutStatistics} object to update with the calculated checkout percentage.
     */
    private void updateCheckoutPercentage(X01CheckoutStatistics playerCheckoutStats) {
        if (playerCheckoutStats == null) return;

        // Calculate the checkout attempts (successful + missed)
        int checkoutAttempts = playerCheckoutStats.getCheckoutsHit() + playerCheckoutStats.getCheckoutsMissed();

        // Calculate the checkout percentage
        int checkoutPercentage = NumberUtils.calcPercentage(playerCheckoutStats.getCheckoutsHit(), checkoutAttempts);

        // Update the checkout percentage in the statistics
        playerCheckoutStats.setCheckoutPercentage(checkoutPercentage);
    }
}
