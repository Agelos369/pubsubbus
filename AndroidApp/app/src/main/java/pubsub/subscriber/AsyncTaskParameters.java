package pubsub.subscriber;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

public class AsyncTaskParameters {
    private int port;
    private InetAddress ip;
    private MapsActivity subscriber;
    private String subscriberId;
    private ArrayList<String> userLines;

    public AsyncTaskParameters(int port, InetAddress ip, MapsActivity subscriber, String subscriberId, ArrayList<String> userLines) {
        this.port = port;
        this.ip = ip;
        this.subscriber = subscriber;
        this.subscriberId = subscriberId;
        this.userLines = userLines;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public MapsActivity getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(MapsActivity subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public String toString() {
        return "AsyncTaskParameters{" +
                "port=" + port +
                ", ip=" + ip +
                ", subscriber=" + subscriber +
                '}';
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }
}
