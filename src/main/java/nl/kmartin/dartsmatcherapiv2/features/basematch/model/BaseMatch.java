package nl.kmartin.dartsmatcherapiv2.features.basematch.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapiv2.validators.noduplicatematchplayername.NoDuplicateMatchPlayerName;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseMatch<PlayerType extends MatchPlayer> {
    @MongoId
    private ObjectId id;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull
    private MatchStatus matchStatus;

    @Valid
    @NotNull
    @Size(min = 1, max = 4)
    @NoDuplicateMatchPlayerName
    private ArrayList<PlayerType> players;

    @NotNull
    private MatchType matchType;
}
