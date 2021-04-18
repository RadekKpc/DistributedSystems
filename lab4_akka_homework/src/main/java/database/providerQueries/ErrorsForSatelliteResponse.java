package database.providerQueries;

import database.model.Satellite;
import queries.StationQuery;

public class ErrorsForSatelliteResponse implements StationQuery, DatabaseQueries {

    public Satellite satellite;

    public ErrorsForSatelliteResponse(Satellite satellite) {
        this.satellite = satellite;
    }
}
