package pubsub;

import java.io.Serializable;

public class Topic implements Serializable {
    private static final long serialVersionUID = -8589192786256719218L;

    private String busLineId;

    public Topic(String busLineId) {
        this.busLineId = busLineId;
    }

    public String getBusLineId() {
        return busLineId;
    }

    public void setBusLineId(String busLineId) {
        this.busLineId = busLineId;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "busLineId='" + busLineId + '\'' +
                '}';
    }
}
