package nl.revolution.sdr.services.positiondata.mongodb;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import nl.revolution.sdr.services.config.ConfigService;

import java.net.UnknownHostException;

public class MongoConnector {

    private static final String DB_NAME = "sdr";
    private static MongoConnector instance;
    private MongoClient client;

    private MongoConnector() throws UnknownHostException {
        client = new MongoClient(ConfigService.getInstance().getDatabaseHost());
    }

    public static MongoConnector getInstance() {
        if (instance == null) {
            synchronized(MongoConnector.class) {
                try {
                    instance = new MongoConnector();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    public DB getDB() {
        return client.getDB(DB_NAME);
    }

}
