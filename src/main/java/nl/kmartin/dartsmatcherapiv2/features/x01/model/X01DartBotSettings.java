package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotSettings {
    public static final int MINIMUM_BOT_AVG = 1;
    public static final int MAXIMUM_BOT_AVG = 167;

    @Min(MINIMUM_BOT_AVG)
    @Max(MAXIMUM_BOT_AVG)
    private int threeDartAverage;
}
