package nl.revolution.sdr.connectors.ais.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        LOG.info("AIS connector started. Press enter to quit.");
        System.in.read();

    }

}
