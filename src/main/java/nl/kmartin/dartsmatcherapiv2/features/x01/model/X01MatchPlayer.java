package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class X01MatchPlayer extends MatchPlayer {
    @Min(0)
    private int legsWon;

    @Min(0)
    private int setsWon;

    @Valid
    private X01DartBotSettings x01DartBotSettings;

    @Valid
    private X01Statistics statistics;
}
