import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Main {

    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {

                    ActorRef<BasicStateQuery> dispatcher =  context.spawn(Dispatcher.create(), "dispatcher");
                    ActorRef<StationQuery> station1 = context.spawn(MonitorStation.create(), "station1");
                    ActorRef<StationQuery> station2 = context.spawn(MonitorStation.create(), "station2");

                    System.out.println("Actros have been setup");
                    Thread.sleep(2000);

                    station1.tell(new SetStationNameQuery("station1"));
                    station2.tell(new SetStationNameQuery("station2"));

                    station1.tell(new MonitorStationQuery(100,50,300,dispatcher));
                    station2.tell(new MonitorStationQuery(100,5,300,dispatcher));
                    station1.tell(new MonitorStationQuery(100,5,300,dispatcher));

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        File configFile = new File("src/main/dispatcher.conf");
        Config config = ConfigFactory.parseFile(configFile);
        System.out.println("Dispatcher config: " + config);

        ActorSystem.create(Main.create(), "SatelliteSystem", config);
    }
}
