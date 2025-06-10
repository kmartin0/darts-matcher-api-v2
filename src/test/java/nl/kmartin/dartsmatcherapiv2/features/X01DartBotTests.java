package nl.kmartin.dartsmatcherapiv2.features;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.DartboardServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.IDartboardService;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dartboard;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics.IX01AverageStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics.X01AverageStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.X01CheckoutServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics.IX01CheckoutStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics.X01CheckoutServiceImplService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.X01LegServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.X01LegRoundServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.IX01MatchRepository;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.IX01MatchService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.X01MatchServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchprogress.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchprogress.X01MatchProgressServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.IX01MatchSetupService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.X01MatchSetupServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics.IX01ScoreStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics.X01ScoreStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.X01SetServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.X01StatisticsServiceImpl;
import nl.kmartin.dartsmatcherapiv2.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
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
        dartBotService = createDartBotService();
    }

    @Test
    void testDartBotLegs() throws IOException {
        final ObjectId dartBotId = new ObjectId();
        final X01Match match = createTestMatch(dartBotId);

        Mockito.when(matchRepository.findById(Mockito.any())).thenReturn(Optional.of(match));

        for (int targetAvg = MAX_AVG_TO_TEST; targetAvg > MIN_AVG_TO_TEST; targetAvg--) {
            executeTestForTargetAvg(match, dartBotId, targetAvg);
        }
    }

    private void executeTestForTargetAvg(X01Match match, ObjectId dartBotId, int targetAvg) throws IOException {
        printTargetAvg(targetAvg);
        // Initialize the match with the base settings.
        X01Leg x01Leg = new X01Leg(1, null, dartBotId, new ArrayList<>());
        X01Set x01Set = new X01Set(1, new ArrayList<>(Collections.singletonList(x01Leg)), dartBotId, null);

        X01DartBotSettings dartBotSettings = new X01DartBotSettings(targetAvg);
        X01MatchPlayer dartBotPlayer = new X01MatchPlayer(dartBotId, "Dart Bot", PlayerType.DART_BOT, null, 0, 0, dartBotSettings, null);

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

    private int simulateLeg(int targetAvg, X01Match match, X01Leg currentLeg, ObjectId dartBotId) throws IOException {
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

    private IX01DartBotService createDartBotService() {
        IX01CheckoutService checkoutService = createCheckoutService();
        IX01LegRoundService legRoundService = new X01LegRoundServiceImpl(checkoutService);
        IX01LegService legService = new X01LegServiceImpl(messageResolver, legRoundService);
        IX01SetService setService = new X01SetServiceImpl(legService, legRoundService);

        IX01MatchProgressService matchProgressService = new X01MatchProgressServiceImpl(setService, legService, legRoundService);

        return new X01DartBotServiceImpl(createMatchService(), matchProgressService, legRoundService, createDartBotThrowSimulator(), messageResolver);
    }

    private IX01CheckoutService createCheckoutService() {
        IX01CheckoutService checkoutService = new X01CheckoutServiceImpl(messageResolver);

        // 2. Manually load the checkouts resource
        Resource checkoutsResource = new ClassPathResource("data/checkouts.json");

        // 3. Inject the resource into the private field using reflection
        ReflectionTestUtils.setField(checkoutService, // Target object
                "checkoutsResourceFile", // Field name to inject
                checkoutsResource // Value to set
        );

        return checkoutService;
    }

    private IX01MatchService createMatchService() {
        IX01MatchSetupService matchSetupService = new X01MatchSetupServiceImpl();
        IX01CheckoutService checkoutService = createCheckoutService();
        IX01LegRoundService legRoundService = new X01LegRoundServiceImpl(checkoutService);
        IX01LegService legService = new X01LegServiceImpl(messageResolver, legRoundService);
        IX01SetService setService = new X01SetServiceImpl(legService, legRoundService);

        IX01ScoreStatisticsService scoreStatisticsService = new X01ScoreStatisticsServiceImpl();
        IX01AverageStatisticsService averageStatisticsService = new X01AverageStatisticsServiceImpl();
        IX01CheckoutStatisticsService checkoutStatisticsService = new X01CheckoutServiceImplService();
        IX01StatisticsService statisticsService = new X01StatisticsServiceImpl(scoreStatisticsService, checkoutStatisticsService, averageStatisticsService, legService);

        IX01MatchProgressService matchProgressService = new X01MatchProgressServiceImpl(setService, legService, legRoundService);

        return new X01MatchServiceImpl(matchRepository, matchSetupService, setService, legService, legRoundService, statisticsService, matchProgressService);
    }

    private IX01DartBotThrowSimulator createDartBotThrowSimulator() {
        IDartboardService dartboardService = new DartboardServiceImpl(new Dartboard());
        IX01CheckoutService checkoutService = createCheckoutService();
        IX01DartBotCheckoutPolicy dartBotCheckoutPolicy = new X01DartBotCheckoutPolicyImpl(checkoutService);
        IX01DartBotAccuracyCalculator dartBotAccuracyCalculator = new X01DartBotAccuracyCalculatorImpl();
        IX01DartBotScoringStrategy dartBotScoringStrategy = new X01DartBotScoringStrategyImpl();

        return new X01DartBotThrowSimulatorImpl(dartboardService, checkoutService, dartBotCheckoutPolicy, dartBotAccuracyCalculator, dartBotScoringStrategy);
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