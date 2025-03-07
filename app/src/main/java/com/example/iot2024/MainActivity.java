package com.example.iot2024;

import static com.example.iot2024.LocationService.ACTION_START_LOCATION_SERVICE;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;
import android.net.Uri;
import android.widget.Button;

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
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String WEB_CLIENT_ID = "357628845072-aia2a6hbtln777dfl9bmc5oatb7bpbo1.apps.googleusercontent.com";
    private String weather_api_response = "";
    private TextView mv;
    private TextView lv;
    private TextView tv;
    private ImageView qv;
    private ImageView pv;
    private Button historyButton;
    private Button backButton;


    private double longitude;
    private double latitude;

    private Switch lightToggle = null;
    private MqttAndroidClient client;
    //private static final String SERVER_URI = "tcp://broker.hivemq.com:1883";
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "MOBILEAPPLICATION";



    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private String description;
    private String soil;
    private String light;
    private String watering;
    private String userid = "";
    private String deviceLocation = "";
    private String hostname;
    private String username = "iot";
    private String password = "iot2024";

    private String image;

    private String mac;

    private Bitmap plantBitmap;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

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

        //Retrieve the signed in user in the device
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            userid = acct.getId();
        }

        //Shall extract the data associated with this plant
        Intent intent = getIntent();
        hostname = intent.getStringExtra("deviceip");
        image= intent.getStringExtra("image");
        mac = intent.getStringExtra("mac");
        plantBitmap = intent.getParcelableExtra("bitmap");


        GetGoogleIdOption googleIdOption  = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true).build();

        //Shall start the location service to obtain the coordinates of the device
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d("ANGEL LOCATION","" + fusedLocationProviderClient);
        requestLocationPermission();
        getLastKnownLocation();

        //Shall connect the application to the mqtt broker and publsh some topics accessible to all connected devices in the network
        connect();


        //Initiates the views
        lightToggle = findViewById(R.id.values_button);
        mv = findViewById(R.id.moisture_value);
        lv = findViewById(R.id.lumen_value);
        tv = findViewById(R.id.temp_value);
        qv = findViewById(R.id.info);
        pv = findViewById(R.id.plantImage);
        historyButton = findViewById(R.id.chartButton);
        backButton = findViewById(R.id.backButton);


        //Sets the passed image bitmap to render it on the interface
        pv.setImageBitmap(plantBitmap);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);


        //Goes back to the plant list after being finished
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start SecondActivity
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        //Sends the user to the chart Activity
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start SecondActivity
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                intent.putExtra("mac", mac);
                intent.putExtra("gid",userid);
                startActivity(intent);
            }
        });

        //Activates the Lamp actuator over ssh
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

        //Subscribe to topics in mqtt based on the Mac associated to the plant
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    subscribe(mac + "/moistv");
                    subscribe(mac + "/luxv");

                } else {
                    Log.d("CONNECTION ANGEL", "IS THS WORKING?");
                    System.out.println("Connected to: " + serverURI);
                    subscribe(mac + "/moistv");
                    subscribe(mac + "/luxv");
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }
            //Updates the UI based on the updates to the topic the application is subscribed to
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String newMessage = new String(message.getPayload());
                Log.d("ANGEL","Incoming message: " + newMessage + " Topic: " + topic);
                //Cut the mac portion of the topic for switch evaluation
                topic = topic.substring(mac.length() + 1);
                Log.d("ANGEL", "TOPIC VALUE RECIEVED" + topic);

                Log.d("ANGEL", "EVALUATING TOPIC VALUES");
                try {


                    switch (topic) {
                        case "luxv":
                            double value = Double.parseDouble(newMessage);
                            Log.d("ANGEL", "EVALUATING LUXV");
                            //if the registered light level falls under 15% turn on the lamp
                            if (value < 15.0) {
                                Log.d("ANGEL", "TURNING ON LIGHT");
                                run("tdtool --on 1");
                            } else run("tdtool --off 1");

                            //Cut tailing decimals to avoid interface clutter
                            lv.setText(String.format("%.2f" + " %", value));

                            break;
                        case "moistv":
                            Log.d("ANGEL", "EVALUATING MOISTV");
                            mv.setText(newMessage +" %");
                            break;

                    }
                    Log.d("ANGEL", "PASSED TOPIC READING");

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        //Lastly fetch the information about this particular plant
        fetchPlant();




    }

    //Shall request location permissions for the user
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ANGEL PERMISSION", "Permission not in package");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

    //Evaluated result from the user input on location permission
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

    //Shall get the location of the device for later usage
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Get latitude and longitude
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        fetchWeather();
                        deviceLocation =  latitude + ":" + longitude;
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

    //shall Execute the passed in command that the user may require to run over ssh on the raspberry pi
    private void run (String command) {
        Log.d("Angel", "RUNNING SSH COMMAND");
        Log.d("Angel", "NAME OF HOST: " + hostname);

        //hard coded values that we expect users to have setup on the raspberry pies, unless there is a default user on the raspberry that would allow shh without a password
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

    //Shall connect the application with mqtt
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
                    publish("plinplon", userid);
                    publish("location", deviceLocation);
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

    //helper function to subscribe to a topic
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

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Helper function to publish a topic to the mqtt broker
    private void publish(String topic, String payload) {
        if (client == null || !client.isConnected()) {
            Log.e(TAG, "Client is not connected. Cannot publish message.");
            return;
        }
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(1);
            message.setRetained(true); // Tell the broker to retain the topic
            client.publish(topic, message);
            Log.d(TAG, "Message published to topic " + topic + ": " + payload);
        } catch (MqttException e) {
            Log.e(TAG, "Error publishing message to topic " + topic, e);
        }
    }

    //Shall utilize the associated base64 image string to send the data to the plant registry api and obtain descriptions that fits the image
    private void fetchPlant() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        //Shall format the data according to the api requirements and a response with data corresponding to the plant
        executorService.execute(() -> {
            try {

                String base64String = image;
                Log.d("ANGEL PLANT", base64String);

                OkHttpClient client = new OkHttpClient().newBuilder().build();

                JSONObject json = new JSONObject();
                JSONArray images = new JSONArray();

                images.put("data:image/jpg;base64," + base64String);
                json.put("images", images);
                String jsonPayload = json.toString();

                Request request = new Request.Builder()
                        .url("https://plant.id/api/v3/identification?details=common_names,url,description,taxonomy,rank,gbif_id,inaturalist_id,image,synonyms,edible_parts,watering,best_light_condition,best_soil_type,common_uses,cultural_significance,toxicity,best_watering")
                        .post(RequestBody.create(MediaType.parse("application/json"),jsonPayload))
                        .addHeader("Api-Key", "Cy4H05QHgexHDMevHwcyaVscBPfvXXj1lEnhDwhl2ak7c1EEmN")
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                String plantidResponse = response.body().string();
                Log.d("ANGEL PLANT RESPONSE", plantidResponse);

                    // Optionally update UI
                    runOnUiThread(() -> {
                        JsonObject jsonObject = null;
                        try {
                            //Format the response to JSON to access deeply nested properties
                            Log.d("ANGEL", "PARSING RESPONSE");
                            jsonObject = JsonParser.parseString(plantidResponse).getAsJsonObject();
                            Log.d("ANGEL", "PARSED OBJECT FROM PLANT RESPONSE");


                            JsonArray ja = jsonObject.getAsJsonObject("result").getAsJsonObject("classification").getAsJsonArray("suggestions");
                            JsonObject mlp = ja.get(0).getAsJsonObject();
                            JsonObject plantDetails = mlp.getAsJsonObject("details");

                            description = plantDetails.getAsJsonObject("description").get("value").getAsString();
                            soil = plantDetails.get("best_light_condition").getAsString();
                            light = plantDetails.get("best_soil_type").getAsString();
                            watering = plantDetails.get("best_watering").getAsString();


                            //Initiate fragment that will display the plant information to the user
                            DetailFragment bottomSheetDialog = new DetailFragment();

                            //Since it is being passed to a fragment, the data needs to be inserted into a bundle
                            Bundle args = new Bundle();
                            args.putString("description", description);
                            args.putString("soil", soil);
                            args.putString("light", light);
                            args.putString("watering", watering);
                            bottomSheetDialog.setArguments(args);

                            qv.setOnClickListener((e) -> {
                                bottomSheetDialog.show(getSupportFragmentManager(), "Details");
                            });

                            System.out.println(description +"\n\n" + soil + "\n\n" + light + "\n\n" + watering + "\n\n");



                        } catch (Exception e) {
                            Log.e("ANGEL", "error with IO", e);
                        }
                    });



            } catch (Exception e) {
                Log.e(TAG, "Error fetching JSON", e);
            }
        });
    }

    //Shall obtain temperature data from a public api utilizing the previosuly obtained location from the device and help it retrieve data relative to the location of the user
    private void fetchWeather() {
        String weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=ee2f79f1eea97bc6f758346e8a0856cb";
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                // Creates a request to the api and retrieves the data from the reponse
                URL url = new URL(weatherApiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                // Check response code
                int responseCode = connection.getResponseCode();

                //parses the response till there is nothing to read
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



                    // Updates the ui with the retrieved data from the openweather API
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

