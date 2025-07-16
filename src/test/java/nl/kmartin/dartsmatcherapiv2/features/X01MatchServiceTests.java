package nl.kmartin.dartsmatcherapiv2.features;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api.IX01MatchRepository;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.IX01MatchSetupService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class X01MatchServiceTests {

    private IX01MatchService x01MatchService;

    @Mock
    IX01MatchRepository x01MatchRepository;

    @Mock
    IX01MatchSetupService matchSetupService;

    @Mock
    IX01MatchResultService matchResultService;

    @Mock
    IX01MatchProgressService matchProgressService;

    @Mock
    IX01StatisticsService statisticsService;

    @Mock
    IX01SetProgressService setProgressService;

    @Mock
    IX01LegService legService;

    @Mock
    IX01LegRoundService legRoundService;

    @Mock
    IX01DartBotService dartBotService;

    @Mock
    IX01MatchPublishService matchPublishService;


    @BeforeEach
    void setUp() {
        this.x01MatchService = new X01MatchServiceImpl(
                x01MatchRepository,
                matchSetupService,
                matchResultService,
                matchProgressService,
                statisticsService,
                setProgressService,
                legService,
                legRoundService,
                dartBotService,
                matchPublishService
        );
    }

    @Test
    void test_1() {
        // Given
        Mockito.when(x01MatchRepository.save(Mockito.any(X01Match.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        X01Match x01Match = new X01Match();
        X01ClearByTwoRule clearByTwoRule = new X01ClearByTwoRule(false, 0);
        X01BestOf bestOf = new X01BestOf(1, 5, X01BestOfType.SETS, clearByTwoRule, clearByTwoRule, clearByTwoRule);
        X01MatchSettings x01MatchSettings = new X01MatchSettings(501, false, bestOf);
        x01Match.setMatchSettings(x01MatchSettings);

        X01MatchPlayer player1 = new X01MatchPlayer();
        player1.setPlayerName("John Doe");
        player1.setPlayerType(PlayerType.HUMAN);

        X01MatchPlayer player2 = new X01MatchPlayer();
        player1.setPlayerName("Jane Doe");
        player1.setPlayerType(PlayerType.HUMAN);

        x01Match.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));
        x01Match.setMatchProgress(new X01MatchProgress(1, 1, 1, player1.getPlayerId()));

        // When
        X01Match createdMatch = x01MatchService.createMatch(x01Match);
        Mockito.when(x01MatchRepository.findById(createdMatch.getId())).thenReturn(Optional.of(createdMatch));
        Mockito.when(matchProgressService.getCurrentSetOrCreate(x01Match)).thenReturn(Optional.of(new X01SetEntry(1, new X01Set())));
        Mockito.when(matchProgressService.getCurrentLegOrCreate(Mockito.any(), Mockito.any())).thenReturn(Optional.of(new X01LegEntry(1, new X01Leg())));
        Mockito.when(matchProgressService.getCurrentLegRoundOrCreate(Mockito.any(), Mockito.any())).thenReturn(Optional.of(new X01LegRoundEntry(1, new X01LegRound())));

        X01Turn x01Turn = new X01Turn(60, 3, 0);

        x01MatchService.addTurn(createdMatch.getId(), x01Turn);

        // Then
        System.out.println(createdMatch);
    }

}
