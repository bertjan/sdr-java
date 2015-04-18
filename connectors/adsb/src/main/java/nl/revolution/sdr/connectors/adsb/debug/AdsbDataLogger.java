package nl.revolution.sdr.connectors.adsb.debug;

import nl.revolution.sdr.connectors.adsb.decoder.Dump1090Source;
import nl.revolution.sdr.services.config.ConfigService;
import nl.revolution.sdr.services.positiondata.debug.LoggingDataService;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class AdsbDataLogger {

    private static final Logger LOG = Log.getLogger(AdsbDataLogger.class);

    public static void main(String[] args) throws Exception {
        Dump1090Source dataServer = new Dump1090Source(ConfigService.getInstance().getDump1090Url(), new LoggingDataService());
        dataServer.start();

        LOG.info("Data logger started. Press enter to quit.");
        System.in.read();

        dataServer.stop();

        LOG.info("Stopped.");
    }
}

