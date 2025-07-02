package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;

public interface IX01DartBotService {
    X01Turn createDartBotTurn(@NotNull @Valid X01Match match);
}