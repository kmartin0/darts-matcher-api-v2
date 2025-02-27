package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01ScoreLineSnapshot {
    private int set;
    private int leg;
    private Map<ObjectId, Integer> setsWon;
    private Map<ObjectId, Integer> legsWon;
}
