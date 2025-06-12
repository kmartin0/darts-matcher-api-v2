package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRound {
    @Min(0)
    private int round;

    @Valid
    private Map<ObjectId, X01LegRoundScore> scores;
}
