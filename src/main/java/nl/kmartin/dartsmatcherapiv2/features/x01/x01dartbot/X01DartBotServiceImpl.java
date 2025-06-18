package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.common.Constants;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegResultService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class X01DartBotServiceImpl implements IX01DartBotService {

    private final IX01MatchService matchService;
    private final IX01MatchProgressService matchProgressService;
    private final IX01DartBotThrowSimulator dartBotThrowSimulatorService;
    private final MessageResolver messageResolver;
    private final IX01LegResultService legResultService;

    public X01DartBotServiceImpl(IX01MatchService matchService,
                                 IX01MatchProgressService matchProgressService,
                                 IX01DartBotThrowSimulator dartBotThrowSimulatorService,
                                 MessageResolver messageResolver, IX01LegResultService legResultService) {
        this.matchService = matchService;
        this.matchProgressService = matchProgressService;
        this.dartBotThrowSimulatorService = dartBotThrowSimulatorService;
        this.messageResolver = messageResolver;
        this.legResultService = legResultService;
    }

    /**
     * Creates a turn for a dart bot in an X01 match, but only if the current thrower is a dart bot.
     *
     * This method retrieves the current match and verifies if the current thrower is a dart bot.
     * If the current thrower is a dart bot, it proceeds to generate a turn for that dart bot.
     *
     * If the current thrower is not a dart bot, this method will throw an invalid arguments exception.
     *
     * @param matchId {@link ObjectId} the ID of the match for which to create the turn.
     * @return {@link X01Turn} the turn created for the dart bot (if the current thrower is a dart bot).
     */
    @Override
    public X01Turn createDartBotTurn(@NotNull ObjectId matchId) {
        // Get the match and dart bot for this turn.
        X01Match match = matchService.getMatch(matchId);
        X01MatchPlayer dartBotPlayer = getCurrentDartBotPlayer(match);

        // Get the current leg for the match.
        X01Set currentSet = matchProgressService.getCurrentSetOrCreate(match).orElse(null);
        X01Leg currentLeg = matchProgressService.getCurrentLegOrCreate(match, currentSet)
                .orElseThrow(() -> new InvalidArgumentsException(new TargetError("currentLeg", messageResolver.getMessage(MessageKeys.EXCEPTION_INVALID_ARGUMENTS))));

        // Create the round score object for this turn.
        X01LegRoundScore roundScore = createRoundScore(createDartBotLegState(match, dartBotPlayer, currentLeg));

        // Create and return an X01Turn object using the values from the created round score for this turn
        return new X01Turn(roundScore.getScore(), roundScore.getDartsUsed(), roundScore.getDoublesMissed());
    }

    /**
     * Retrieves the current thrower for a match and determines if it is a dart bot and the bot settings are set. Will
     * throw an {@link InvalidArgumentsException} if any of these constraints aren't met.
     *
     * @param match {@link X01Match} containing the match data
     * @return {@link X01MatchPlayer} representing the current thrower who is a DART_BOT
     */
    private X01MatchPlayer getCurrentDartBotPlayer(X01Match match) {
        // Get the current thrower and list of players for the match
        ObjectId currentThrower = match.getMatchProgress().getCurrentThrower();
        List<X01MatchPlayer> matchPlayers = match.getPlayers();

        // Find the X01MatchPlayer object associated with the current thrower and verify it's a dart bot. Otherwise,
        // throw an InvalidArgumentsException
        return matchPlayers.stream()
                .filter(matchPlayer -> matchPlayer.getPlayerId().equals(currentThrower) &&
                        matchPlayer.getPlayerType().equals(PlayerType.DART_BOT) &&
                        matchPlayer.getX01DartBotSettings() != null)
                .findFirst()
                .orElseThrow(() -> new InvalidArgumentsException(new TargetError("dartBotId", messageResolver.getMessage(MessageKeys.MESSAGE_X01_DART_BOT_CURRENT_THROWER_ERROR))));
    }

    /**
     * Creates a {@link X01DartBotLegState} by mapping dart bot's current state for the current leg regarding
     * points scored, darts used, target average and target number of darts.
     *
     * The leg state also stores the data for the current round. And the x01 setting for the match.
     *
     * @param match         {@link X01Match} containing the match data
     * @param dartBotPlayer {@link X01Match} representing the dart bot
     * @param currentLeg    {@link X01Leg} the current leg being played
     * @return {@link X01DartBotLegState} representing the dart bot's current state in the leg.
     */
    private X01DartBotLegState createDartBotLegState(X01Match match, X01MatchPlayer dartBotPlayer, X01Leg currentLeg) {
        // Get the starting score
        int x01 = match.getMatchSettings().getX01();

        // Calculate the score already score by the dart bot in the current leg
        int legScored = x01 - legResultService.calculateRemainingScore(currentLeg, x01, dartBotPlayer.getPlayerId());

        // Calculate the number of darts used by the dart bot in the current leg
        int dartsUsed = legResultService.calculateDartsUsed(currentLeg, dartBotPlayer.getPlayerId());

        // Calculate the 1-dart average of the dart bot in the current leg
        double targetOneDartAvg = (double) dartBotPlayer.getX01DartBotSettings().getThreeDartAverage() / Constants.NUM_OF_DARTS_IN_A_ROUND;

        // Create and return a new dart bot leg state with the calculated values
        return new X01DartBotLegState(
                x01,
                legScored,
                dartsUsed,
                createTargetNumOfDarts(match.getMatchSettings().getX01(), targetOneDartAvg),
                targetOneDartAvg,
                new X01LegRoundScore(0, 0, 0)
        );
    }

    /**
     * Creates a target number of darts based on the starting score and the bot's one-dart average.
     * First calculates how many darts are needed to finish a leg in the target average. Then returns a random target
     * number within a 5% lower and upper range to simulate a more realistic performance.
     *
     * @param x01              int the starting score in the x01 match
     * @param targetOneDartAvg double the dart bot's target average score per dart
     * @return int the target number of darts the dart bot should finish the leg in
     */
    private int createTargetNumOfDarts(int x01, double targetOneDartAvg) {
        // Define the factor for the range that the target number of darts should be in (5% range)
        final double rangeFactor = 0.05;

        // Determine the target number of darts needed to finish a leg at the target one dart average
        int targetNumOfDarts = (int) Math.round(x01 / targetOneDartAvg);

        // Get a random target number within the calculated bounds
        double lowerBound = targetNumOfDarts * (1 - rangeFactor);
        double upperBound = targetNumOfDarts * (1 + rangeFactor);
        double randomWithinRange = ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);

        // Return the rounded random target number of darts
        return Math.max(1, (int) Math.round(randomWithinRange));
    }

    /**
     * Creates a round score for a dart bot's turn based on the provided dart bot leg state.
     * The method simulates the dart throws made by the dart bot and updates the round score
     * until the remaining points are zero or the maximum darts for the turn are used.
     *
     * @param dartBotLegState {@link X01DartBotLegState} representing the dart bot's current state in the leg.
     * @return {@link X01LegRoundScore} representing the score, darts used, and doubles missed for the dart bot's turn in the current leg.
     */
    private X01LegRoundScore createRoundScore(X01DartBotLegState dartBotLegState) {
        int remaining = dartBotLegState.getRemainingPoints();

        // Simulate dart throws until either the remaining points has reached zero or there no darts left to throw in the round
        while (remaining != 0 && dartBotLegState.getLegRoundScore().getDartsLeft() > 0) {
            // Simulate dart throws and update the leg state for each throw.
            List<DartThrow> dartThrows = dartBotThrowSimulatorService.getNextDartThrows(dartBotLegState);
            dartThrows.forEach(dartThrow -> updateRoundScore(dartBotLegState.getLegRoundScore(), dartThrow));

            // Update the remaining counter
            remaining = dartBotLegState.getRemainingPoints();
        }

        // Return the updated round score from the leg state
        return dartBotLegState.getLegRoundScore();
    }

    /**
     * Updates the round score based on the dart throw.
     *
     * This method updates the score and the number of darts used for the given round. It also checks if the dart
     * throw missed the intended double section and updates the count of missed doubles if applicable.
     *
     * @param roundScore {@link X01LegRoundScore} the current round score to update
     * @param dartThrow  {@link DartThrow} the dart throw that contains the result to be added to the score
     */
    private void updateRoundScore(X01LegRoundScore roundScore, DartThrow dartThrow) {
        roundScore.setScore(roundScore.getScore() + dartThrow.getResult().getScore());
        roundScore.setDartsUsed(roundScore.getDartsUsed() + 1);

        // Check for double missed
        DartboardSectionArea targetArea = dartThrow.getTarget().getArea();
        DartboardSectionArea resultArea = dartThrow.getResult().getArea();
        if ((targetArea.equals(DartboardSectionArea.DOUBLE_BULL) && !resultArea.equals(DartboardSectionArea.DOUBLE_BULL)) ||
                (targetArea.equals(DartboardSectionArea.DOUBLE) && !resultArea.equals(DartboardSectionArea.DOUBLE))) {
            roundScore.setDoublesMissed(roundScore.getDoublesMissed() + 1);
        }
    }
}