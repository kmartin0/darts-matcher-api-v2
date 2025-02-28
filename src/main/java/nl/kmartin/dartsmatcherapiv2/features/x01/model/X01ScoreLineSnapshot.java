package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01ScoreLineSnapshot {
    @Min(0)
    private int set;

    @Min(0)
    private int leg;

    private Map<ObjectId, Integer> setsWon;

    private Map<ObjectId, Integer> legsWon;
}
