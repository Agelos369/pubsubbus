package pubsub.middleware;

import pubsub.Info;
import pubsub.subscriber.Subscriber;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class SubscriberHandler extends Thread{

    private final ObjectInputStream dis;
    private final ObjectOutputStream dos;
    private final Socket s;
    private Broker broker;

    public SubscriberHandler(Socket s, ObjectInputStream dis, ObjectOutputStream dos, Broker broker) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.broker = broker;
    }


    @Override
    public void run()
    {
        String received0;
        List<String> received3;
        int i = 0;
            try {
                //Send the topics that this broker handles only the first time of the communication
                /*if(i==0) {
                    synchronized (broker) {
                        //Send the list of the topics that this broker handles
                        dos.writeObject(broker.calculateKeys(broker.getTopicsList()));
                    }
                }*/

                //Receive the Subscriber id that is connected with
                received0 = dis.readUTF();

                dos.writeObject(broker.getOtherBrokers());
                dos.flush();

                //Read the lines that the user want to subscribe to
                received3 = (ArrayList<String>) dis.readObject();
                for(String topic : received3) {
                    broker.addSubscriber(topic, received0);
                }

                while(true) {
                    if(s.isClosed()) break;
                    if (broker.isSubscriber(received0)) {
                        //Calls broker's method to send messages to this Subscriber if there is any available
                        broker.pull(received0, dos, s, dis);
                    }
                }

                //i++;
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                e.printStackTrace();
            }
    }
}
