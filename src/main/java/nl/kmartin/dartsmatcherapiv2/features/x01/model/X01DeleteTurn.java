package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class X01DeleteTurn {
    @NotNull
    private ObjectId matchId;

    @NotNull
    private ObjectId playerId;

    @Min(1)
    private int set;

    @Min(1)
    private int leg;

    @Min(1)
    private int round;
}
