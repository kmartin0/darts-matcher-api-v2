package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.IDartboardService;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Checkout;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DartBotLegState;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class X01DartBotThrowSimulatorImpl implements IX01DartBotThrowSimulator {
    private final IDartboardService dartboardService;
    private final IX01CheckoutService checkoutService;
    private final IX01DartBotCheckoutPolicy dartBotCheckoutPolicy;
    private final IX01DartBotAccuracyCalculator dartBotAccuracyCalculator;
    private final IX01DartBotScoringStrategy dartBotScoringStrategy;

    public X01DartBotThrowSimulatorImpl(IDartboardService dartboardService,
                                        IX01CheckoutService checkoutService,
                                        IX01DartBotCheckoutPolicy dartBotCheckoutPolicy,
                                        IX01DartBotAccuracyCalculator dartBotAccuracyCalculator,
                                        IX01DartBotScoringStrategy dartBotScoringStrategy) {
        this.dartboardService = dartboardService;
        this.checkoutService = checkoutService;
        this.dartBotCheckoutPolicy = dartBotCheckoutPolicy;
        this.dartBotAccuracyCalculator = dartBotAccuracyCalculator;
        this.dartBotScoringStrategy = dartBotScoringStrategy;
    }

    /**
     * Generates the next dart throws for the dart bot based on the current state of the leg.
     * If the remaining points are within checkout range, the bot will aim for a checkout.
     * Otherwise, the bot will aim for a scoring throw.
     *
     * When aiming for a scoring throw, the list will contain 1 {@link DartThrow}.
     * When aiming for a checkout, the list will contain 1 {@link DartThrow} or, if the bot needs
     * to complete a checkout sequence, it will contain the dart throws required for the checkout,
     * but the number of dart throws will not exceed the maximum darts the bot can still throw in the round.
     *
     * @param dartBotLegState {@link X01DartBotLegState} the current state of the dart bot in the leg
     * @return {@link List<DartThrow>} a list containing the dart throws for the next turn.
     */
    @Override
    public List<DartThrow> getNextDartThrows(X01DartBotLegState dartBotLegState) {
        // When the bot is outside checkout range, create a scoring throw. Otherwise, create a checkout throw.
        boolean isRemainingCheckout = checkoutService.isScoreCheckout(dartBotLegState.getRemainingPoints());
        return isRemainingCheckout
                ? createCheckoutThrowResult(dartBotLegState)
                : List.of(createScoringThrowResult(dartBotLegState));
    }

    /**
     * Creates a scoring throw for the dart bot based on the current leg state.
     * The method generates a dart throw aimed at the scoring target based on the dart bot's
     * target one-dart average and current one-dart average.
     * It validates the throw to ensure that a scoring throw doesn't accidentally throw an invalid check out.
     *
     * @param dartBotLegState {@link X01DartBotLegState} the current state of the dart bot in the leg
     * @return {@link DartThrow} the dart throw generated for the scoring attempt
     */
    private DartThrow createScoringThrowResult(X01DartBotLegState dartBotLegState) {
        // Generate a dart throw aimed at a generated scoring target
        DartThrow dartThrow = throwAtTarget(
                dartBotLegState.getTargetOneDartAvg(),
                dartBotLegState.getCurrentOneDartAvg(),
                dartBotScoringStrategy.createScoringTarget(dartBotLegState.getTargetOneDartAvg())
        );

        // Validate the result. If for some reason a scoring throw has checked out while it shouldn't, sets the result to a MISS.
        validateResult(dartThrow.getResult(), dartBotLegState);

        // Return the dart throw created for the scoring attempt
        return dartThrow;
    }

    /**
     * Creates a checkout throw for the dart bot based on the current state of the leg.
     * If the remaining points are in checkout range, it will create a sequence of dart throws
     * aimed at achieving the checkout. If no checkout is available for the current remaining points,
     * a scoring throw will be created instead.
     *
     * If the bot doesn't have to check out yet, the list will contain only one dart throw, aimed at the next target
     * in the checkout sequence. If the bot has to check out complete the checkout in the next dart throws,
     * but only using the darts remaining in the round.
     *
     * @param dartBotLegState {@link X01DartBotLegState} the current state of the dart bot in the leg
     * @return {@link List<DartThrow>} a list of dart throws that were thrown aiming at a checkout
     */
    private List<DartThrow> createCheckoutThrowResult(X01DartBotLegState dartBotLegState) {
        Optional<X01Checkout> checkout = checkoutService.getCheckout(dartBotLegState.getRemainingPoints());

        // When there is no checkout, return a scoring throw.
        if (checkout.isEmpty()) {
            return List.of(createScoringThrowResult(dartBotLegState));
        }

        // Simulate throwing at the next target in the checkout sequence.
        return throwAtCheckout(checkout.get(), dartBotLegState);
    }

    /**
     * Simulates the dart throws for the bot when it is aiming for a checkout.
     * This method checks if the bot needs to finish the game (i.e., checkout)
     * or if it can continue throwing at the first target in the checkout sequence.
     *
     * If the bot has to check out, the appropriate dart throws for the checkout sequence are generated.
     * If the bot doesn't have to check out yet, it will aim at the first target in the checkout sequence.
     *
     * @param checkout        {@link X01Checkout} the checkout sequence to be followed
     * @param dartBotLegState {@link X01DartBotLegState} the current state of the dart bot in the leg
     * @return {@link List<DartThrow>} a list of dart throws: either a sequence of throws for the checkout or a single dart throw aimed at the next target in the checkout sequence
     */
    private List<DartThrow> throwAtCheckout(X01Checkout checkout, X01DartBotLegState dartBotLegState) {
        // Check if the bot has to complete the checkout based on if the checkout sequence will equal or surpass the target number of darts
        int dartsUsedAfterCheckout = dartBotLegState.getDartsUsedInLeg() + checkout.getMinDarts();

        boolean hasToCheckout = dartBotCheckoutPolicy.isTargetNumOfDartsReached(dartsUsedAfterCheckout, dartBotLegState.getTargetNumOfDarts());
        if (hasToCheckout) {
            return createGuaranteedCheckoutThrows(checkout, dartBotLegState.getDartsLeftInRound());
        }

        // Simulate throwing at the first target in the checkout sequence.
        DartThrow dartThrow = throwAtTarget(
                dartBotLegState.getTargetOneDartAvg(),
                dartBotLegState.getCurrentOneDartAvg(),
                checkout.getSuggested().get(0)
        );

        // Validates the result, creates a MISS if the checkout is invalid (bust, no double finish, above target avg)
        validateResult(dartThrow.getResult(), dartBotLegState);

        // Return the simulated dart throw aimed at the first target in the checkout sequence
        return List.of(dartThrow);
    }

    /**
     * Creates a list of dart throws for the checkout sequence. This method generates the dart throws
     * necessary to complete the checkout based on the checkout sequence and how many darts remain in the round
     *
     * @param checkout       {@link X01Checkout} the checkout sequence to be followed
     * @param dartsRemaining int the number of darts remaining in the current round
     * @return {@link List<DartThrow>} a list of dart throws to complete the checkout sequence
     */
    private List<DartThrow> createGuaranteedCheckoutThrows(X01Checkout checkout, int dartsRemaining) {
        // Determine the number of darts to use based on darts remaining and darts required
        int dartsRequired = checkout.getSuggested().size();
        int dartsToUse = Math.min(dartsRemaining, dartsRequired);

        // Create and return a list of DartThrow objects for the required darts
        return checkout.getSuggested().stream().limit(dartsToUse).map(dart -> new DartThrow(dart, dart)).collect(Collectors.toList());
    }

    /**
     * Virtually throws a dart at a target on a dartboard with both an angle and a radial offset based on the target average
     * and the current average (over performing will result in a higher offset, under performing will result in a lower offset).
     *
     * @param targetOneDartAvg  double The expected one dart average of bot.
     * @param currentOneDartAvg double The current one dart average of bot in the leg.
     * @param target            {@link Dart} The target to be thrown at.
     * @return {@link DartThrow} The result of where the dart landed on the board.
     */
    private DartThrow throwAtTarget(double targetOneDartAvg, double currentOneDartAvg, Dart target) {
        // Generate the offset of the angle and radial.
        double offsetR = dartBotAccuracyCalculator.createOffsetR(targetOneDartAvg, currentOneDartAvg);
        double offsetTheta = dartBotAccuracyCalculator.createOffsetTheta(targetOneDartAvg, currentOneDartAvg);

        // Determine the final dart result based on the calculated offsets.
        Dart result = dartboardService.getScore(target, offsetR, offsetTheta);

        // Create and return the DartThrow containing the target and result
        return new DartThrow(target, result);
    }

    /**
     * This method checks if the dart throw adheres to the rules defined by the bot's checkout policy.
     * If the result is not valid, it adjusts the result area to MISS, simulating a missed throw.
     *
     * @param result          {@link Dart} the dart result that needs validation
     * @param dartBotLegState {@link X01DartBotLegState} the current state of the dart bot in the leg
     */
    private void validateResult(Dart result, X01DartBotLegState dartBotLegState) {
        // Check if the dart result is valid based on the current leg state and checkout policy
        if (!dartBotCheckoutPolicy.isDartResultValid(result, dartBotLegState)) {
            // If the result is invalid, mark it as a MISS
            result.setArea(DartboardSectionArea.MISS);
        }
    }
}