package nl.revolution.sdr.connectors.ais.debug;

import nl.revolution.sdr.connectors.ais.decoder.AISMessageDecoder;
import nl.revolution.sdr.services.positiondata.debug.LoggingDataService;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AISTMPDataLogger {

    public static void main(String... args) throws IOException {
        AISMessageDecoder aisMessageDecoder = new AISMessageDecoder(new LoggingDataService());
        DatagramSocket datagramSocket = new DatagramSocket(1234);

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(packet);
            aisMessageDecoder.processAISMessage(StringUtils.trim(new String(packet.getData())));
        }
    }

}
