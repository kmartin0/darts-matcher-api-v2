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
@EqualsAndHashCode()
public class X01DeleteSet {
    @NotNull
    ObjectId matchId;

    @Min(0)
    int set;
}
