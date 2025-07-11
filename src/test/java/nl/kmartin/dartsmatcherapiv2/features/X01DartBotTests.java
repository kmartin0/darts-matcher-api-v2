package nl.kmartin.dartsmatcherapiv2.features;

import nl.kmartin.dartsmatcherapiv2.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.testutils.X01FeatureTestFactory;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api.IX01MatchRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class X01DartBotTests {
    private static final Logger logger = LoggerFactory.getLogger(X01DartBotTests.class);
    private static final int MAX_AVG_TO_TEST = 180;
    private static final int MIN_AVG_TO_TEST = 1;
    private static final int ITERATION_PER_TARGET = 100;

    @Mock
    private IX01MatchRepository matchRepository; // Mocked repository

    @Mock
    private MessageResolver messageResolver;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private IX01DartBotService dartBotService;

    @BeforeEach
    void setUp() {
        X01FeatureTestFactory featureTestFactory = new X01FeatureTestFactory(matchRepository, messageResolver, eventPublisher);
        dartBotService = featureTestFactory.createDartBotService();
    }

    @Test
    void isDartBotServiceInitialized() {
        Assertions.assertNotNull(dartBotService);
    }

    @Test
    void testDartBotLegs() {
        final ObjectId dartBotId = new ObjectId();
        final X01Match match = createTestMatch(dartBotId);

        for (int targetAvg = MAX_AVG_TO_TEST; targetAvg > MIN_AVG_TO_TEST; targetAvg--) {
            executeTestForTargetAvg(match, dartBotId, targetAvg);
        }
    }

    private void executeTestForTargetAvg(X01Match match, ObjectId dartBotId, int targetAvg) {
        printTargetAvg(targetAvg);
        // Initialize the match with the base settings.
        X01Leg x01Leg = new X01Leg(null, dartBotId, new TreeMap<>());
        X01Set x01Set = new X01Set(new TreeMap<>(Map.of(1, x01Leg)), dartBotId, null);

        X01DartBotSettings dartBotSettings = new X01DartBotSettings(targetAvg);
        X01MatchPlayer dartBotPlayer = new X01MatchPlayer(dartBotId, "Dart Bot", PlayerType.DART_BOT, null, dartBotSettings, null);

        match.setSets(new TreeMap<>(Map.of(1, x01Set)));
        match.setPlayers(new ArrayList<>(Collections.singletonList(dartBotPlayer)));

        System.out.printf(TargetDartsBoundaries.create(targetAvg, match.getMatchSettings().getX01()) + "\n");

        Map<Integer, Integer> dartsUsedMap = new TreeMap<>();
        for (int j = 0; j < ITERATION_PER_TARGET; j++) {
            int dartsUsed = simulateLeg(targetAvg, match, x01Leg, dartBotId);
            dartsUsedMap.put(dartsUsed, dartsUsedMap.getOrDefault(dartsUsed, 0) + 1);
            x01Leg.getRounds().clear();
        }
        System.out.println("Darts Used Map: " + dartsUsedMap);
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
            X01Turn x01Turn = dartBotService.createDartBotTurn(match);
            X01LegRoundScore roundScore = new X01LegRoundScore(x01Turn.getDoublesMissed(), x01Turn.getScore());

            currentLeg.getRounds().put(round++, new X01LegRound(new LinkedHashMap<>(Map.of(dartBotId, roundScore))));
            remaining -= x01Turn.getScore();
            round++;
            dartsUsed = dartsUsed + (x01Turn.getCheckoutDartsUsed() == null ? 3 : x01Turn.getCheckoutDartsUsed());
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
        X01ClearByTwoRule clearByTwoRule = new X01ClearByTwoRule(false, 0);
        X01BestOf bestOf = new X01BestOf(1, 1, clearByTwoRule, clearByTwoRule, clearByTwoRule);
        match.setMatchSettings(new X01MatchSettings(501, true, bestOf));
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