package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.validators.validx01dartbotsettings.ValidX01DartBotSettings;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ValidX01DartBotSettings
public class X01MatchPlayer extends MatchPlayer {
    public X01MatchPlayer(ObjectId playerId, String playerName, PlayerType playerType, ResultType resultType,
                          X01DartBotSettings x01DartBotSettings, X01Statistics statistics) {
        super(playerId, playerName, playerType, resultType);
        this.x01DartBotSettings = x01DartBotSettings;
        this.statistics = statistics;
    }

    @Valid
    private X01DartBotSettings x01DartBotSettings;

    @Valid
    private X01Statistics statistics;
}
