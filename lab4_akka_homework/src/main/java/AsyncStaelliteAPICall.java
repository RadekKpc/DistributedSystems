import java.util.concurrent.Callable;

public class AsyncStaelliteAPICall implements Callable<SatelliteAPI.Status> {

    int satId;

    public AsyncStaelliteAPICall(int satId) {
        this.satId = satId;
    }

    @Override
    public SatelliteAPI.Status call() throws Exception {
        return SatelliteAPI.getStatus(satId);
    }
}
