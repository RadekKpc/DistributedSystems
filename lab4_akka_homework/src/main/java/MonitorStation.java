import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import database.providerQueries.ErrorsForSatelliteResponse;
import queries.*;

import java.util.HashMap;
import java.util.Map;

public class MonitorStation extends AbstractBehavior<StationQuery> {

    private String name;
    private final Map<Integer,Long> sendQueryTime = new HashMap<>();
    private int nextQueryId = 0;

    public MonitorStation(ActorContext<StationQuery> context) {
        super(context);
    }

    public static Behavior<StationQuery> create() {
        return Behaviors.setup(MonitorStation::new);
    }

    @Override
    public Receive<StationQuery> createReceive() {
        return newReceiveBuilder()
                .onMessage(BasicStateResponse.class, this::onBasicStateResponse)
                .onMessage(MonitorStationQuery.class, this::onMonitorStationQuery)
                .onMessage(SetStationNameQuery.class, this::onSetStationNameQuery)
                .onMessage(ErrorsForSatelliteResponse.class, this::onErrorsForSatelliteResponse)
                .build();
    }

    private Behavior<StationQuery> onBasicStateResponse(BasicStateResponse response){
        long timeTaken = System.currentTimeMillis() - sendQueryTime.get(response.queryId);
        System.out.println(name + " response in: " + timeTaken + " Good time: " + response.percentOfGoodTimeResponses);
        response.errors.forEach((sat,value) -> System.out.println(name + ": " + "satellite " + sat + " error " + value));
        return this;
    }

    private Behavior<StationQuery> onMonitorStationQuery(MonitorStationQuery query){
        BasicStateQuery message = new BasicStateQuery(nextQueryId,query.firstSatId,query.range,query.timeout, getContext().getSelf());
        query.dispatcher.tell(message);
        sendQueryTime.put(nextQueryId,System.currentTimeMillis());
        nextQueryId += 1;
        return this;
    }

    private Behavior<StationQuery> onErrorsForSatelliteResponse(ErrorsForSatelliteResponse query){
        if(query.satellite.getErrors() > 0){
            System.out.println(name + ": " + "satellite " + query.satellite.getId() + " errors: " + query.satellite.getErrors());
        }
        return this;
    }

    private Behavior<StationQuery> onSetStationNameQuery(SetStationNameQuery query){
        this.name = query.name;
        return this;
    }

}
