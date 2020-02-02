package pubsub.subscriber;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;

public class Thread2ThreadInfo implements Serializable {
    private  ObjectInputStream dis;
    private  ObjectOutputStream dos;
    private  SubscriberActivity subscriber;
    private InetAddress ip;
    private static final long serialVersionUID = -2459387589256719385L;

    public Thread2ThreadInfo(ObjectInputStream dis, ObjectOutputStream dos, SubscriberActivity subscriber, InetAddress ip) {
        this.dis = dis;
        this.dos = dos;
        this.subscriber = subscriber;
        this.ip = ip;
    }

    public ObjectInputStream getDis() {
        return dis;
    }

    public ObjectOutputStream getDos() {
        return dos;
    }

    public SubscriberActivity getSubscriber() {
        return subscriber;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setDis(ObjectInputStream dis) {
        this.dis = dis;
    }

    public void setDos(ObjectOutputStream dos) {
        this.dos = dos;
    }

    public void setSubscriber(SubscriberActivity subscriber) {
        this.subscriber = subscriber;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "Thread2ThreadInfo{" +
                "dis=" + dis +
                ", dos=" + dos +
                ", subscriber=" + subscriber +
                ", ip=" + ip +
                '}';
    }
}
