package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import nl.kmartin.dartsmatcherapiv2.features.dartboard.model.DartThrow;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01DartBotLegState;

import java.io.IOException;
import java.util.List;

public interface IX01DartBotThrowSimulator {
    List<DartThrow> getNextDartThrows(X01DartBotLegState dartBotLegState) throws IOException;
}