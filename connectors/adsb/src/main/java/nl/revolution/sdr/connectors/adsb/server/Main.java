package nl.revolution.sdr.connectors.adsb.server;

import nl.revolution.sdr.connectors.adsb.decoder.Dump1090Source;
import nl.revolution.sdr.services.config.ConfigService;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import nl.revolution.sdr.services.positiondata.mongodb.MongoPositionDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        PositionDataService dataService = new MongoPositionDataService();
        Dump1090Source dataServer = new Dump1090Source(ConfigService.getInstance().getDump1090Url(), dataService);
        dataServer.start();

        LOG.info("Press enter to quit.");
        System.in.read();

        dataServer.stop();

    }

}
