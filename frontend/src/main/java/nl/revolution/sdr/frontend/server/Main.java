package nl.revolution.sdr.frontend.server;

import nl.revolution.sdr.frontend.api.WebServer;
import nl.revolution.sdr.services.config.ConfigService;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import nl.revolution.sdr.services.positiondata.mongodb.MongoPositionDataService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class Main {

    private static final Logger LOG = Log.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        PositionDataService dataService = new MongoPositionDataService();
        Server viewServer = new WebServer().createServer(ConfigService.getInstance().getHttpPort(), dataService);
        viewServer.start();

        LOG.info("Press enter to quit.");
        System.in.read();

        viewServer.stop();

    }

}

