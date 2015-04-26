package nl.revolution.sdr.connectors.ais.decoder;

import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AISDecoderSource {
    private static final Logger LOG = LoggerFactory.getLogger(AISDecoderSource.class);

    private AISMessageDecoder aisMessageDecoder;
    private Thread worker;
    private boolean stop;

    public AISDecoderSource(PositionDataService positionDataService) {
        aisMessageDecoder = new AISMessageDecoder(positionDataService);
    }

    public void start() {
        worker = new AISDataWorker();
        worker.start();
    }

    private class AISDataWorker extends Thread {
        public void run() {
            LOG.info("Thread for AISDecoder starting.");

            try {
                DatagramSocket datagramSocket = new DatagramSocket(1234);

                while (!stop) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(packet);
                    aisMessageDecoder.processAISMessage(StringUtils.trim(new String(packet.getData())));
                }
            } catch (IOException e) {
                LOG.error("Error while receiving AISDecoder data", e);
            }

            LOG.info("Thread for AISDecoder has finished.");
        }
    }

    public void stop() {
        if (worker != null) {
            if (worker.isAlive()) {
                stop = true;
                try {
                    worker.join();
                } catch (InterruptedException ex) {
                    LOG.warn("Error while stopping AISDecoder worker", ex);
                }
            }
            worker = null;
        }
    }

}
