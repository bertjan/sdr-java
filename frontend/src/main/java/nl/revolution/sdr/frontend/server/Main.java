package nl.revolution.sdr.frontend.server;

import nl.revolution.sdr.frontend.api.WebServer;
import nl.revolution.sdr.services.config.ConfigService;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import nl.revolution.sdr.services.positiondata.mongodb.MongoPositionDataService;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        PositionDataService dataService = new MongoPositionDataService();
        Server viewServer = new WebServer().createServer(ConfigService.getInstance().getHttpPort(), dataService);
        viewServer.start();

        LOG.info("Press enter to quit.");
        System.in.read();

        viewServer.stop();
    }

}

