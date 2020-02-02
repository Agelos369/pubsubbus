package pubsub.middleware;

import pubsub.Value;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;


public class PublisherHandler extends Thread{

    private final ObjectInputStream dis;
    private final ObjectOutputStream dos;
    private final Socket s;
    private Broker broker;

    public PublisherHandler(Socket s, ObjectInputStream dis, ObjectOutputStream dos, Broker broker) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.broker = broker;
    }


    @Override
    public void run()
    {
        Value receivedMessage;

        int i = 0;
        while (true)
        {
            try {
                //Send the topics that this broker handles only the first time of the communication
                if(i==0) {
                    synchronized (broker) {
                        //Send the list of the topics that this broker handles
                        dos.writeObject(broker.calculateKeys(broker.getTopicsList()));
                    }
                }

                //Receive a message from the publisher
                receivedMessage = (Value) dis.readObject();

                //Add the messages received by the publisher to the message queue
                broker.addMessageToQueue(receivedMessage);

                i++;
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}
