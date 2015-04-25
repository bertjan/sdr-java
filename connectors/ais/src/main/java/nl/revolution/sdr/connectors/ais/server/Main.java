package nl.revolution.sdr.connectors.ais.server;

import nl.revolution.sdr.connectors.ais.decoder.AISDecoderSource;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import nl.revolution.sdr.services.positiondata.mongodb.MongoPositionDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        PositionDataService dataService = new MongoPositionDataService();
        AISDecoderSource dataServer = new AISDecoderSource(dataService);
        dataServer.start();

        LOG.info("AIS connector started. Press enter to quit.");
        System.in.read();

        dataServer.stop();
    }

}
