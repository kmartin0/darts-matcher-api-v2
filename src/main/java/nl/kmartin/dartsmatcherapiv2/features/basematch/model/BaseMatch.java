package nl.kmartin.dartsmatcherapiv2.features.basematch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseMatch<PlayerType extends MatchPlayer> {
    private ObjectId id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private MatchStatus matchStatus;
    private ArrayList<PlayerType> players;
    private MatchType matchType;
}
