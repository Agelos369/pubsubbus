package pubsub.subscriber;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.p3150039.katanemhmenaandroid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import pubsub.ConnectionInfo;
import pubsub.Info;
import pubsub.Value;

public class SubscriberActivity extends AppCompatActivity implements Serializable {

    private static final long serialVersionUID = -2459338569256719385L;
    Button button;
    private List<CheckBox> items = new ArrayList<>();       //Contains all the lines checkboxes
    private static Thread2ThreadInfo connInfo;
    private ArrayList<String> userLines = new ArrayList<>();


    //Create a checkBox for each line so the user can then select the lines he wants to subscribe to
    public void createLinesCheckBoxes(String path, LinearLayout ll) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(path)));
            String line;
            while ((line = br.readLine()) != null) {        //Read the busLinesNew.txt line by line
                String[] tokens = line.split(",");    //For each line in the busLines.txt take the linecode, lineid and description
                CheckBox ch = new CheckBox(getApplicationContext());
                ch.setText(tokens[1]);

                ll.addView(ch);

                items.add(ch);      //Add the checkboxes to the items list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);

        ScrollView sv = new ScrollView(this);
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);

        //When the application starts print all the available lines
        String path = "busLinesNew.txt";
        createLinesCheckBoxes(path, ll);

        this.setContentView(sv);

        //Button to be added after the checkboxes
        Button myButton = new Button(this);
        myButton.setId(R.id.checkbox_1);
        myButton.setText("SHOW POSITIONS");         //Set the text of the button
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.addView(myButton, lp);   //Add the button to the screen


        button = findViewById(R.id.checkbox_1
        );
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (CheckBox item : items) {        //For each checkbox
                    if (item.isChecked())            //Check if the checkox is selected
                        userLines.add(item.getText().toString());       //Add the checkbox line to the lines list
                }
                Intent mapIntent = new Intent(SubscriberActivity.this, MapsActivity.class);
                mapIntent.putExtra("userLines", userLines);
                Log.e("start", "map loading");
                SubscriberActivity.this.startActivity(mapIntent);
            }
        });
    }
}
