package nl.kmartin.dartsmatcherapiv2.features.testutils;

import nl.kmartin.dartsmatcherapiv2.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.DartboardServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.IDartboardService;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dartboard;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics.IX01AverageStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics.X01AverageStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.X01CheckoutServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics.IX01CheckoutStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics.X01CheckoutStatisticsServiceImplService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.X01LegRoundServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api.IX01MatchRepository;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.IX01MatchSetupService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.X01MatchSetupServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01resultstatistics.IX01ResultStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01resultstatistics.X01ResultStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01rules.IX01RulesService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01rules.X01RulesServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics.IX01ScoreStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics.X01ScoreStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01standings.IX01StandingsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01standings.X01StandingsServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.X01StatisticsServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class X01FeatureTestFactory {

    private final IX01MatchRepository matchRepositoryMock;
    private final MessageResolver messageResolverMock;
    private final ApplicationEventPublisher eventPublisherMock;

    public X01FeatureTestFactory(IX01MatchRepository matchRepositoryMock, MessageResolver messageResolverMock, ApplicationEventPublisher eventPublisherMock) {
        this.matchRepositoryMock = matchRepositoryMock;
        this.messageResolverMock = messageResolverMock;
        this.eventPublisherMock = eventPublisherMock;
    }

    public IX01MatchService createMatchService() {
        return new X01MatchServiceImpl(
                matchRepositoryMock,
                createMatchSetupService(),
                createMatchResultService(),
                createMatchProgressService(),
                createStatisticsService(),
                createSetProgressService(),
                createLegService(),
                createLegRoundService(),
                createDartBotService(),
                createMatchPublishService()
        );
    }

    public IX01MatchResultService createMatchResultService() {
        return new X01MatchResultServiceImpl(createSetResultService(), createSetProgressService(), createStandingsService());
    }

    public IX01MatchProgressService createMatchProgressService() {
        return new X01MatchProgressServiceImpl(
                createSetService(),
                createSetProgressService(),
                createLegProgressService(),
                createLegRoundService(),
                createRulesService()
        );
    }

    public IX01SetService createSetService() {
        return new X01SetServiceImpl();
    }

    public IX01SetResultService createSetResultService() {
        return new X01SetResultServiceImpl(createLegProgressService(), createLegResultService(), createStandingsService());
    }

    public IX01SetProgressService createSetProgressService() {
        return new X01SetProgressServiceImpl(createLegService(), createLegProgressService(), createRulesService());
    }

    public IX01LegService createLegService() {
        return new X01LegServiceImpl(
                messageResolverMock,
                createLegProgressService(),
                createLegResultService(),
                createCheckoutService()
        );
    }

    public IX01LegResultService createLegResultService() {
        return new X01LegResultServiceImpl(createLegRoundService());
    }

    public IX01LegProgressService createLegProgressService() {
        return new X01LegProgressServiceImpl(createLegRoundService());
    }

    public IX01LegRoundService createLegRoundService() {
        return new X01LegRoundServiceImpl();
    }

    public IX01CheckoutService createCheckoutService() {
        Resource checkoutsResource = new ClassPathResource("data/checkouts.json");
        return new X01CheckoutServiceImpl(checkoutsResource, messageResolverMock);
    }

    public IX01MatchSetupService createMatchSetupService() {
        return new X01MatchSetupServiceImpl();
    }

    public IX01StatisticsService createStatisticsService() {
        return new X01StatisticsServiceImpl(
                createResultStatisticsService(),
                createScoreStatisticsService(),
                createCheckoutStatisticsService(),
                createAverageStatisticsService(),
                createLegService()
        );
    }

    public IX01ResultStatisticsService createResultStatisticsService() {
        return new X01ResultStatisticsServiceImpl();
    }

    public IX01CheckoutStatisticsService createCheckoutStatisticsService() {
        return new X01CheckoutStatisticsServiceImplService();
    }

    public IX01AverageStatisticsService createAverageStatisticsService() {
        return new X01AverageStatisticsServiceImpl();
    }

    public IX01ScoreStatisticsService createScoreStatisticsService() {
        return new X01ScoreStatisticsServiceImpl();
    }

    public IX01DartBotService createDartBotService() {
        return new X01DartBotServiceImpl(
                createMatchProgressService(),
                createDartBotThrowSimulator(),
                messageResolverMock,
                createLegResultService()
        );
    }

    public IX01DartBotThrowSimulator createDartBotThrowSimulator() {
        return new X01DartBotThrowSimulatorImpl(
                createDartboardService(),
                createCheckoutService(),
                createDartBotCheckoutPolicy(),
                createDartBotAccuracyCalculator(),
                createDartBotScoringStrategy());
    }

    public IDartboardService createDartboardService() {
        return new DartboardServiceImpl(new Dartboard());
    }

    public IX01DartBotScoringStrategy createDartBotScoringStrategy() {
        return new X01DartBotScoringStrategyImpl();
    }

    public IX01DartBotCheckoutPolicy createDartBotCheckoutPolicy() {
        return new X01DartBotCheckoutPolicyImpl(createCheckoutService());
    }

    public IX01DartBotAccuracyCalculator createDartBotAccuracyCalculator() {
        return new X01DartBotAccuracyCalculatorImpl();
    }

    public IX01MatchPublishService createMatchPublishService() {
        return new X01MatchPublishServiceImpl(eventPublisherMock);
    }

    public IX01StandingsService createStandingsService() {
        return new X01StandingsServiceImpl(
                createMatchProgressService(),
                createRulesService()
        );
    }

    public IX01RulesService createRulesService() {
        return new X01RulesServiceImpl();
    }
}
