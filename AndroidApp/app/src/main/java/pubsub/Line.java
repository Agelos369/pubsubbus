package pubsub;


//This class represent a line from the busLineNew.txt
public class Line {
    private String lineCode;
    private String busLineId;
    private String description;

    public Line(String lineCode, String busLineId, String description) {
        this.lineCode = lineCode;
        this.busLineId = busLineId;
        this.description = description;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        lineCode = lineCode;
    }

    public String getBusLineId() {
        return busLineId;
    }

    public void setBusLineId(String busLineId) {
        this.busLineId = busLineId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Line{" +
                "LineCode='" + lineCode + '\'' +
                ", busLineId='" + busLineId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
