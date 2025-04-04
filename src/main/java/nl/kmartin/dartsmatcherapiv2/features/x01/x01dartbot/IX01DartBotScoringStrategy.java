package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.Dart;

public interface IX01DartBotScoringStrategy {
    Dart createScoringTarget(double targetOneDartAvg);
}