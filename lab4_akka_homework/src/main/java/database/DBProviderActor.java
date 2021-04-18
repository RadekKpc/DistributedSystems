package database;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import database.model.Satellite;
import database.providerQueries.DatabaseQueries;
import database.providerQueries.ErrorsForSatelliteQuery;
import database.providerQueries.ErrorsForSatelliteResponse;
import database.providerQueries.UpdateSatelliteErrorsQuery;

public class DBProviderActor extends AbstractBehavior<DatabaseQueries> {
    private final SatelliteService service;

    public DBProviderActor(ActorContext<DatabaseQueries> context) {
        super(context);
        service = SatelliteService.getInstance();
    }

    @Override
    public Receive<DatabaseQueries> createReceive() {
        return newReceiveBuilder()
                .onMessage(UpdateSatelliteErrorsQuery.class, this::onUpdateSatelliteErrorsQuery)
                .onMessage(ErrorsForSatelliteQuery.class, this::onErrorsForSatelliteQuery)
                .build();
    }

    public static Behavior<DatabaseQueries> create() {
        return Behaviors.setup(DBProviderActor::new);
    }

    private Behavior<DatabaseQueries> onErrorsForSatelliteQuery(ErrorsForSatelliteQuery request) {
        Satellite satellite = service.getSatellite(request.id);
        request.replyTo.tell(new ErrorsForSatelliteResponse(satellite));
        return this;
    }
    private Behavior<DatabaseQueries> onUpdateSatelliteErrorsQuery(UpdateSatelliteErrorsQuery request) {
        service.updateSatelliteErrors(request.id);
        return this;
    }
}
