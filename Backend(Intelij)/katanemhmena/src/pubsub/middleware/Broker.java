package pubsub.middleware;

import pubsub.*;
import pubsub.subscriber.Subscriber;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


//Server class
public class Broker {

    //The ip of the broker
    private String ip;

    //The id of the broker
    private String brokerId;

    private List<ConnectionInfo> brokersInfos = new ArrayList<>();      //Contains the ips and the ports of all the other brokers

    //Contains all the topics
    public static Set<Topic> topicsSet = new HashSet<>();

    //The port that the broker listen to connections
    private int port;

    //Keeps set of subscriber topic wise, using set to prevent duplicates
    private Map<String, Set<String>> subscribersTopicMap = new HashMap<String, Set<String>>();

    //Contains the decimal form of the hash of the ip and port of this broker
    private BigInteger hashOfThisBroker;

    //Contains the hashes of the brokers sorted in ascending order
    private List<BigInteger> sortedBrokersHashesList;

    //Contains all the informations about the other brokers
    private List<Info> otherBrokers = new ArrayList<>();


    //Holds messages (bus info) published by publishers
    private Queue<Value> messagesQueue = new LinkedList<Value>();

    public Broker(int port, String ip) throws UnknownHostException, NoSuchAlgorithmException {
        this.port = port;
        this.brokerId = generateId();
        this.ip = ip;
    }

    private String generateId(){
        String uniqueID = UUID.randomUUID().toString();
        return uniqueID;
    }

    //Adds a value (bus info) to queue
    public synchronized void addMessageToQueue(Value value) {
        messagesQueue.add(value);   //When new messages are send by the publisher
        //this.notify();              //Notify the subscriber thread
    }

    //Add a new Subscriber for a topic
    public void addSubscriber(String topic, String subscriberId) {

        if (subscribersTopicMap.containsKey(topic)) {
            Set<String> subscribers = subscribersTopicMap.get(topic);
            subscribers.add(subscriberId);
            subscribersTopicMap.put(topic, subscribers);
        } else {
            Set<String> subscribers = new HashSet<String>();
            subscribers.add(subscriberId);
            subscribersTopicMap.put(topic, subscribers);
        }
    }

    //Remove an existing subscriber for a topic
    public void removeSubscriber(String topic, Subscriber subscriber){
        if(subscribersTopicMap.containsKey(topic)){
            Set<String> subscribers = subscribersTopicMap.get(topic);
            subscribers.remove(subscriber);
            subscribersTopicMap.put(topic, subscribers);
        }
    }

    //Sends message to a subscriber if there is any available
    public synchronized void pull(String subscriberId, ObjectOutputStream dos, Socket s, ObjectInputStream dis) throws IOException, InterruptedException{
        /*while(messagesQueue.isEmpty()){     //If there are no messages to be send
            this.wait();                    //Thread is waiting until there are new messages to send
        }*/
        while(!messagesQueue.isEmpty()) {
            Value message = null;
            message = messagesQueue.remove();
            System.out.println(message.toString());
            Bus bus = message.getBus();
            String topic = bus.getBusLineId();
            Set<String> subscribersOfTopic;

            if (subscribersTopicMap.containsKey(topic)) {
                subscribersOfTopic = subscribersTopicMap.get(topic);

                for (String _subscriberId : subscribersOfTopic) {
                    if (_subscriberId.equals(subscriberId)) {
                        //add broadcasted message to object output stream
                        //we use writeunshared instead of write object because we want the Value object to be always serialized in the same manner as a newly appearing object,
                        //regardless of whether or not the object has been written previously.
                        dos.writeUTF("New message");
                        dos.flush();
                        dos.writeUnshared(message);
                        dos.flush();
                    }
                }
            }
        }
       /* dos.writeUTF("stop");
        dos.flush();
        s.close();
        dos.close();
        dis.close();*/
    }

