package com.example.deepak.shuttle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private String toLocation;

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    private Shuttle shuttle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shuttle = setup();
        // testing changes
        Map<String, ShuttleDetails> myMap = shuttle.getMainMap();
        List<String> items = new ArrayList<String>();

        for (String key : myMap.keySet()) {
            items.add(key);
        }

        Spinner dropdown = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                setToLocation((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSTracker gps = new GPSTracker(MainActivity.this);
                Location location = gps.getLocation();
                while(location == null){
                    showSettingsAlert();
                }
                Double lonn = null;
                Double latt = null;
                if (gps.canGetLocation()) {
                    latt = gps.getLatitude();
                    lonn = gps.getLongitude();
                }
                handleRequest(latt, lonn);
            }
        });
    }

    private void handleRequest(double myLatt, double myLonn) {

        double distFromMyLocToDest = getDistFromMyLocToDest(myLatt, myLonn);
        String[] closestStopArray = shuttle.getClosestStopFromMyLocation(myLatt, myLonn);
        int results[]=shuttle.calculateTime(closestStopArray,getToLocation());
        AlertDialog ad =new AlertDialog.Builder(MainActivity.this).create();
        ad.setMessage("green"+results[0]+" "+"blue"+results[1]);
        ad.show();
        double distFromClosestStopToDest = getDistThruClosestStopToDest(closestStopArray);
        if (distFromMyLocToDest > 0.4 && distFromClosestStopToDest > 0.4) {
            EditText blueStopName = (EditText) findViewById(R.id.value1);
            EditText blueStopDistance = (EditText) findViewById(R.id.value2);

            EditText greenStopName = (EditText) findViewById(R.id.value3);
            EditText greenStopDistance = (EditText) findViewById(R.id.value4);

            blueStopName.setText(closestStopArray[0]);
            blueStopDistance.setText(closestStopArray[1]);
            greenStopName.setText(closestStopArray[2]);
            greenStopDistance.setText(closestStopArray[3]);
        } else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setMessage("Take a Walk !");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    private double getDistThruClosestStopToDest(String[] closestStopArray) {
        String colorOfDest = shuttle.getMainMap().get(getToLocation()).getColor();

        int orderOfDest = shuttle.getMainMap().get(getToLocation()).getOrder();
        int orderOfClosestBlueStop = shuttle.getMainMap().get(closestStopArray[0]).getOrder();

        double latOfDest = shuttle.getMainMap().get(getToLocation()).getLatitude();
        double lonOfDest = shuttle.getMainMap().get(getToLocation()).getLongitude();

        double distance = 0.0;

        if (colorOfDest.equalsIgnoreCase("blue") && orderOfClosestBlueStop < orderOfDest) {
            distance = shuttle.getDistanceByBusStopNames(closestStopArray[0], getToLocation()) + Double.valueOf(closestStopArray[1]);
        } else {
            distance = shuttle.getDistanceByBusStopNames(closestStopArray[2], getToLocation()) + Double.valueOf(closestStopArray[3]);
        }

        return distance;
    }

    private double getDistFromMyLocToDest(double myLat, double myLonn) {
        return shuttle.getDistance(getToLocation(), myLat, myLonn);
    }

    public Shuttle setup() {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open(
                    "bus_stops")));
            String temp;
            while ((temp = br.readLine()) != null)
                sb.append(temp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String myjsonstring = sb.toString();

        Shuttle s = new Shuttle(myjsonstring);
        return s;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MainActivity.this.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
