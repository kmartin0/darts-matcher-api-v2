package nl.kmartin.dartsmatcherapiv2.features.basematch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayer {

    private ObjectId playerId;

    private String playerName;

    private PlayerType playerType;

    private ResultType resultType;

}
