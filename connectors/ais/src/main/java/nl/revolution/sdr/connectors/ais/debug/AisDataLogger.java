package nl.revolution.sdr.connectors.ais.debug;

import dk.tbsalling.aismessages.ais.messages.PositionReport;
import dk.tbsalling.aismessages.ais.messages.types.AISMessageType;
import dk.tbsalling.aismessages.nmea.NMEAMessageHandler;
import dk.tbsalling.aismessages.nmea.exceptions.NMEAParseException;
import dk.tbsalling.aismessages.nmea.messages.NMEAMessage;
import nl.revolution.sdr.services.positiondata.api.PositionData;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import nl.revolution.sdr.services.positiondata.mongodb.MongoPositionDataService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AisDataLogger {

    private static final Logger LOG = LoggerFactory.getLogger(AisDataLogger.class);

    private static final PositionDataService dataService = new MongoPositionDataService();

    public static void main(String... args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(1234);

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(packet);
            String aisMsg = StringUtils.trim(new String(packet.getData()));
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

                PositionData positionData = new PositionData()
                        .withType(PositionData.ObjectType.SHIP)
                        .withObjectId(String.valueOf(positionMessage.getSourceMmsi().getMMSI()))
                        .withLongitude(positionMessage.getLongitude())
                        .withLatitude(positionMessage.getLatitude())
                        .withHeading(positionMessage.getCourseOverGround())
                        .withTimestamp(System.currentTimeMillis());
                dataService.positionDataReceived(positionData);
 /*           } else if (messageType == AISMessageType.BaseStationReport) {
                BaseStationReport reportMessage = (BaseStationReport) aisMessage;
                PositionData positionData = new PositionData()
                        .withObjectId(String.valueOf(reportMessage.getSourceMmsi().getMMSI()))
                        .withLongitude(reportMessage.getLongitude())
                        .withLatitude(reportMessage.getLatitude())
                        .withHeading(0.0)
                        .withTimestamp(System.currentTimeMillis());
                dataService.positionDataReceived(positionData);
*/
            } else {
                //LOG.info("Ignoring message of type {}, content {}", messageType, aisMessage.toString());
            }
        });

        try {
            nmeaMessageHandler.accept(NMEAMessage.fromString(aisMessageString));
        } catch (NMEAParseException ex) {
            // Do nothing; just ship this message.
        }

    }

}
