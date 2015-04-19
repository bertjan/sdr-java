package nl.revolution.sdr.services.positiondata.api;

import org.json.simple.JSONObject;

import java.util.List;

public interface PositionDataService {

    public void positionDataReceived(PositionData data);

    public List<JSONObject> getPositionData(Long minTimestamp);
}
