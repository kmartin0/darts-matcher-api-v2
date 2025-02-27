package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01MatchPlayer extends MatchPlayer {
    private int legsWon;
    private int setsWon;
    private X01DartBotSettings x01DartBotSettings;
    private X01Statistics statistics;
}
