package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class X01Statistics {
    @Valid
    private X01AverageStatistics averageStats;

    @Valid
    private X01CheckoutStatistics checkoutStats;

    @Valid
    private X01ScoresStatistics x01ScoresStatistics;

    public X01Statistics() {
        this.setAverageStats(new X01AverageStatistics());
        this.setCheckoutStats(new X01CheckoutStatistics());
        this.setX01ScoresStatistics(new X01ScoresStatistics());
    }
}
