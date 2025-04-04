package nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot;

import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Turn;
import org.bson.types.ObjectId;

import java.io.IOException;

public interface IX01DartBotService {
    X01Turn createDartBotTurn(@NotNull ObjectId matchId) throws IOException;
}