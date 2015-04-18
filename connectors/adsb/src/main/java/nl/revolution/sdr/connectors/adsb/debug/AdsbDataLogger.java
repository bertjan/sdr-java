package nl.revolution.sdr.connectors.adsb.debug;

import nl.revolution.sdr.connectors.adsb.decoder.Dump1090Source;
import nl.revolution.sdr.services.config.ConfigService;
import nl.revolution.sdr.services.positiondata.debug.LoggingDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdsbDataLogger {

    private static final Logger LOG = LoggerFactory.getLogger(AdsbDataLogger.class);

    public static void main(String[] args) throws Exception {
        Dump1090Source dataServer = new Dump1090Source(ConfigService.getInstance().getDump1090Url(), new LoggingDataService());
        dataServer.start();

        LOG.info("Data logger started. Press enter to quit.");
        System.in.read();

        dataServer.stop();

        LOG.info("Stopped.");
    }
}

