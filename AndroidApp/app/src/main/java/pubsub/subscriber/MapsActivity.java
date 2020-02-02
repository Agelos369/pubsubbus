package pubsub.subscriber;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.p3150039.katanemhmenaandroid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import pubsub.ConnectionInfo;
import pubsub.Info;
import pubsub.Value;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Thread2ThreadInfo connectionInfo;
    private String subscriberId;
    private ArrayList<String> userLines = new ArrayList<>();

    public MapsActivity() {
        this.subscriberId = generateId();   //Each subscriber has a unique id
    }

    private String generateId() {
        String uniqueID = UUID.randomUUID().toString();
        return uniqueID;
    }

    public String getSubscriberId() {
        return this.subscriberId;
    }

    private List<ConnectionInfo> brokers = new ArrayList<>();

    public void addConnectionInfo(ConnectionInfo connectionInfo) {
        brokers.add(connectionInfo);
    }

    public List<ConnectionInfo> getBrokerInfos() {
        return brokers;
    }

    public ArrayList<String> getUserLines() {
        return userLines;
    }

    public void setUserLines(ArrayList<String> updatedUserLines) {
        userLines = updatedUserLines;
    }


    public void establishFirstConnection() throws IOException {
        ConnectionInfo connectionInfo = brokers.get(0);
        int port = connectionInfo.getPort();
        InetAddress ip = InetAddress.getByName(connectionInfo.getIp());
        createThread(ip, port, this);
    }

    //Create a new subscriber thread that handles the connection with a broker
    public void createThread(InetAddress ip, int port, MapsActivity subscriber) {
        AsyncTaskParameters params = new AsyncTaskParameters(port, ip, subscriber, subscriberId, userLines);
        ConnectionThread conThread = new ConnectionThread(this);
        conThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);    //We use executeOnExecutor so that the tasks execute in parallel instead of sequential
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle b = getIntent().getExtras();
        userLines = (ArrayList<String>) b.get("userLines");

        ConnectionInfo brokerInfo = new ConnectionInfo("192.168.21.200", 3201);
        //ConnectionInfo brokerInfo2 = new ConnectionInfo("192.168.21.230", 3202);
        addConnectionInfo(brokerInfo);
        //addConnectionInfo(brokerInfo2);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            establishFirstConnection();
        } catch (IOException e) {}
    }

    public GoogleMap getmMap(){
        return this.mMap;
    }


    public class ConnectionThread extends AsyncTask<AsyncTaskParameters, Value, Void> {

        WeakReference<MapsActivity> mapsActivityWeakReference;

        public ConnectionThread(MapsActivity mapsActivity){
            mapsActivityWeakReference = new WeakReference<>(mapsActivity);
        }

        Marker m = null;
        @Override
        protected void onProgressUpdate(Value... values) {
            super.onProgressUpdate(values);

            MapsActivity activity = mapsActivityWeakReference.get();

            if(activity!=null) {
                if(m != null){
                    m.remove();
                }
                GoogleMap mMap = activity.getmMap();
                Value v = values[0];
                LatLng templt = null;
                double lat = v.getLatitude();
                double lon = v.getLongtitude();
                LatLng lt = new LatLng(lat, lon);

                templt = lt;

                MarkerOptions a = new MarkerOptions().position(lt).title(v.getBus().getBusLineId() + " " + v.getBus().getInfo());
                m = mMap.addMarker(a);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(lt));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(templt, 12.0f));
            }
        }

        @Override
        protected Void doInBackground(AsyncTaskParameters... asyncTaskParameters) {    //This thread is used to establish a connection
            MapsActivity subscriber = asyncTaskParameters[0].getSubscriber();     //Get the MapsActivity object from the parameters wrapper
            InetAddress ip = asyncTaskParameters[0].getIp();                            //Get the ip from the parameters wrapper
            int port = asyncTaskParameters[0].getPort();                                //Get the port from the parameters wrapper
            String subscriberId = asyncTaskParameters[0].getSubscriberId();
            ArrayList<String> userLines;

            List<Info> otherBrokers = new ArrayList<>();    //Contains all the other brokers informations

            try {
                // establish the connection with the server
                Socket s = new Socket(ip, port);

                // obtaining input and out streams
                ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream dis = new ObjectInputStream(s.getInputStream());

                dos.writeUTF("Subscriber");     //Notify the broker that the connection is from a subscriber
                dos.flush();

                dos.writeUTF(subscriberId);        //Send this subscriber id
                dos.flush();

                otherBrokers = (List<Info>) dis.readObject();
                Log.e("start", otherBrokers.toString());

                //Get the lines that the user wants to subscribe to
                userLines = subscriber.getUserLines();

                //Contains the lines that the user wants to subscribe and are handled by the broker of this connection
                List<String> lines = new ArrayList<>();


                for (int j = 0; j < userLines.size(); j++) {         //For each line that the user want to subscribe to
                    String line = userLines.get(j);
                    for (Info broker : otherBrokers) {           //For each broker
                        if (broker.getIp().equals(ip.getHostAddress())) {
                            List<String> respLines = broker.getResponsibilityLines();    //Get the lines that this broker handles
                            if (respLines.contains(line)) {                                //Check if this line is handled by this broker
                                lines.add(line);                                         //Add this line to a list
                            }
                        }
                    }
                }

                for (String li : lines) {
                    int p = userLines.indexOf(li);
                    userLines.remove(p);
                }

                subscriber.setUserLines(userLines);     //Update the userLine (in the subscriber)
                dos.writeObject(lines);                 //Send the lines that are handled by the broker of this connection to the broker
                dos.flush();


                if (!userLines.isEmpty()) {
                    for (String line : userLines) {           //For each line left in the userLines
                        for (Info broker : otherBrokers) {    //For each broker
                            List<String> respLines = broker.getResponsibilityLines();
                            if (respLines.contains(line)) {                                   //If this broker contains this line
                                InetAddress ip1 = InetAddress.getByName(broker.getIp());
                                int port1 = broker.getSubscriberPort();
                                Log.e("start", "new thread creation");
                                subscriber.createThread(ip1, port1, subscriber);              //Create a new connection with this broker
                            }
                        }
                    }
                }

                //while(true){
                    Value message = null;

                    try {
                        String moreMessages;
                        int i = 0;
                        moreMessages = dis.readUTF();
                        while (moreMessages.equals("New message")) {
                            message = (Value) dis.readUnshared();
                            Log.e("start", message.toString());
                            publishProgress(message);
                            moreMessages = dis.readUTF();
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                //}
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Thread2ThreadInfo info){
            //delegate.processFinish(info);
        }

    }
}
