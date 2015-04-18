package nl.revolution.sdr.connectors.adsb.decoder;

import nl.revolution.sdr.connectors.adsb.domain.FlightData;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Dump1090Client {

    private static final Logger LOG = Log.getLogger(Dump1090Client.class);

    private static final String HTTP_STATUS_OK = "HTTP/1.1 200";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final HttpGet httpGet;

    public Dump1090Client(String dump1090URL) {
        httpGet = new HttpGet(dump1090URL);
    }

    public List<FlightData> fetchFlightData() {
        List<FlightData> flightDataList = new ArrayList<>();

        // Perform GET to Dump1090 source.
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            if (!httpResponse.getStatusLine().toString().startsWith(HTTP_STATUS_OK)) {
                LOG.warn("HTTP error while fetching flight data from dump1090: [" + httpResponse.getStatusLine() + "]");
                return flightDataList;
            }

            // Parse response.
            String responseData = EntityUtils.toString(httpResponse.getEntity());
            JSONArray jsonArray = (JSONArray)JSONValue.parse(responseData);
            long currentTimestamp = System.currentTimeMillis();

            // Process all flight events.
            for (Object jo : jsonArray) {
                FlightData flightData = FlightData.fromJSON((JSONObject) jo);
                if (flightData.isComplete()) {
                    flightData.setTimestamp(currentTimestamp);
                    flightDataList.add(flightData);
                }
            }

            return flightDataList;
        } catch (IOException ex) {
            LOG.warn("Exception while fetching flight data from dump1090", ex);
        }

        return flightDataList;
    }

}
