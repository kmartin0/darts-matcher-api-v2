package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Statistics {
    @Valid
    private X01AverageStatistics averageStats;

    @Valid
    private X01CheckoutStatistics checkoutStats;

    @Valid
    private X01ScoresStatistics x01ScoresStatistics;
}
