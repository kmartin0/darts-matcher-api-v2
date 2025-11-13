package nl.kmartin.dartsmatcherapiv2.config;

import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthScheduler {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthScheduler.class);
    private final MongoTemplate mongoTemplate;

    public DatabaseHealthScheduler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Health check the database every day at 3AM and 3PM.
     */
    @Scheduled(cron = "0 0 3,15 * * *")
    public void pingDatabase() {
        try {
            // Get the MongoDB database object
            MongoDatabase database = mongoTemplate.getDb();

            // Execute the "ping" command which checks the connection.
            database.runCommand(new org.bson.Document("ping", 1));

            log.info("MongoDB ping successful.");

        } catch (Exception e) {
            // The ping has failed, which indicates a database connection issue.
            log.error("MongoDB Ping Failed.", e);
        }
    }
}
