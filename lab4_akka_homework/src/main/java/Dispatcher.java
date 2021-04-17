import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Dispatcher extends AbstractBehavior<BasicStateQuery> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    public Dispatcher(ActorContext<BasicStateQuery> context) {
        super(context);
    }

    public static Behavior<BasicStateQuery> create() {
        return Behaviors.setup(Dispatcher::new);
    }

    @Override
    public Receive<BasicStateQuery> createReceive() {
        return newReceiveBuilder()
                .onMessage(BasicStateQuery.class, this::onBasicStateQuery)
                .build();
    }

    private Behavior<BasicStateQuery> onBasicStateQuery(BasicStateQuery request) throws ExecutionException, InterruptedException {
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
                if(responses.get(i).isDone()){
                    if(!isTaken.get(i)) {
                        SatelliteAPI.Status status = responses.get(i).get();
                        if (!status.equals(SatelliteAPI.Status.OK)) {
                            errors.put(i, status);
                        }
                        isTaken.put(i, true);
                        responsesOnTime += 1;
                    }
                }
                if(System.currentTimeMillis() - requestsTimes.get(i) > request.timeout ){
                    responses.get(i).cancel(true);
                    canceled += 1;
                }
            }
        }

        BasicStateResponse response = new BasicStateResponse(request.queryId,errors,(double) responsesOnTime/request.range);
        request.replyTo.tell(response);

        return this;
    }
}
