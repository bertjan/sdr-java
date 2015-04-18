package nl.revolution.sdr.connectors.ais.debug;

import dk.tbsalling.aismessages.ais.messages.BaseStationReport;
import dk.tbsalling.aismessages.ais.messages.PositionReport;
import dk.tbsalling.aismessages.ais.messages.types.AISMessageType;
import dk.tbsalling.aismessages.nmea.NMEAMessageHandler;
import dk.tbsalling.aismessages.nmea.exceptions.NMEAParseException;
import dk.tbsalling.aismessages.nmea.messages.NMEAMessage;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import nl.revolution.sdr.services.positiondata.debug.LoggingDataService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class AisDataLogger {

    private static final Logger LOG = LoggerFactory.getLogger(AisDataLogger.class);

    private static final PositionDataService dataService = new LoggingDataService();

    public static void main(String... args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(1234);

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(packet);
            byte[] data = packet.getData();
            String aisMsg = StringUtils.trim(new String(data));
            decode(aisMsg);
        }
    }

    private static void decode(String aisMessageString) {
        NMEAMessageHandler nmeaMessageHandler = new NMEAMessageHandler("RTL-SDR", aisMessage -> {

            AISMessageType messageType = aisMessage.getMessageType();
            if (messageType == AISMessageType.PositionReportClassAScheduled ||
                    messageType == AISMessageType.PositionReportClassAAssignedSchedule ||
                    messageType == AISMessageType.PositionReportClassAResponseToInterrogation) {
                PositionReport positionMessage = (PositionReport) aisMessage;
                Map<String, Object> flightData = new HashMap<>();
                flightData.put("flight", positionMessage.getSourceMmsi().getMMSI());
                flightData.put("longitude", positionMessage.getLongitude());
                flightData.put("heading", positionMessage.getCourseOverGround());
                flightData.put("latitude", positionMessage.getLatitude());
                flightData.put("timestamp", System.currentTimeMillis());
                dataService.positionDataReceived(flightData);
            } else if (messageType == AISMessageType.BaseStationReport) {
                BaseStationReport reportMessage = (BaseStationReport) aisMessage;
                Map<String, Object> flightData = new HashMap<>();
                flightData.put("flight", reportMessage.getSourceMmsi().getMMSI());
                flightData.put("longitude", reportMessage.getLongitude());
                flightData.put("heading", 0.0);
                flightData.put("latitude", reportMessage.getLatitude());
                flightData.put("timestamp", System.currentTimeMillis());
                dataService.positionDataReceived(flightData);
            }
        });

        try {
            nmeaMessageHandler.accept(NMEAMessage.fromString(aisMessageString));
        } catch (NMEAParseException ex) {
            LOG.error("NMEAParseException while decoding");
        }

    }

}
