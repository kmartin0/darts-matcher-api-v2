package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartBoardSection;
import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartboardSectionArea;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class X01DartBotScoringStrategyImpl implements IX01DartBotScoringStrategy {
    private static final Map<DartBoardSection, Double> TREBLE_PROBABILITIES = Map.of(
            DartBoardSection.TWENTY, 0.85,
            DartBoardSection.NINETEEN, 0.125,
            DartBoardSection.EIGHTEEN, 0.05,
            DartBoardSection.SEVENTEEN, 0.025
    );

    /**
     * Creates a biased random dartboard target intended for scoring. Will choose between
     * Treble 20 (85%), Treble 19 (12.5%), Treble 18 (5%), Treble 16 (2.5%). Always returns treble 20 if the target
     * average is a nine darter.
     *
     * @param targetOneDartAvg double   The target one dart average to determine
     * @return Dart Treble target used for scoring (T20, T19, T18 or T17).
     */
    @Override
    public Dart createScoringTarget(double targetOneDartAvg) {
        // Return Treble 20 if the target average is a nine darter or better.
        if (targetOneDartAvg >= 50) {
            return new Dart(DartBoardSection.TWENTY, DartboardSectionArea.TRIPLE);
        }

        // Generate a biased random treble using the static map.
        DartBoardSection selectedTreble = selectRandomTreble();
        return new Dart(selectedTreble, DartboardSectionArea.TRIPLE);
    }

    private DartBoardSection selectRandomTreble() {
        double rand = Math.random(); // Generate a random value between 0 and 1
        double cumulativeProbability = 0.0;

        for (Map.Entry<DartBoardSection, Double> entry : TREBLE_PROBABILITIES.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (rand <= cumulativeProbability) {
                return entry.getKey(); // Return the selected treble
            }
        }

        // Fallback, should not be reached due to the control of probabilities
        return DartBoardSection.TWENTY;
    }
}