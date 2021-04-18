package queries;

import akka.actor.typed.ActorRef;

public class MonitorStationQuery implements StationQuery {

    public final int firstSatId;
    public final int range;
    public final int timeout;
    public final ActorRef<DispatcherQuery> dispatcher;

    public MonitorStationQuery(int firstSatId, int range, int timeout, ActorRef<DispatcherQuery> dispatcher) {
        this.firstSatId = firstSatId;
        this.range = range;
        this.timeout = timeout;
        this.dispatcher = dispatcher;
    }
}