    //Return true if a subscriber is subscribed to this broker
    public boolean isSubscriber(String subscriberId){
        Iterator it = subscribersTopicMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Set<String> subscribers = (Set<String>) pair.getValue();
            for (String _subscriberId : subscribers){
                if(_subscriberId.equals(subscriberId)){
                    return true;
                }
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        return false;
    }

    //Calculate the keys (topics) that this broker can handle and add them to a list
    //Return that list
    public List<Topic> calculateKeys(Set<Topic> topicsList) throws NoSuchAlgorithmException{
        List<Topic> topicsForThisBroker = new ArrayList<>();      //Contains the topics that this broker is responsible for

        BigInteger ipAndPortHash = this.hashOfThisBroker;     //Take the decimal form of the hash of the ip plus the port

        boolean flag = false;
        if(ipAndPortHash.equals(sortedBrokersHashesList.get(0))){        //If the hash of this broker is the minimum of all the hashes
            flag = true;                                                    //Set the flag to true
        }

        int pos = -1;
        if(!flag) {                                                             //If the broker is not the one with the lower hash
            for (int i=0; i<sortedBrokersHashesList.size(); i++) {              //Find his position in list
                if (sortedBrokersHashesList.get(i).equals(ipAndPortHash)) {
                    pos=i;
                    break;
                }
            }
        }

        for(Topic topic : topicsList){                  //For each topic in the list of all topics
            String busLine = topic.getBusLineId();
            BigInteger busLineHash = hash(busLine);     //Take the decimal form of the hash of the bus line
            if(flag) {
                if (busLineHash.compareTo(ipAndPortHash) < 0 || busLineHash.compareTo(sortedBrokersHashesList.get(sortedBrokersHashesList.size() - 1)) > 0) {       //If the hash of bus line is less than the hash of the ip plus the port or if the hash of bus line is greater than the max hash
                    topicsForThisBroker.add(topic);                 //Add this topic to the list of the topics that this broker is responsible for
                }
            }else{
                int postest = pos - 1;
                if(postest>=0) {
                    if (busLineHash.compareTo(ipAndPortHash) < 0 && busLineHash.compareTo(sortedBrokersHashesList.get(postest)) > 0) {
                        topicsForThisBroker.add(topic);
                    }
                }
            }
        }
        return topicsForThisBroker;
    }

    //Return the ip and port String of this broker
    private String getIpAndPort(int port, String ip){
        int[] ipIntArray = new int[4];              //Contains the 4 parts of the ip as integers

        String[] parts = ip.split("\\.");     //Take the 4 parts of the ip and store them in an array as strings
        for (int i = 0; i < 4; i++) {                //Loop through every part of the ip and store it as an integer to ipIntArray
            ipIntArray[i] = Integer.parseInt(parts[i]);
        }

        for (int i = 0; i < 4; i++) {                //Add the port to each ip part
            ipIntArray[i] = ipIntArray[i]+port;
        }

        String ipAndPort = "";              //The ip plus the port string
        for (int i = 0; i < 4; i++) {       //Convert the ipIntArray in a string
            String part = String.valueOf(ipIntArray[i]);
            if(i == 0) {
                ipAndPort = ipAndPort + part;
            }else{
                ipAndPort = ipAndPort + "." + part;
            }
        }

        return ipAndPort;
    }


    //Create an MD5 hash
    //Return the string type of hash
    public BigInteger hash(String message) throws NoSuchAlgorithmException {
        //Creating the MessageDigest object
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        //Passing data to the created MessageDigest Object
        md.update(message.getBytes());

        //Compute the message digest
        byte[] digest = md.digest();

        //Converting the byte array in to HexString format
        StringBuffer hexString = new StringBuffer();
        for (int i = 0;i<digest.length;i++) {
            hexString.append(Integer.toHexString(0xFF & digest[i]));
        }
        String hashString = hexString.toString();

        //Transform hash to uppercase
        hashString = hashString.toUpperCase();

        //Create a new bigInteger object with the hash hexadecimal value
        BigInteger hash = new BigInteger(hashString, 16);

        //Return the decimal form of the hexadecimal hash
        return hash;

    }


    //Send the list of the topics that this broker handles
    public void sendTopicList(ObjectOutputStream dos) throws IOException, NoSuchAlgorithmException{
        dos.writeObject(calculateKeys(topicsSet));
        dos.flush();
    }


    //Read the ips and the ports of all the brokers from a
    // txt file and store them to a list
    private void readBrokersInfo(String path){
        File file1 = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(file1))) {
            String ip;
            int port;
            String line;
            while ((line = br.readLine()) != null) {        //Read the file with the ips and the ports of the brokers line by line
                String[] tokens = line.split(",");
                ip = tokens[0];
                port = Integer.parseInt(tokens[1]);
                ConnectionInfo brokerInfo = new ConnectionInfo(ip, port);       //Create a new ConnectionInfo object with the infos of all the brokers
                this.brokersInfos.add(brokerInfo);                              //Add it to the brokersInfo list
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //Set the decimal form of the hash of the ip and port of this broker
    private void setHashOfThisBroker() throws NoSuchAlgorithmException{
        String ipAndPort = getIpAndPort(port, ip);

        this.hashOfThisBroker = hash(ipAndPort);     //Take the decimal form of the hash of the ip plus the port
    }


    //Set the list that contains the sorted hashes of the brokers
    private void setSortedHashesList() throws NoSuchAlgorithmException{
        String ip;
        int port;
        String ipAndPort;
        BigInteger hash;
        List<BigInteger> hashes = new ArrayList<>();

        for(ConnectionInfo info : brokersInfos){
            ip = info.getIp();
            port = info.getPort();
            ipAndPort = getIpAndPort(port, ip);
            hash = hash(ipAndPort);
            hashes.add(hash);
        }

        Collections.sort(hashes, Comparator.naturalOrder());

        this.sortedBrokersHashesList = hashes;

    }


    public Set<Topic> getTopicsList(){
        return topicsSet;
    }


    //Read all the topics from a txt file and store them to a set (to avoid duplicate reads)
    public void setTopicsList(String path){
        File file1 = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(file1))) {
            String line;
            while ((line = br.readLine()) != null) {         //Read the file with the topics
                String[] tokens = line.split(",");    //For each line in the file take the lineCode, busLineId (Topic) and description
                Topic topic = new Topic(tokens[1]);         //Create a new topic item with the busLineId
                this.topicsSet.add(topic);                  //Add the topic to the topics Set
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public List<BigInteger> getSortedBrokersHashesList(){
        return this.sortedBrokersHashesList;
    }

    public BigInteger getHashOfThisBroker(){
        return this.hashOfThisBroker;
    }

    //Calculate the lines that the other brokers are responsibles for and add the Info objects to otherBrokers list
    private void setOtherBrokers(String path){

        File file1 = new File(path);

        try (BufferedReader br = new BufferedReader(new FileReader(file1))) {
            String ip;
            int port;
            String line;
            while ((line = br.readLine()) != null) {        //Read the file with the ips and the ports of the brokers line by line
                String[] tokens = line.split(",");
                ip = tokens[0];
                port = Integer.parseInt(tokens[1]);

                String ipAndPort = getIpAndPort(port, ip);

                List<String> topicsForThisBroker = new ArrayList<>();      //Contains the topics that this broker is responsible for

                BigInteger ipAndPortHash = hash(ipAndPort);     //Take the decimal form of the hash of the ip plus the port

                boolean flag = false;
                if (ipAndPortHash.equals(sortedBrokersHashesList.get(0))) {        //If the hash of this broker is the minimum of all the hashes
                    flag = true;                                                    //Set the flag to true
                }

                int pos = -1;
                if (!flag) {                                                             //If the broker is not the one with the lower hash
                    for (int i = 0; i < sortedBrokersHashesList.size(); i++) {              //Find his position in list
                        if (sortedBrokersHashesList.get(i).equals(ipAndPortHash)) {
                            pos = i;
                            break;
                        }
                    }
                }

                for (Topic topic : getTopicsList()) {                  //For each topic in the list of all topics
                    String busLine = topic.getBusLineId();
                    BigInteger busLineHash = hash(busLine);     //Take the decimal form of the hash of the bus line
                    if (flag) {
                        if (busLineHash.compareTo(ipAndPortHash) < 0 || busLineHash.compareTo(sortedBrokersHashesList.get(sortedBrokersHashesList.size() - 1)) > 0) {       //If the hash of bus line is less than the hash of the ip plus the port or if the hash of bus line is greater than the max hash
                            topicsForThisBroker.add(topic.getBusLineId());                 //Add this topic to the list of the topics that this broker is responsible for
                        }
                    } else {
                        int postest = pos - 1;
                        if (postest >= 0) {
                            if (busLineHash.compareTo(ipAndPortHash) < 0 && busLineHash.compareTo(sortedBrokersHashesList.get(postest)) > 0) {
                                topicsForThisBroker.add(topic.getBusLineId());
                            }
                        }
                    }
                }
                Info brokerInfo = new Info(ip, port, topicsForThisBroker);
                this.otherBrokers.add(brokerInfo);
            }
        }catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }


    public List<Info> getOtherBrokers(){
        return otherBrokers;
    }

    //Accepts publisher and subscriber connections
    public void acceptConnections() throws IOException {
        ServerSocket ss = new ServerSocket(port);
        System.out.println("Listening for connections");
        String typeOfConnection ="";

        // running infinite loop for getting
        // publisher or subscriber requests
        while (true) {
            Socket s = null;
            try {
                // socket object to receive incoming client requests
                s = ss.accept();


                // obtaining input and out streams
                ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream dis = new ObjectInputStream(s.getInputStream());


                typeOfConnection = dis.readUTF();   //Read a message with the type of client that wants to connect (publisher or subscriber)
                System.out.println(typeOfConnection);

                if (typeOfConnection.equals("Publisher")) {
                    System.out.println("Assigning new thread for this publisher");

                    // create a new publisher thread object
                    Thread threadPub = new PublisherHandler(s, dis, dos, this);

                    threadPub.start();
                } else if (typeOfConnection.equals("Subscriber")) {
                    System.out.println("Assigning new thread for this subscriber");

                    // create a new subscriber thread object
                    Thread threadSub = new SubscriberHandler(s, dis, dos, this);

                    threadSub.start();
                }
            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }
    }




    public static void main(String[] args){
        try {
            String path1 = "/Users/eleni/Desktop/DS_project_dataset-2/brokersInfos.txt";
            String path2 = "/Users/eleni/Desktop/DS_project_dataset-2/busLinesNew.txt";

            Broker broker1 = new Broker(3202, "192.168.21.230");
            broker1.readBrokersInfo(path1);          //Read the brokers ips and ports and add them to a list
            broker1.setHashOfThisBroker();          //Set the hash of the ip and port of this broker
            broker1.setSortedHashesList();          //Add the hashes of the brokers ips and ports to the sortedBrokersHashesList list
            broker1.setTopicsList(path2);           //Read the topics from a file and add them to a set
            broker1.setOtherBrokers(path1);         //Set the informations of the other brokers list


            for(Topic topic : broker1.calculateKeys(broker1.getTopicsList())){
                System.out.println(topic.getBusLineId());
            }


            broker1.acceptConnections();
        }catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }
}
