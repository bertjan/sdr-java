package nl.revolution.sdr.connectors.adsb.decoder;

import nl.revolution.sdr.connectors.adsb.domain.FlightData;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dump1090Source {

    private static final Logger LOG = Log.getLogger(Dump1090Source.class);
    private static final int FETCH_DATA_INTERVAL = 250;

    private final Dump1090Client dump1090Client;
    private final PositionDataService positionDataService;
    private Thread worker;
    private boolean stop;

    public Dump1090Source(String dump1090URL, PositionDataService positionDataService) {
        this.dump1090Client = new Dump1090Client(dump1090URL);
        this.positionDataService = positionDataService;

    }

    public void start() {
        worker = new FlightDataWorker();
        worker.start();
    }

    private class FlightDataWorker extends Thread {
        public void run() {
            // Keep a list of processed flight data in memory for de-duplication of flight events from Dump1090.
            Map<String, FlightData> processedData = new HashMap<>();

            LOG.info("Thread for dump1090 starting.");
            while (!stop) {
                List<FlightData> flightDataList = dump1090Client.fetchFlightData();
                for (FlightData currentFlightData : flightDataList) {
                    FlightData previousFlightData = processedData.get(currentFlightData.getFlightId());
                    // If this is a new event, persist it.
                    if (!currentFlightData.equals(previousFlightData)) {
                        positionDataService.positionDataReceived(currentFlightData.toMap());
                    }
                    processedData.put(currentFlightData.getFlightId(), currentFlightData);
                }
                waitFor(FETCH_DATA_INTERVAL);
            }

            LOG.info("Thread for dump1090 has finished.");
        }
    }

    public void stop() {
        if (worker != null) {
            if (worker.isAlive()) {
                stop = true;
                try {
                    worker.join();
                } catch (InterruptedException ex) {
                    LOG.warn("Error while stopping Dump1090 worker", ex);
                }
            }
            worker = null;
        }
    }


    private void waitFor(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

