import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import database.DBProviderActor;
import database.providerQueries.DatabaseQueries;
import database.providerQueries.ErrorsForSatelliteQuery;
import database.providerQueries.UpdateSatelliteErrorsQuery;
import queries.BasicStateQuery;
import queries.BasicStateResponse;
import queries.DispatcherQuery;
import queries.SatelliteAPI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Dispatcher extends AbstractBehavior<DispatcherQuery> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
    private ActorRef<DatabaseQueries> dbProvider;

    public Dispatcher(ActorContext<DispatcherQuery> context) {
        super(context);

        dbProvider = getContext().spawn(
                Behaviors.supervise(DBProviderActor.create())
                        .onFailure(Exception.class, SupervisorStrategy.resume()), "dbProviderActor");

    }

    public static Behavior<DispatcherQuery> create() {
        return Behaviors.setup(Dispatcher::new);
    }

    @Override
    public Receive<DispatcherQuery> createReceive() {
        return newReceiveBuilder()
                .onMessage(BasicStateQuery.class, this::onBasicStateQuery)
                .onMessage(ErrorsForSatelliteQuery.class, this::onErrorsForSatelliteQuery)
                .build();
    }

    private Behavior<DispatcherQuery> onBasicStateQuery(BasicStateQuery request) throws ExecutionException, InterruptedException {
        Map<Integer, SatelliteAPI.Status> errors = new HashMap<>();
        Map<Integer, Future<SatelliteAPI.Status>> responses = new HashMap<>();
        Map<Integer,Long> requestsTimes = new HashMap<>();
        Map<Integer,Boolean> isTaken = new HashMap<>();

        int responsesOnTime = 0;
        int canceled = 0;

        for(int i = request.firstSatId; i < request.firstSatId + request.range; i++){
            AsyncStaelliteAPICall call = new AsyncStaelliteAPICall(i);
            responses.put(i, executorService.submit(call));
            requestsTimes.put(i,System.currentTimeMillis());
            isTaken.put(i,false);
        }

        while (responsesOnTime + canceled < request.range){
            for(int i = request.firstSatId; i < request.firstSatId + request.range; i++){
                if (responses.get(i).isDone()) {
                    if (!isTaken.get(i)) {
                        SatelliteAPI.Status status = responses.get(i).get();
                        if (!status.equals(SatelliteAPI.Status.OK)) {
                            errors.put(i, status);
                        }
                        isTaken.put(i, true);
                        responsesOnTime += 1;
                    }
                }
                if(System.currentTimeMillis() - requestsTimes.get(i) > request.timeout ){
                    canceled += 1;
                    isTaken.put(i,true);
                }
            }
        }

//      db update
        errors.keySet().forEach(k -> dbProvider.tell(new UpdateSatelliteErrorsQuery(k)));

        BasicStateResponse response = new BasicStateResponse(request.queryId,errors,(double) responsesOnTime/request.range);
        request.replyTo.tell(response);

        return this;
    }

    private Behavior<DispatcherQuery> onErrorsForSatelliteQuery(ErrorsForSatelliteQuery request){
        dbProvider.tell(request);
        return this;
    }

}
