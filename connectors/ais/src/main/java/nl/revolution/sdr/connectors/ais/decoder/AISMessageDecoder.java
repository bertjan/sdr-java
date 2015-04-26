package nl.revolution.sdr.connectors.ais.decoder;

import dk.tbsalling.aismessages.ais.messages.PositionReport;
import dk.tbsalling.aismessages.ais.messages.types.AISMessageType;
import dk.tbsalling.aismessages.nmea.NMEAMessageHandler;
import dk.tbsalling.aismessages.nmea.exceptions.InvalidMessage;
import dk.tbsalling.aismessages.nmea.exceptions.NMEAParseException;
import dk.tbsalling.aismessages.nmea.messages.NMEAMessage;
import nl.revolution.sdr.services.positiondata.api.PositionData;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;

public class AISMessageDecoder {

    private PositionDataService positionDataService;

    public AISMessageDecoder(PositionDataService positionDataService) {
        this.positionDataService = positionDataService;
    }

    public void processAISMessage(String aisMessageString) {
        try {
            new NMEAMessageHandler("RTL-SDR", aisMessage -> {
                AISMessageType messageType = aisMessage.getMessageType();
                if (messageType == AISMessageType.PositionReportClassAScheduled ||
                        messageType == AISMessageType.PositionReportClassAAssignedSchedule ||
                        messageType == AISMessageType.PositionReportClassAResponseToInterrogation) {
                    PositionReport positionMessage = (PositionReport) aisMessage;

                    positionDataService.positionDataReceived(new PositionData()
                            .withType(PositionData.ObjectType.SHIP)
                            .withObjectId(String.valueOf(positionMessage.getSourceMmsi().getMMSI()))
                            .withLongitude(positionMessage.getLongitude())
                            .withLatitude(positionMessage.getLatitude())
                            .withHeading(positionMessage.getCourseOverGround())
                            .withTimestamp(System.currentTimeMillis()));
                }
            }).accept(NMEAMessage.fromString(aisMessageString));
        } catch (NMEAParseException | InvalidMessage e) {
            // Do nothing.
        }
    }

}
