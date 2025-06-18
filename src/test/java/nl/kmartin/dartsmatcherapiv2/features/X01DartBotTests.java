package nl.kmartin.dartsmatcherapiv2.features;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.testutils.X01FeatureTestFactory;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api.IX01MatchRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class X01DartBotTests {

    private static final int MAX_AVG_TO_TEST = 50;
    private static final int MIN_AVG_TO_TEST = 40;
    private static final int ITERATION_PER_TARGET = 50;

    @Mock
    private IX01MatchRepository matchRepository; // Mocked repository

    @Mock
    private MessageResolver messageResolver;

    private IX01DartBotService dartBotService;

    @BeforeEach
    void setUp() {
        X01FeatureTestFactory featureTestFactory = new X01FeatureTestFactory(matchRepository, messageResolver);
        dartBotService = featureTestFactory.createDartBotService();
    }

    @Test
    void isMatchServiceOk() {
        Assertions.assertNotNull(dartBotService);
    }

    @Test
    void testDartBotLegs() {
        final ObjectId dartBotId = new ObjectId();
        final X01Match match = createTestMatch(dartBotId);

        Mockito.when(matchRepository.findById(Mockito.any())).thenReturn(Optional.of(match));

        for (int targetAvg = MAX_AVG_TO_TEST; targetAvg > MIN_AVG_TO_TEST; targetAvg--) {
            executeTestForTargetAvg(match, dartBotId, targetAvg);
        }
    }

    private void executeTestForTargetAvg(X01Match match, ObjectId dartBotId, int targetAvg) {
        printTargetAvg(targetAvg);
        // Initialize the match with the base settings.
        X01Leg x01Leg = new X01Leg(1, null, dartBotId, new ArrayList<>());
        X01Set x01Set = new X01Set(1, new ArrayList<>(Collections.singletonList(x01Leg)), dartBotId, null);

        X01DartBotSettings dartBotSettings = new X01DartBotSettings(targetAvg);
        X01MatchPlayer dartBotPlayer = new X01MatchPlayer(dartBotId, "Dart Bot", PlayerType.DART_BOT, null, dartBotSettings, null);

        match.setSets(new ArrayList<>(Collections.singletonList(x01Set)));
        match.setPlayers(new ArrayList<>(Collections.singletonList(dartBotPlayer)));

        System.out.printf(TargetDartsBoundaries.create(targetAvg, match.getMatchSettings().getX01()) + "\n");

        Map<Integer, Integer> dartsUsedMap = new TreeMap<>();
        for (int j = 0; j < ITERATION_PER_TARGET; j++) {
            int dartsUsed = simulateLeg(targetAvg, match, x01Leg, dartBotId);
            dartsUsedMap.put(dartsUsed, dartsUsedMap.getOrDefault(dartsUsed, 0) + 1);
            x01Leg.getRounds().clear();
        }
        System.out.println("\nDarts Used Map: " + dartsUsedMap);
    }

    private int simulateLeg(int targetAvg, X01Match match, X01Leg currentLeg, ObjectId dartBotId) {
        int round = 1;
        int remaining = match.getMatchSettings().getX01();
        int dartsUsed = 0;
        while (remaining != 0) {
            if (remaining < 0) {
                System.out.println("ERROR: Remaining below zero");
                break;
            }
            match.getMatchProgress().setCurrentRound(round);
            X01Turn x01Turn = dartBotService.createDartBotTurn(match.getId());
            X01LegRoundScore roundScore = new X01LegRoundScore(x01Turn.getDoublesMissed(), x01Turn.getDartsUsed(), x01Turn.getScore());
            currentLeg.getRounds().add(new X01LegRound(round++, Map.of(dartBotId, roundScore)));
            remaining -= x01Turn.getScore();
            round++;
            dartsUsed = dartsUsed + roundScore.getDartsUsed();
        }

        assertDartsUsedWithinBounds(targetAvg, match.getMatchSettings().getX01(), dartsUsed);
        currentLeg.getRounds().clear();
        return dartsUsed;
    }

    private void assertDartsUsedWithinBounds(int targetAvg, int x01, int dartsUsed) {
        TargetDartsBoundaries targetDartsBoundaries = TargetDartsBoundaries.create(targetAvg, x01);

        // Check the lower bound
        Assertions.assertTrue(
                dartsUsed >= targetDartsBoundaries.lowerTargetNumOfDarts,
                "AssertionFailed(expected: >= " + targetDartsBoundaries.lowerTargetNumOfDarts + ", actual: " + dartsUsed + ")"
        );

        // Check the upper bound
        Assertions.assertTrue(
                dartsUsed <= targetDartsBoundaries.upperTargetNumOfDarts,
                "exp: <= " + targetDartsBoundaries.upperTargetNumOfDarts + ", act: " + dartsUsed + ")"
        );
    }

    private X01Match createTestMatch(ObjectId starter) {
        X01Match match = new X01Match();
        match.setId(new ObjectId());
        match.setMatchSettings(new X01MatchSettings(501, false, new X01BestOf(1, 1)));
        match.setMatchProgress(new X01MatchProgress(1, 1, 1, starter));
        return match;
    }

    private void printTargetAvg(int targetAvg) {
        System.out.println();
        System.out.println("=".repeat(20));
        System.out.println("Target Avg. " + targetAvg);
        System.out.println("=".repeat(20));
    }

    private record TargetDartsBoundaries(int upperTargetNumOfDarts, int targetNumOfDarts, int lowerTargetNumOfDarts) {
        public static TargetDartsBoundaries create(int targetAvg, int x01) {
            int targetNumOfDarts = (int) Math.max(9, Math.round(x01 / (targetAvg / 3.0)));
            int upperTargetNumOfDarts = (int) Math.max(9, Math.round(targetNumOfDarts * 1.05)) + 1;
            int lowerTargetNumOfDarts = (int) Math.max(9, Math.round(targetNumOfDarts * 0.95)) - 1;

            return new TargetDartsBoundaries(upperTargetNumOfDarts, targetNumOfDarts, lowerTargetNumOfDarts);
        }

        @Override
        public String toString() {
            return String.format("Lower Target: %-10dTarget: %-10dUpper Target: %-10d",
                    lowerTargetNumOfDarts, targetNumOfDarts, upperTargetNumOfDarts);
        }
    }
}