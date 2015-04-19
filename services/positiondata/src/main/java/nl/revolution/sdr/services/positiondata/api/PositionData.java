package nl.revolution.sdr.services.positiondata.api;

import java.util.HashMap;
import java.util.Map;

public class PositionData {

    public enum ObjectType { AIRCRAFT, SHIP }

    private String objectId;
    private double longitude;
    private double latitude;
    private double heading;
    private long timestamp;
    private ObjectType type;

    public PositionData withType(ObjectType type) {
        this.type = type;
        return this;
    }

    public PositionData withObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    public PositionData withLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public PositionData withLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public PositionData withHeading(double heading) {
        this.heading = heading;
        return this;
    }

    public PositionData withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Map toMap() {
        Map result = new HashMap<>();
        result.put("objectType", type.toString());
        result.put("objectId", objectId);
        result.put("longitude", longitude);
        result.put("latitude", latitude);
        result.put("heading", heading);
        result.put("timestamp", timestamp);
        return result;
    }


}
