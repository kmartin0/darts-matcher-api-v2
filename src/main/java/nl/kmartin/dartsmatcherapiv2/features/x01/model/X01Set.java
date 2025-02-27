package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Set {
    private int set;
    private ArrayList<X01Leg> legs;
    private Map<ObjectId, ResultType> result;
}
