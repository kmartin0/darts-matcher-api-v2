package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
public class X01Set {
    @Min(0)
    private int set;

    @Valid
    private ArrayList<X01Leg> legs = new ArrayList<>();

    private ObjectId throwsFirst;

    private Map<ObjectId, ResultType> result;

    public X01Set(int set, ArrayList<X01Leg> legs, ObjectId throwsFirst, Map<ObjectId, ResultType> result) {
        this.set = set;
        this.setLegs(legs);
        this.throwsFirst = throwsFirst;
        this.result = result;
    }

    public void setLegs(@Valid ArrayList<X01Leg> legs) {
        this.legs = legs != null ? legs : new ArrayList<>();
    }
}
