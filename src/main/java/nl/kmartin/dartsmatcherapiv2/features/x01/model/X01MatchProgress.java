package nl.kmartin.dartsmatcherapiv2.features.x01.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01MatchProgress {
    private Integer currentSet;
    private Integer currentLeg;
    private Integer currentRound;
    private ObjectId currentThrower;

    @Valid
    private ArrayList<X01ScoreLineSnapshot> scoreLineTimeline;
}
