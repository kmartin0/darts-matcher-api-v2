package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01BestOf {
    @Min(1)
    @Max(49)
    private int legs;

    @Min(1)
    @Max(49)
    private int sets;
}
