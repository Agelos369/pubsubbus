package pubsub.publisher;

import pubsub.ConnectionInfo;
import pubsub.Value;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;



public class Publisher{

    private int numberOfPublishers = 2;

    private List<ConnectionInfo> brokers = new ArrayList<>();

    public void addConnectionInfo(ConnectionInfo connectionInfo){
        brokers.add(connectionInfo);
    }


    public void createThreads() throws UnknownHostException {
        for(ConnectionInfo connectionInfo : brokers) {
            InetAddress ip = InetAddress.getByName(connectionInfo.getIp());
            int port = connectionInfo.getPort();
            PublisherThread pubthread = new PublisherThread(ip, port, this, numberOfPublishers, 2);
            pubthread.start();
        }
    }


    public void push(Value message, ObjectOutputStream dos) throws IOException {
        dos.writeUnshared(message);   //Send the value to the broker
        dos.flush();
    }

    public int getNumberOfPublishers(){
        return this.numberOfPublishers;
    }

    public static void main(String[] args){
        try {
            Publisher publisher1 = new Publisher();

            ConnectionInfo brokerInfo = new ConnectionInfo("192.168.21.200", 3201);
            //ConnectionInfo brokerInfo2 = new ConnectionInfo("192.168.21.230", 3202);

            //publisher1.addConnectionInfo(brokerInfo2);
            publisher1.addConnectionInfo(brokerInfo);

            publisher1.createThreads();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
