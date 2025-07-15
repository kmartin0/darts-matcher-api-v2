package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01BestOf {
    @Min(1)
    @Max(49)
    private int sets;

    @Min(1)
    @Max(49)
    private int legs;

    @NotNull
    private X01BestOfType bestOfType;

    @Valid
    @NotNull
    private X01ClearByTwoRule clearByTwoSetsRule;

    @Valid
    @NotNull
    private X01ClearByTwoRule clearByTwoLegsRule;

    @Valid
    @NotNull
    private X01ClearByTwoRule clearByTwoLegsInFinalSetRule;

    @JsonIgnore
    public X01ClearByTwoRule getClearByTwoLegsRuleForSet(int setNumber) {
        int bestOfSets = getSets();
        boolean isBestOfSetsReached = setNumber >= bestOfSets;

        return isBestOfSetsReached
                ? getClearByTwoLegsInFinalSetRule()
                : getClearByTwoLegsRule();
    }
}
