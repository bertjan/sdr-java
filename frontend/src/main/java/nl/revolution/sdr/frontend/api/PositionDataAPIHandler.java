package nl.revolution.sdr.frontend.api;

import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionDataAPIHandler extends AbstractHandler {

    private static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=utf-8";
    private static final long DEFAULT_MAX_AGE_IN_MINUTES = 15;
    private final PositionDataService positionDataService;
    private static final Logger LOG = LoggerFactory.getLogger(PositionDataAPIHandler.class);

    public PositionDataAPIHandler(PositionDataService positionDataService) {
        this.positionDataService = positionDataService;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (!request.getRequestURI().equals("/api/positions")) {
            new NotFoundHandler().handle(target, baseRequest, request, response);
            return;
        }

        response.setContentType(CONTENT_TYPE_JSON_UTF8);
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        createAPIResponse(request.getParameter("from"), request.getParameter("to"), response.getOutputStream());
    }


    private void createAPIResponse(String inputFrom, String inputTo, OutputStream out) throws IOException {
        Long from = processInputFrom(inputFrom);
        Long to = processInputTo(inputTo);

        List<JSONObject> results = positionDataService.getPositionData(determineTimestamp(from), determineTimestamp(to));
        Map<String, List<JSONObject>> positionDataMap = convertDBResultsToPositionDataMap(results);

        write(out, "{");
        write(out, "\"from\":" + from + "," + "\"to\":" + to + ",");
        write(out, "\"positions\":[");

        int index = 0;
        Long latestTimestamp = 0l;

        for (String objectId : positionDataMap.keySet()) {
            List<Map> objectPositions = new ArrayList<>();
            List<JSONObject> positionData = positionDataMap.get(objectId);
            latestTimestamp = processPositionData(latestTimestamp, objectPositions, positionData);

            String objectType = null;
            if (!positionData.isEmpty()) {
                objectType = String.valueOf(positionData.get(0).get("objectType"));
            }

            if (!objectPositions.isEmpty()) {
                index++;
                if (index > 1) {
                    write(out, ",\n");
                }
                write(out, createPositionListForObject(objectId, objectType, objectPositions).toJSONString());
            }
        }

        write(out, "]");
        writeLastUpdatedTimestamp(out, latestTimestamp);
        write(out, "}");

        out.flush();
        out.close();
    }

    private JSONObject createPositionListForObject(String objectId, String objectType, List<Map> coords) {
        Map<String, Object> positionData = new HashMap<>();
        positionData.put("objectId", objectId);
        positionData.put("objectType", objectType);
        positionData.put("heading", String.valueOf(coords.get(coords.size() - 1).get("heading")));

        Long timestamp = Long.valueOf(String.valueOf(coords.get(coords.size() - 1).get("timestamp")));
        positionData.put("timestamp", new Date(timestamp).toString());

        // Remove unnecessary fields.
        List<String> fieldsToRemove = Arrays.asList("timestamp", "heading");
        coords.forEach(coordinate -> fieldsToRemove.forEach(coordinate::remove));
        positionData.put("positions", coords);

        return new JSONObject(positionData);
    }

    private Long processPositionData(Long latestTimestamp, List<Map> positions, List<JSONObject> positionData) {
        for (JSONObject result : positionData) {
            Map<String, String> position = new HashMap<>();

            Double latitude = (Double)result.get("latitude");
            Double longitude = (Double)result.get("longitude");

            if (!isValidCoordinate(latitude, longitude)) {
                continue;
            }

            position.put("lat", String.valueOf(latitude));
            position.put("lon", String.valueOf(longitude));
            position.put("heading", String.valueOf(result.get("heading")));

            String timestamp = String.valueOf(result.get("timestamp"));
            position.put("timestamp", timestamp);

            positions.add(position);

            Long currentTimestamp = Long.valueOf(timestamp);
            if (currentTimestamp > latestTimestamp) {
                latestTimestamp = currentTimestamp;
            }
        }
        return latestTimestamp;
    }

    private Map<String, List<JSONObject>> convertDBResultsToPositionDataMap(List<JSONObject> results) {
        Map<String,List<JSONObject>> allPositionData = new HashMap<>();
        for (JSONObject result : results) {
            String objectId = String.valueOf(result.get("objectId"));
            if (!allPositionData.containsKey(objectId)) {
                allPositionData.put(objectId, new ArrayList<>());
            }
            allPositionData.get(objectId).add(result);
        }
        return allPositionData;
    }

    private Long determineTimestamp(Long tsInMinutes) {
        if (tsInMinutes == null) {
            return null;
        }
        Long maxAgeInMS = tsInMinutes * 60 * 1000;
        return System.currentTimeMillis() - maxAgeInMS;
    }

    private Long processInputFrom(String from) {
        Long maxFromInMinutes = DEFAULT_MAX_AGE_IN_MINUTES;

        if (StringUtils.isNotEmpty(from)) {
            try {
                maxFromInMinutes = Long.valueOf(from);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid 'from' input value '" + from + "', using default value '" + maxFromInMinutes + "'.");
            }
        }
        return maxFromInMinutes;
    }

    private Long processInputTo(String to) {
        Long maxToInMinutes = null;

        if (StringUtils.isNotEmpty(to)) {
            try {
                maxToInMinutes = Long.valueOf(to);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid 'to' input value '" + to + "', using default value '" + maxToInMinutes + "'.");
            }
        }
        return maxToInMinutes;
    }

    private static void write(OutputStream out, String data) throws IOException {
        out.write(data.getBytes(Charsets.UTF_8));
    }


    private void writeLastUpdatedTimestamp(OutputStream out, Long latestTimestamp) throws IOException {
        String updated = String.valueOf(latestTimestamp);
        if ("0".equals(updated)) {
            updated = "<no data>";
        } else {
            updated = new DateTime(latestTimestamp).toString("dd/MM/yyyy HH:mm:ss");
        }
        write(out, ", \"updated\":\"" + updated + "\"");
    }

    private boolean isValidCoordinate(Double latitude, Double longitude) {
        return (latitude > 0.0 && latitude < 90.0) && (longitude > -180.0 && longitude < 180.0);
    }

}



