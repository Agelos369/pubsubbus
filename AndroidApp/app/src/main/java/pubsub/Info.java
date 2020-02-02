package pubsub;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Info implements Serializable {
    private static final long serialVersionUID = -8967456786256719218L;


    private String ip;
    private int port;
    private List<String> responsibilityLines;


    public Info(String ip, int port, List<String> responsibilityLines) {
        this.ip = ip;
        this.port = port;
        this.responsibilityLines = responsibilityLines;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getSubscriberPort() {
        return port;
    }

    public void setSubscriberPort(int port) {
        this.port = port;
    }

    public List<String> getResponsibilityLines() {
        return responsibilityLines;
    }

    public void setResponsibilityLines(ArrayList<String> responsibilityLines) {
        this.responsibilityLines = responsibilityLines;
    }

    @Override
    public String toString() {
        return "Info{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", responsibilityLines=" + responsibilityLines +
                '}';
    }
}