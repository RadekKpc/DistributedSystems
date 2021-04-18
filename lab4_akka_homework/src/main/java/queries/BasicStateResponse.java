package queries;

import java.util.Map;

public class BasicStateResponse implements StationQuery{

    public final int queryId;
    public final Map<Integer, SatelliteAPI.Status> errors;
    public final double percentOfGoodTimeResponses;

    public BasicStateResponse(int queryId, Map<Integer, SatelliteAPI.Status> errors, double percentOfGoodTimeResponses) {
        this.queryId = queryId;
        this.errors = errors;
        this.percentOfGoodTimeResponses = percentOfGoodTimeResponses;

    }
}
