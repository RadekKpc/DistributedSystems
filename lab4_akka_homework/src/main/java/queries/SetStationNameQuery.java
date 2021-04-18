package queries;

public class SetStationNameQuery implements StationQuery{
    public String name;

    public SetStationNameQuery(String name) {
        this.name = name;
    }
}
