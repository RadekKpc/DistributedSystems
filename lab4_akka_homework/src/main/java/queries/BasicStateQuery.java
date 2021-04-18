package queries;

import akka.actor.typed.ActorRef;

public class BasicStateQuery implements DispatcherQuery {

    public final int queryId;
    public final int firstSatId;
    public final int range;
    public final int timeout;
    public final ActorRef<StationQuery> replyTo;

    public BasicStateQuery(int queryId, int firstSatId, int range, int timeout, ActorRef<StationQuery> replyTo) {
        this.queryId = queryId;
        this.firstSatId = firstSatId;
        this.range = range;
        this.timeout = timeout;
        this.replyTo = replyTo;
    }
}
