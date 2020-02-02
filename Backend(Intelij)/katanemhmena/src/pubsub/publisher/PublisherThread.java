package pubsub.publisher;

import pubsub.Bus;
import pubsub.Line;
import pubsub.Topic;
import pubsub.Value;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PublisherThread extends Thread {

    private int port;
    private InetAddress ip;
    private Publisher publisher;
    private int numberOfPublishers;
    private int id;

    public PublisherThread(InetAddress ip, int port, Publisher publisher, int numberOfPublishers, int id) {
        this.ip = ip;
        this.port = port;
        this.publisher = publisher;
        this.numberOfPublishers = numberOfPublishers;
        this.id = id;
    }

    public void run() {

        List<Topic> topicsList = new ArrayList<>();     //Contains all the topics that the broker of this connection handles
        List<Line> lines = new ArrayList<>();       //Contains all the lines we read from the busLinesNew.txt
        String path1 = "/Users/eleni/Desktop/DS_project_dataset-2/busLinesNew.txt";      //Path of the busLinesNew.txt file
        String path2 = "/Users/eleni/Desktop/DS_project_dataset-2/busPositionsNew.txt";  //Path of the busPositionsNew.txt file
        Socket s = null;

        try {
            // establish the connection with server
            s = new Socket(ip, port);

            // obtaining input and out streams
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());


            dos.writeUTF("Publisher");     //Notify the broker that the connection is from a publisher
            dos.flush();

            File file1 = new File(path1);
            try (BufferedReader br = new BufferedReader(new FileReader(file1))) {
                String line;
                while ((line = br.readLine()) != null) {        //Read the busLinesNew.txt line by line
                    String[] tokens = line.split(",");    //For each line in the busLines.txt take the linecode, lineid and description
                    Line busLine = new Line(tokens[0], tokens[1], tokens[2]);       //Create a new Line object with the file line data
                    lines.add(busLine);         //Add the Line object to the line list
                }
            }

            int i = 0;
            // the following loop performs the exchange of
            // information between client and client handler
            while (true) {
                if (i == 0) {
                    topicsList = (List<Topic>) dis.readObject();      //The first time that this connection is established read the topics that this broker handles

                    int numberOfLines = 0;
                    File file2 = new File(path2);
                    try (BufferedReader br0 = new BufferedReader(new FileReader(file2))) {
                        String line0;
                        while ((line0 = br0.readLine()) != null) {
                            numberOfLines++;
                        }
                    }

                    if(numberOfPublishers == 1) {
                        try (BufferedReader br = new BufferedReader(new FileReader(file2))) {
                            String line;
                            while ((line = br.readLine()) != null) {        //Read the busPositionsNew.txt line by line
                                String[] tokens = line.split(",");    //For each line of the busPositionsNew.txt read the LineCode, RouteCode, vehicleId, latitude, longitude, timestampOfBusPosition
                                String lineCode = tokens[0];
                                String routeCode = tokens[1];
                                String vehicleId = tokens[2];
                                String busLineId = "";
                                String info = "";
                                for (Line _line : lines) {
                                    if (_line.getLineCode().equals(lineCode)) {         //Join the busLinesNew.txt and the busPositionsNew.txt on line code
                                        busLineId = _line.getBusLineId();               //Take the busLineId of a line code from the busLinesNew.txt
                                        info = _line.getDescription();                  //Take the info of a line code from the busLinesNew.txt
                                    }
                                }

                                double latitude = Double.parseDouble(tokens[3]);
                                double longitude = Double.parseDouble(tokens[4]);


                                Boolean toSend = false;                 //A flag that indicates if we should send this value to the broker
                                for (Topic topic : topicsList) {         //For each topic that this broker handles
                                    String topicBusLineId = topic.getBusLineId();
                                    if (topicBusLineId.equals(busLineId)) {          //If the topic that we read from the file is in the list of topics that this broker handles
                                        toSend = true;                              //The flag becomes true
                                    }
                                }
                                if (toSend) {
                                    Bus bus = new Bus(lineCode, routeCode, vehicleId, busLineId, info);     //Creates a new bus object
                                    Value value = new Value(bus, latitude, longitude);                      //Create a new value object
                                    publisher.push(value, dos);

                                    try {
                                      Thread.sleep(3000);         //Wait a little bit and then send the next line of data

                                    } catch (InterruptedException e) {}
                                }
                            }
                        }
                    }else{
                        int middlePos;
                        if(numberOfLines%2 == 0){      //Find if the number of lines is not a prime
                            middlePos = numberOfLines/2;    //Find the middle position
                        }else{
                            middlePos = (numberOfLines/2) + 1;
                        }
                        try (BufferedReader br = new BufferedReader(new FileReader(file2))) {
                            String line;
                            if (id == 1) {
                                int counter = 0;
                                while ((line = br.readLine()) != null && counter < middlePos) {        //Read the busPositionsNew.txt line by line
                                    String[] tokens = line.split(",");    //For each line of the busPositionsNew.txt read the LineCode, RouteCode, vehicleId, latitude, longitude, timestampOfBusPosition
                                    String lineCode = tokens[0];
                                    String routeCode = tokens[1];
                                    String vehicleId = tokens[2];
                                    String busLineId = "";
                                    String info = "";
                                    for (Line _line : lines) {
                                        if (_line.getLineCode().equals(lineCode)) {         //Join the busLinesNew.txt and the busPositionsNew.txt on line code
                                            busLineId = _line.getBusLineId();               //Take the busLineId of a line code from the busLinesNew.txt
                                            info = _line.getDescription();                  //Take the info of a line code from the busLinesNew.txt
                                        }
                                    }

                                    double latitude = Double.parseDouble(tokens[3]);
                                    double longitude = Double.parseDouble(tokens[4]);


                                    Boolean toSend = false;                 //A flag that indicates if we should send this value to the broker
                                    for (Topic topic : topicsList) {         //For each topic that this broker handles
                                        String topicBusLineId = topic.getBusLineId();
                                        if (topicBusLineId.equals(busLineId)) {          //If the topic that we read from the file is in the list of topics that this broker handles
                                            toSend = true;                              //The flag becomes true
                                        }
                                    }
                                    if (toSend) {
                                        Bus bus = new Bus(lineCode, routeCode, vehicleId, busLineId, info);     //Creates a new bus object
                                        Value value = new Value(bus, latitude, longitude);                      //Create a new value object
                                        publisher.push(value, dos);

                                        try {
                                          Thread.sleep(3000);         //Wait a little bit and then send the next line of data

                                        } catch (InterruptedException e) {}
                                    }
                                }
                            }else {
                                int counter = 0;
                                while ((line = br.readLine()) != null && counter <= middlePos) {
                                    continue;
                                }
                                while ((line = br.readLine()) != null) {        //Read the busPositionsNew.txt line by line
                                    String[] tokens = line.split(",");    //For each line of the busPositionsNew.txt read the LineCode, RouteCode, vehicleId, latitude, longitude, timestampOfBusPosition
                                    String lineCode = tokens[0];
                                    String routeCode = tokens[1];
                                    String vehicleId = tokens[2];
                                    String busLineId = "";
                                    String info = "";
                                    for (Line _line : lines) {
                                        if (_line.getLineCode().equals(lineCode)) {         //Join the busLinesNew.txt and the busPositionsNew.txt on line code
                                            busLineId = _line.getBusLineId();               //Take the busLineId of a line code from the busLinesNew.txt
                                            info = _line.getDescription();                  //Take the info of a line code from the busLinesNew.txt
                                        }
                                    }

                                    double latitude = Double.parseDouble(tokens[3]);
                                    double longitude = Double.parseDouble(tokens[4]);


                                    Boolean toSend = false;                 //A flag that indicates if we should send this value to the broker
                                    for (Topic topic : topicsList) {         //For each topic that this broker handles
                                        String topicBusLineId = topic.getBusLineId();
                                        if (topicBusLineId.equals(busLineId)) {          //If the topic that we read from the file is in the list of topics that this broker handles
                                            toSend = true;                              //The flag becomes true
                                        }
                                    }
                                    if (toSend) {
                                        Bus bus = new Bus(lineCode, routeCode, vehicleId, busLineId, info);     //Creates a new bus object
                                        Value value = new Value(bus, latitude, longitude);                      //Create a new value object
                                        publisher.push(value, dos);

                                        try {
                                          Thread.sleep(3000);         //Wait a little bit and then send the next line of data

                                        } catch (InterruptedException e) {}
                                    }
                                }
                            }
                        }
                    }
                }

                i++;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

