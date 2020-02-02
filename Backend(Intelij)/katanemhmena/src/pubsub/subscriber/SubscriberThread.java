package pubsub.subscriber;

import pubsub.Info;
import pubsub.Topic;
import pubsub.Value;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public class SubscriberThread extends Thread {

    private int port;
    private InetAddress ip;
    private Subscriber subscriber;

    public SubscriberThread(InetAddress ip, int port, Subscriber subscriber) {
        this.ip = ip;
        this.port = port;
        this.subscriber = subscriber;
    }

    public void run(){

        List<Topic> topicsList = new ArrayList<>();     //Contains all the topics that the broker of this connection handles
        List<Info> otherBrokers = new ArrayList<>();    //Contains all the other brokers informations
        String subscriberId = subscriber.getSubscriberId();

        try {
            // establish the connection with the server
            Socket s = new Socket(ip, port);

            // obtaining input and out streams
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());

            dos.writeUTF("Subscriber");     //Notify the broker that the connection is from a subscriber
            dos.flush();


            String moreMessages;
            Value message;
            ArrayList<String> userLines; //Contains the lines that the user want to subscribe to

            int i = 0;
            while(true){
                synchronized (this) {
                    /*if (i == 0) {
                        topicsList = (List<Topic>) dis.readObject();      //The first time that this connection is established read the topics that this broker handles
                    }*/

                    dos.writeUTF(subscriberId);                       //Send this subscriber id
                    dos.flush();
                    //Print the topics that this broker handles to the user
                    /*for (Topic topic : topicsList) {
                        System.out.println(topic.getBusLineId());
                    }*/




                    otherBrokers = (List<Info>) dis.readObject();

                    //Get the lines that the user wants to subscribe to
                    userLines = subscriber.getUserLines();

                    //Contains the lines that the user wants to subscribe and are handled by the broker of this connection
                    List<String> lines = new ArrayList<>();


                    for(int j=0; j<userLines.size(); j++){         //For each line that the user want to subscribe to
                        String line = userLines.get(j);
                        for(Info broker : otherBrokers){           //For each broker
                            if(broker.getIp().equals(this.ip.getHostAddress())) {
                                List<String> respLines = broker.getResponsibilityLines();    //Get the lines that this broker handles
                                if (respLines.contains(line)) {                                //Check if this line is handled by this broker
                                    lines.add(line);                                         //Add this line to a list
                                }
                            }
                        }
                    }

                    for(String li : lines){
                        int p = userLines.indexOf(li);
                        userLines.remove(p);
                    }

                    //for(String o : userLines){
                      //  System.out.println(o);
                    //}

                    subscriber.setUserLines(userLines);     //Update the userLine (in the subscriber)
                    dos.writeObject(lines);                 //Send the lines that are handled by the broker of this connection to the broker
                    dos.flush();
                    //for(String k : subscriber.getUserLines()) {
                      //  System.out.println(k);
                    //}

                    if(!userLines.isEmpty()){
                        for(String line : userLines){           //For each line left in the usersLine
                            for(Info broker : otherBrokers){    //For each broker
                                List<String> respLines = broker.getResponsibilityLines();
                                if(respLines.contains(line)){                                   //If this broker contains this line
                                    InetAddress ip = InetAddress.getByName(broker.getIp());
                                    int port = broker.getSubscriberPort();
                                    subscriber.createThread(ip, port, subscriber);              //Create a new connection with this broker
                                }
                            }
                        }
                    }
                }


                moreMessages = dis.readUTF();
                while(moreMessages.equals("New message")){
                    message = (Value) dis.readUnshared();
                    System.out.println(message.getBus().getBusLineId() +" "+ message.getLatitude()+ " " + message.getLongtitude());
                    moreMessages = dis.readUTF();
                }

                i++;
            }

        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}