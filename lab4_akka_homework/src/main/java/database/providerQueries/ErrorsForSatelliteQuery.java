package database.providerQueries;

import akka.actor.typed.ActorRef;
import queries.DispatcherQuery;
import queries.StationQuery;

public class ErrorsForSatelliteQuery implements DispatcherQuery, DatabaseQueries {

    public final ActorRef<StationQuery> replyTo;
    public Integer id;

    public ErrorsForSatelliteQuery(ActorRef<StationQuery> replyTo, Integer id) {
        this.replyTo = replyTo;
        this.id = id;
    }
}
