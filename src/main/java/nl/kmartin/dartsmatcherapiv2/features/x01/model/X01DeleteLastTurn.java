package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DeleteLastTurn {
    @NotNull
    ObjectId matchId;
}
