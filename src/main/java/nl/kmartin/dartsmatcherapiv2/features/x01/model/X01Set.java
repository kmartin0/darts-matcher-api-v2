package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class X01Set {

    @Min(0)
    private int set;

    @Valid
    private NavigableMap<Integer, X01Leg> legs = new TreeMap<>();

    private ObjectId throwsFirst;

    private Map<ObjectId, ResultType> result;

    public X01Set(int set, NavigableMap<Integer, X01Leg> legs, ObjectId throwsFirst, Map<ObjectId, ResultType> result) {
        this.set = set;
        this.setLegs(legs);
        this.throwsFirst = throwsFirst;
        this.result = result;
    }

    @JsonIgnore
    public @Valid NavigableMap<Integer, X01Leg> getLegs() {
        return legs;
    }

    public void setLegs(@Valid NavigableMap<Integer, X01Leg> legs) {
        this.legs = legs != null ? legs : new TreeMap<>();
    }

    // Serialize legs as a list because JSON objects don't guarantee key order.
    @JsonProperty("legs")
    public List<X01LegEntry> getLegEntries() {
        return legs.entrySet().stream()
                .map(X01LegEntry::new)
                .collect(Collectors.toList());
    }

    // Deserialize legs from a list to a NavigableMap to guarantee key order.
    @JsonProperty("legs")
    public void setLegEntries(List<X01LegEntry> entries) {
        this.legs = entries.stream()
                .collect(Collectors.toMap(
                        X01LegEntry::legNumber,
                        X01LegEntry::leg,
                        (oldVal, newVal) -> newVal,
                        TreeMap::new
                ));
    }
}
