package nl.revolution.sdr.services.positiondata.api;

import org.json.simple.JSONObject;

import java.util.List;

public interface PositionDataService {

    void positionDataReceived(PositionData data);

    List<JSONObject> getPositionData(Long minTimestamp, Long maxTimestamp, String objectId, PositionData.ObjectType objectType);
}
