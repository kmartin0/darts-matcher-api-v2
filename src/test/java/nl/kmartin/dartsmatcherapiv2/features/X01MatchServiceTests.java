package nl.kmartin.dartsmatcherapiv2.features;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.IX01MatchRepository;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.IX01MatchService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.X01MatchServiceImpl;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class X01MatchServiceTests {

    private IX01MatchService x01MatchService;

    @Mock
    IX01MatchRepository x01MatchRepository;

    @Mock
    IX01SetService setService;

    @Mock
    IX01LegService legService;

    @Mock
    IX01LegRoundService legRoundService;

    @Mock
    IX01StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        this.x01MatchService = new X01MatchServiceImpl(x01MatchRepository, setService, legService, legRoundService, statisticsService);
    }

    @Test
    void test_1() throws IOException {
        // Given
        Mockito.when(x01MatchRepository.save(Mockito.any(X01Match.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        X01Match x01Match = new X01Match();
        X01MatchSettings x01MatchSettings = new X01MatchSettings(501, false, new X01BestOf(5, 1));
        x01Match.setMatchSettings(x01MatchSettings);

        X01MatchPlayer player1 = new X01MatchPlayer();
        player1.setPlayerName("John Doe");
        player1.setPlayerType(PlayerType.HUMAN);

        X01MatchPlayer player2 = new X01MatchPlayer();
        player1.setPlayerName("Jane Doe");
        player1.setPlayerType(PlayerType.HUMAN);

        x01Match.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));


        // When
        X01Match createdMatch = x01MatchService.createMatch(x01Match);
        Mockito.when(x01MatchRepository.findById(createdMatch.getId())).thenReturn(Optional.of(createdMatch));

        X01Turn x01Turn = new X01Turn(createdMatch.getId(), 60, 3, 0);

        x01MatchService.addTurn(x01Turn);

        // Then
        System.out.println(createdMatch);
    }


}
