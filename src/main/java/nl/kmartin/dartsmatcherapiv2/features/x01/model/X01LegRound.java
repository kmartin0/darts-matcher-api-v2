package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRound {
    @NotNull
    private ObjectId throwsFirst;

    @Valid
    private ArrayList<X01LegRoundScore> scores;
}
