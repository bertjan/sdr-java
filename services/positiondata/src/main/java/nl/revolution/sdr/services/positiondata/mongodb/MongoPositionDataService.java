package nl.revolution.sdr.services.positiondata.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import nl.revolution.sdr.services.positiondata.api.PositionData;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import org.json.simple.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MongoPositionDataService implements PositionDataService {

    private static final String COLLECTION_NAME = "positionData";
    private long tsLastMeasure;
    private long numberOfEventsReceived;

    public MongoPositionDataService() {
        // Fire up / check Mongo connection.
        getPositionData(System.currentTimeMillis(), null);
        tsLastMeasure = System.currentTimeMillis();
    }

    @Override
    public void positionDataReceived(PositionData positionData) {
        logPositionDataReceived();

        // Write data to Mongo.
        DBObject data = new BasicDBObject(positionData.toMap());
        data.put("createdAt", new Date());
        MongoConnector.getInstance().getDB().getCollection(COLLECTION_NAME).insert(data);
    }

    public List<JSONObject> getPositionData(Long minTimestamp, Long maxTimestamp) {
        DBObject filter = new BasicDBObject();
        if (minTimestamp != null) {
            filter.put("timestamp", new BasicDBObject("$gte", minTimestamp));
        }
        if (maxTimestamp != null) {
            if (filter.get("timestamp") == null) {
                // No filter exists, add new filter.
                filter.put("timestamp", new BasicDBObject("$lte", maxTimestamp));
            } else {
                // Filter already exists; append to the existing filter.
                ((BasicDBObject)filter.get("timestamp")).append("$lte", maxTimestamp);
            }
        }

        DBObject sort = new BasicDBObject("timestamp", 1);

        DBObject fields = new BasicDBObject("timestamp", 1)
                .append("heading", 1)
                .append("latitude", 1)
                .append("objectId", 1)
                .append("objectType", 1)
                .append("longitude", 1);

        final List<DBObject> dbResults;
            try (DBCursor cursor = MongoConnector.getInstance()
                .getDB().getCollection(COLLECTION_NAME)
                .find(filter, fields).sort(sort)) {
            dbResults = cursor.toArray();
            cursor.close();
        }

        if (dbResults == null) {
            return null;
        }

        return dbResults.stream().map(dbo -> new JSONObject(dbo.toMap())).collect(Collectors.toList());
    }


    private void logPositionDataReceived() {
        System.out.print(".");
        numberOfEventsReceived++;
        if (numberOfEventsReceived >= 50) {
            double diffSeconds = (System.currentTimeMillis() - tsLastMeasure) / 1000;
            if (diffSeconds > 0) {
                System.out.printf(" -> %.1f position events per second.\r\n", (numberOfEventsReceived / diffSeconds));
            }
            tsLastMeasure = System.currentTimeMillis();
            numberOfEventsReceived = 0;
        }
    }

}
