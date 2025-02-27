package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Leg {
    private int leg;
    private ObjectId winner;
    private ObjectId throwsFirst;
    private ArrayList<X01LegRound> rounds;
}
