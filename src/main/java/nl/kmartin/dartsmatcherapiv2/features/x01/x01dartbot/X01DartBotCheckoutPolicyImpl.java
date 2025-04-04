package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DartBotLegState;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import org.springframework.stereotype.Service;

@Service
public class X01DartBotCheckoutPolicyImpl implements IX01DartBotCheckoutPolicy {

    private final IX01CheckoutService checkoutService;

    public X01DartBotCheckoutPolicyImpl(IX01CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Checks whether the dart result is valid based on the remaining score after the throw.
     *
     * Darts that are not part of a checkout attempt (i.e., remaining score is neither zero nor bust) are always valid.
     * If the remaining score is part of a checkout attempt, it further validates whether the result is a valid checkout.
     *
     * @param result          {@link Dart} the dart result to be validated
     * @param dartBotLegState {@link X01DartBotLegState} the current state of the leg.
     * @return boolean true if the result is valid, false otherwise.
     */
    @Override
    public boolean isDartResultValid(Dart result, X01DartBotLegState dartBotLegState) {
        // Calculate the remaining points after the dart throw.
        int remaining = getRemainingAfterThrow(result, dartBotLegState.getRemainingPoints());

        // Darts that are not part of a checkout attempt (zero or bust remaining) are always valid.
        if (!checkoutService.isRemainingZeroOrBust(remaining)) return true;

        // Otherwise, validate if the checkout is valid.
        return isBotCheckoutValid(result, dartBotLegState);
    }

    /**
     * Checks if the target number of darts has been reached.
     *
     * @param dartsThrown      int the number of darts that have been thrown so far.
     * @param targetNumOfDarts int the target number of darts required to complete the leg.
     * @return boolean true if the number of darts thrown is greater than or equal to the target, false otherwise.
     */
    @Override
    public boolean isTargetNumOfDartsReached(int dartsThrown, int targetNumOfDarts) {
        return dartsThrown >= targetNumOfDarts;
    }

    /**
     * Checks whether the bot's checkout throw is valid.
     * This includes verifying that the remaining points after the throw are valid for a checkout
     * and that the target number of darts required to complete the leg has been reached.
     *
     * @param result          {@link Dart} the dart result to be validated
     * @param dartBotLegState {@link X01DartBotLegState} the current state of the leg.}
     * @return boolean true if the checkout is valid, false otherwise.
     */
    private boolean isBotCheckoutValid(Dart result, X01DartBotLegState dartBotLegState) {
        int remainingAfterThrow = getRemainingAfterThrow(result, dartBotLegState.getRemainingPoints());
        int dartsThrownAfterCheckout = dartBotLegState.getDartsUsedInLeg() + 1;
        int targetNumOfDarts = dartBotLegState.getTargetNumOfDarts();

        // Check if the remaining points and number of darts thrown are valid for the checkout.
        return checkoutService.isValidCheckout(remainingAfterThrow, result) &&
                isTargetNumOfDartsReached(dartsThrownAfterCheckout, targetNumOfDarts);
    }

    /**
     * Calculates the remaining points after a dart throw.
     *
     * @param result               the dart result to calculate the remaining points for.
     * @param remainingBeforeThrow he remaining points before the throw.
     * @return int the remaining points after the dart throw.
     */
    private int getRemainingAfterThrow(Dart result, int remainingBeforeThrow) {
        return remainingBeforeThrow - result.getScore();
    }
}