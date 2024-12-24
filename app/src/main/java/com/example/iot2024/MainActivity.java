package com.example.iot2024;

import static com.example.iot2024.LocationService.ACTION_START_LOCATION_SERVICE;

import android.annotation.SuppressLint;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class MainActivity extends AppCompatActivity {

    private String weather_api_response = "";
    private TextView mv;
    private TextView lv;
    private TextView tv;

    private double longitude;
    private double latitude;
    private Switch lightToggle = null;
    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://broker.hivemq.com:1883";
    private static final String TAG = "MOBILEAPPLICATION";

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationService ls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d("ANGEL LOCATION","" + fusedLocationProviderClient);
        requestLocationPermission();
        getLastKnownLocation();
        connect();


        lightToggle = findViewById(R.id.values_button);
        mv = findViewById(R.id.moisture_value);
        lv = findViewById(R.id.lumen_value);
        tv = findViewById(R.id.temp_value);



        lightToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            // below you write code to change switch status and action to take
                        if (isChecked) {
                            run("tdtool --on 1");
                            //new AsyncTask<Integer, Void, Void>() {
                            //    @Override
                            //    protected Void doInBackground(Integer... params) {
                            //        run("tdtool --on 1");
                            //        return null;
                            //    }
                            //}.execute(1);
                        } else {
                            run("tdtool --off 1");
                        }

                    }
                });

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    subscribe("moistv");
                    subscribe("luxv");
                } else {
                    Log.d("CONNECTION ANGEL", "IS THS WORKING?");
                    System.out.println("Connected to: " + serverURI);
                    subscribe("moistv");
                    subscribe("luxv");
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String newMessage = new String(message.getPayload());
                System.out.println("Incoming message: " + newMessage);

                switch(topic) {
                    case "luxv":

                        lv.setText(newMessage);

                        break;
                    case "moistv":

                        mv.setText(newMessage);
                        break;

                }

                /*
                String weatherApiUrl = "https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=tIZjAylkQoHd5kF2mxZnAvJnxTyekszv";
                fetchJson(weatherApiUrl);
                JsonObject jsonObject = JsonParser.parseString(weather_api_response).getAsJsonObject();
                JsonObject timelines = jsonObject.getAsJsonObject("timelines");
                Log.d("ANGEL temp","" + timelines.getAsJsonObject("minutely"));
                */


            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });


        String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=42.3478&lon=-71.0466&units=metric&appid=ee2f79f1eea97bc6f758346e8a0856cb";
        fetchJson(weatherApiUrl);

    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ANGEL PERMISSION", "Permission not in package");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("ANGEL", "Location permission granted");
                getLastKnownLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Get latitude and longitude
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        //String weatherApiUrl = "https://api.tomorrow.io/v4/weather/forecast?location=" +latitude +","+longitude +"&apikey=tIZjAylkQoHd5kF2mxZnAvJnxTyekszv";

                        //String weatherApiUrl = "https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=tIZjAylkQoHd5kF2mxZnAvJnxTyekszv";

                        //System.out.println(weatherApiUrl);
                        //fetchJson(weatherApiUrl);

                        // Display location
                        Toast.makeText(this, "Lat: " + latitude + ", Lon: " + longitude, Toast.LENGTH_SHORT).show();
                        System.out.println("Lat: " + latitude + ", Lon: " + longitude);
                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                    Log.e("Location", "Error: " + e.getMessage());
                });
    }

    public void run (String command) {
        String hostname = "192.168.1.120";
        String username = "iot";
        String password = "iot2024";
        try
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder() .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Connection conn = new Connection(hostname); //init connection
            conn.connect(); //start connection to the hostname

            boolean isAuthenticated = conn.authenticateWithPassword(username,password);
            if (!isAuthenticated)
                throw new IOException("Authentication failed.");
            Session sess = conn.openSession();
            sess.execCommand(command);
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            //reads text
            while (true){
                String line = br.readLine(); // read line
                if (line == null)
                    break;
                System.out.println(line);

            }
            /* Show exit status, if available (otherwise "null") */
            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close(); // Close this session
            conn.close();
        }
        catch (IOException e)
        { e.printStackTrace(System.err);
            System.exit(2); }
    }

    private void connect(){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), SERVER_URI, clientId);
        Log.wtf("ANGEL", "IS THis CONNECTING?");
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                // We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " + SERVER_URI);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscription successful to topic: " + topic);

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to subscribe to topic: " + topic);
// The subscription could not be performed, maybe the user was not
// authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void fetchJson(String urlString) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                // Create URL object
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Log response
                    String jsonResponse = response.toString();
                    Log.d("ANGEL Response", "Response: " + jsonResponse);
                    weather_api_response = jsonResponse;
                    System.out.println(weather_api_response);



                    // Optionally update UI
                    runOnUiThread(() -> {
                        JsonObject jsonObject = JsonParser.parseString(weather_api_response).getAsJsonObject();

                        tv.setText(jsonObject.getAsJsonObject("main").get("temp").getAsString());
                    });


                } else {
                    Log.e(TAG, "GET request failed with code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching JSON", e);
            }
        });
    }
}

