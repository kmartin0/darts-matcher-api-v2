package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface X01MatchRepository extends MongoRepository<X01Match, ObjectId> {
}
