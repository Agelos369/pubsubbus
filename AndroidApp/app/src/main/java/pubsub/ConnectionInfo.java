package pubsub;

import java.io.Serializable;

public class ConnectionInfo implements Serializable {
    private String ip;
    private int port;
    private static final long serialVersionUID = -2459387923556719385L;

    public ConnectionInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
