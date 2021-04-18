import queries.SatelliteAPI;

import java.util.concurrent.Callable;

public class AsyncStaelliteAPICall implements Callable<SatelliteAPI.Status> {

    int satId;

    public AsyncStaelliteAPICall(int satId) {
        this.satId = satId;
    }

    @Override
    public SatelliteAPI.Status call() {
        return SatelliteAPI.getStatus(satId);
    }
}
