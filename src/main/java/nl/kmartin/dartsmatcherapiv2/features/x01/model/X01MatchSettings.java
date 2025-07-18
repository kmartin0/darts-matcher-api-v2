package nl.kmartin.dartsmatcherapiv2.features.x01.model;

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
public class X01MatchSettings {
    @Min(101)
    @Max(1001)
    private int x01;

    private boolean trackDoubles;

    @NotNull
    @Valid
    private X01BestOf bestOf;
}
