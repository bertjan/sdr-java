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

        String filter = request.getParameter("filter");
        OutputStream outputStream = response.getOutputStream();
        createAPIResponse(filter, outputStream);
        outputStream.flush();
        outputStream.close();
    }


    private void createAPIResponse(String filter, OutputStream out) throws IOException {
        Long maxHistoryInMinutes = determineMaxHistory(filter);
        Long minTimestamp = determineMinTimestamp(maxHistoryInMinutes);

        List<JSONObject> results = positionDataService.getPositionData(minTimestamp);
        Map<String, List<JSONObject>> positionDataMap = convertDBResultsToPositionDataMap(results);

        write(out, "{");
        writeHistory(out, maxHistoryInMinutes);
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
    }

    private void writeHistory(OutputStream out, Long maxHistoryInMinutes) throws IOException {
        write(out, "\"history\":" + maxHistoryInMinutes + ",");
    }

    private JSONObject createPositionListForObject(String objectId, String objectType, List<Map> coords) {
        Map<String, Object> positionData = new HashMap<>();
        positionData.put("objectId", objectId);
        positionData.put("objectType", objectType);
        positionData.put("positions", coords);
        positionData.put("heading", String.valueOf(coords.get(coords.size() - 1).get("heading")));
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
            positions.add(position);

            Long currentTimestamp = Long.valueOf(String.valueOf(result.get("timestamp")));
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

    private Long determineMinTimestamp(Long maxHistoryInMinutes) {
        Long maxAgeInMS = maxHistoryInMinutes * 60 * 1000;
        return System.currentTimeMillis() - maxAgeInMS;
    }

    private Long determineMaxHistory(String filter) {
        Long maxHistoryInMinutes = DEFAULT_MAX_AGE_IN_MINUTES;

        if (StringUtils.isNotEmpty(filter)) {
            try {
                maxHistoryInMinutes = Long.valueOf(filter);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid filter value '" + filter + "', using default value '" + maxHistoryInMinutes + "'.");
            }
        }
        return maxHistoryInMinutes;
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



