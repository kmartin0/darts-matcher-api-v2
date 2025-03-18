package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01MatchProgress {
    private Integer currentSet;
    private Integer currentLeg;
    private Integer currentRound;
    private ObjectId currentThrower;
}
