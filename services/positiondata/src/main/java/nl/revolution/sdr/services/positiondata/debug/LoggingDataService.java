package nl.revolution.sdr.services.positiondata.debug;

import nl.revolution.sdr.services.positiondata.api.PositionDataService;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public class LoggingDataService implements PositionDataService {

    private static final Logger LOG = Log.getLogger(LoggingDataService.class);

    @Override
    public void positionDataReceived(Map data) {
        LOG.info(data.toString());
    }

    @Override
    public List<JSONObject> getPositionData(Long minTimestamp) {
        throw new NotImplementedException("Logging only");
    }
}
