package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.validators.x01dartsbotsettings.ValidX01DartBotSettings;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ValidX01DartBotSettings
public class X01MatchPlayer extends MatchPlayer {
    public X01MatchPlayer(ObjectId playerId, String playerName, PlayerType playerType, ResultType resultType,
                          int legsWon, int setsWon, X01DartBotSettings x01DartBotSettings, X01Statistics statistics) {
        super(playerId, playerName, playerType, resultType);
        this.legsWon = legsWon;
        this.setsWon = setsWon;
        this.x01DartBotSettings = x01DartBotSettings;
        this.statistics = statistics;
    }

    @Min(0)
    private int legsWon;

    @Min(0)
    private int setsWon;

    @Valid
    private X01DartBotSettings x01DartBotSettings;

    @Valid
    private X01Statistics statistics;
}
