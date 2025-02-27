package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Statistics {
    private X01AverageStatistics averageStats;
    private X01CheckoutStatistics checkoutStats;
    private X01ScoresStatistics x01ScoresStatistics;
}
