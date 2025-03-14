package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
    @Min(0)
    private int set;

    @Valid
    private ArrayList<X01Leg> legs;

    private ObjectId throwsFirst;

    private Map<ObjectId, ResultType> result;
}
