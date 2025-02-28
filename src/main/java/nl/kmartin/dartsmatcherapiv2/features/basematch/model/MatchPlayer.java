package nl.kmartin.dartsmatcherapiv2.features.basematch.model;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayer {
    private ObjectId playerId;

    @NotNull
    @NotEmpty
    @Length(min = 3, max = 40)
    private String playerName;

    @NotNull
    private PlayerType playerType;

    private ResultType resultType;
}