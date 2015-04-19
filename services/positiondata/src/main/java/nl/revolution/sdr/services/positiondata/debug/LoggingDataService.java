package nl.revolution.sdr.services.positiondata.debug;

import nl.revolution.sdr.services.positiondata.api.PositionData;
import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import org.apache.commons.lang.NotImplementedException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggingDataService implements PositionDataService {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingDataService.class);

    @Override
    public void positionDataReceived(PositionData data) {
        LOG.info(data.toMap().toString());
    }

    @Override
    public List<JSONObject> getPositionData(Long minTimestamp) {
        throw new NotImplementedException("Logging only");
    }
}
