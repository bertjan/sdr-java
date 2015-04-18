package nl.revolution.sdr.connectors.adsb.domain;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FlightData {

    private String flightId;
    private long altitude;
    private String icao;
    private double longitude;
    private long heading;
    private double latitude;
    private long speed;
    private long timestamp;

    public static FlightData fromJSON(JSONObject json) {
        FlightData data = new FlightData();

        // Data without flightId is unusable.
        Object flightId = json.get("flight");
        if (flightId == null || StringUtils.isBlank(flightId.toString())) {
            return data;
        }

        data.flightId = StringUtils.trim(flightId.toString());
        data.altitude = (long) json.get("altitude");
        data.icao = (String) json.get("hex");
        data.longitude = (double) json.get("lon");
        data.heading = (long) json.get("track");
        data.latitude = (double) json.get("lat");
        data.speed = (long) json.get("speed");
        return data;
    }

    public boolean isComplete() {
        return StringUtils.isNotBlank(flightId)
                && longitude != 0.0
                && latitude != 0.0;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{")
                .append("\"flight\":\"").append(flightId).append("\",")
                .append("\"altitude\":").append(altitude).append(",")
                .append("\"icao\":\"").append(icao).append("\",")
                .append("\"longitude\":").append(longitude).append(",")
                .append("\"heading\":").append(heading).append(",")
                .append("\"latitude\":").append(latitude).append(",")
                .append("\"speed\":").append(speed).append(",")
                .append("\"timestamp\":").append(timestamp)
                .append("}");
        return result.toString();
    }


    public Map toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("flight", flightId);
        map.put("altitude", altitude);
        map.put("icao", icao);
        map.put("longitude", longitude);
        map.put("heading", heading);
        map.put("latitude", latitude);
        map.put("speed", speed);
        map.put("timestamp", timestamp);
        return map;
    }


    public Map toMapWithoutTimestamp() {
        Map result = this.toMap();
        result.remove("timestamp");
        return result;
    }

    public boolean equals(FlightData other) {
        if (other == null) {
            return false;
        }
        return this.toMapWithoutTimestamp().equals(other.toMapWithoutTimestamp());
    }

    public String getFlightId() {
        return flightId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
