package nl.revolution.sdr.services.positiondata.api;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public interface PositionDataService {

    public void positionDataReceived(Map data);

    public List<JSONObject> getPositionData(Long minTimestamp);
}
