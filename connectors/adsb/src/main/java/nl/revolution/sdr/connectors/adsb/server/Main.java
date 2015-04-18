package nl.revolution.sdr.connectors.adsb.server;

import nl.revolution.sdr.connectors.adsb.decoder.Dump1090Source;
import nl.revolution.sdr.services.config.ConfigService;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import nl.revolution.sdr.services.positiondata.mongodb.MongoPositionDataService;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class Main {

    private static final Logger LOG = Log.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        PositionDataService dataService = new MongoPositionDataService();
        Dump1090Source dataServer = new Dump1090Source(ConfigService.getInstance().getDump1090Url(), dataService);
        dataServer.start();

        LOG.info("Press enter to quit.");
        System.in.read();

        dataServer.stop();

    }

}
