package pubsub.subscriber;

import pubsub.*;
import pubsub.middleware.Broker;

import java.io.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Subscriber {

    private String subscriberId;
    private ArrayList<String> userLines = new ArrayList<>();

    public Subscriber(){
        this.subscriberId = generateId();   //Each subscriber has a unique id
    }

    private String generateId(){
        String uniqueID = UUID.randomUUID().toString();
        return uniqueID;
    }

    public String getSubscriberId(){
        return this.subscriberId;
    }

    private List<ConnectionInfo> brokers = new ArrayList<>();

    public void addConnectionInfo(ConnectionInfo connectionInfo){
        brokers.add(connectionInfo);
    }

    public List<ConnectionInfo> getBrokerInfos(){
        return brokers;
    }

    public void establishFirstConnection() throws IOException {
        ConnectionInfo connectionInfo = brokers.get(0);
        int port = connectionInfo.getPort();
        InetAddress ip = InetAddress.getByName(connectionInfo.getIp());
        createThread(ip, port, this);

    }

    //Create a new subscriber thread that handles the connection with a broker
    public void createThread(InetAddress ip, int port, Subscriber subscriber){
        SubscriberThread subThread = new SubscriberThread(ip, port, subscriber);
        subThread.start();
    }

    public void printMessage(Value value){
        Bus bus = value.getBus();
        double lat = value.getLatitude();
        double longit = value.getLongtitude();
        String busLineId = bus.getBusLineId();
        String routeCode = bus.getRouteCode();
        String lineNumber = bus.getLineNumber();
        String vehicleId = bus.getVehicleId();

        System.out.println("Line Number: " + lineNumber +
                            " Route Code :" + routeCode +
                            " Vehicle ID :" + vehicleId +
                            " Bus Line ID :" + busLineId +
                            " Latitude :" + lat +
                            " Longitude :" + longit);
    }

    //Print all the available lines
    public void printLines(String path){
        File file1 = new File(path);
        try (BufferedReader br = new BufferedReader(new FileReader(file1))) {
            String line;
            while ((line = br.readLine()) != null) {        //Read the busLinesNew.txt line by line
                String[] tokens = line.split(",");    //For each line in the busLines.txt take the linecode, lineid and description
                System.out.println(tokens[1]);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //Read lines from the user separated by comma and add them to userLines list
    public void readLines(){
        System.out.println("Enter the lines that you want to subscribe separated by ,");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] lines = input.split(",");

        for(String line : lines) {
            userLines.add(line);
        }
    }

    public ArrayList<String> getUserLines(){
        return userLines;
    }

    public void setUserLines(ArrayList<String> updatedUserLines){
        userLines = updatedUserLines;
    }


    public static void main(String[] args){
        try {
            String path = "";

            Subscriber sub1 = new Subscriber();

            ConnectionInfo brokerInfo = new ConnectionInfo("192.168.21.105", 3201);
            ConnectionInfo brokerInfo2 = new ConnectionInfo("192.168.8.155", 3202);

            sub1.addConnectionInfo(brokerInfo2);
            sub1.addConnectionInfo(brokerInfo);

            //When the application starts print all the available lines
            sub1.printLines(path);

            //After the user see the avaiable lines, he choose the lines that he want to subscribe to and enter them separated by comma
            sub1.readLines();

            sub1.establishFirstConnection();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

