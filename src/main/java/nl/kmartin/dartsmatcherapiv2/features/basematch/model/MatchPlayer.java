package nl.kmartin.dartsmatcherapiv2.features.basematch.model;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayer {
    @NotNull
    private ObjectId playerId;

    @NotNull
    @NotEmpty
    private String playerName;

    @NotNull
    private PlayerType playerType;

    @NotNull
    private ResultType resultType;
}
