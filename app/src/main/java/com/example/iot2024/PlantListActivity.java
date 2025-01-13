package com.example.iot2024;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantListActivity extends AppCompatActivity {

    private List<Plant> plants = new ArrayList<>();
    private PlantAdapter adapter;

    private FirebaseDatabase database;

    private static final int REQUEST_IMAGE_CAPTURE = 7;
    private static final int PLANT_OVERVIEW = 5;

    private Plant pressedPlant;

    DatabaseReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_list);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, 10);
        }

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        String userid = "";
        if(acct!=null){
            userid = acct.getId();
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            Log.d("ANGEL", personName);
            Log.d("ANGEL", personEmail);

        }

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        user =  myRef.child(userid);
        ArrayList<DataSnapshot> devices = new ArrayList<>();

        // Read data
        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the value

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String mac = ds.getKey();
                    String ip = "";
                    String image = "";
                    String name = "";
                    Bitmap bi = null;
                    for(DataSnapshot properties : ds.getChildren()){

                        String d = properties.getKey();
                        switch (d){
                            case "image":
                                image = properties.getValue(String.class);
                                bi = base64ToBitmap(image);
                                Log.d("ANGEL VALUES", image);
                                break;
                            case "ip":
                                ip = properties.getValue(String.class);
                                Log.d("ANGEL VALUES", ip);
                                break;
                            case "name":
                                name = properties.getValue(String.class);
                                Log.d("ANGEL VALUES", ip);
                                break;

                        }

                    }

                    plants.add(new Plant(name,  image, ip, mac, bi));

                    System.out.println(plants);

                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                System.out.println("Failed to read value: " + error.toException());
            }
        });

        // Lägg till en standardplanta
        //plants.add(new Plant("Default Plant", "Default Species", null, ""));

        System.out.println(plants);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlantAdapter(plants, plant -> {
            // Navigera till MainActivity
            pressedPlant = plant;
            Log.d("Angel", "PLANT IMAGE STATUS: " + pressedPlant.getImageUri());
            Log.d("ANGEL", "PLANT EMPTY STATUS: " + pressedPlant.getImageUri().isEmpty());
            if(plant.getImageUri().isEmpty()){
                Log.d("Angel", "Starting camera");
                try {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        Log.d("Angel", "Started Camera intent");
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }

                }catch (Exception e){
                    Log.d("Angel", "CAMERA ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if(!pressedPlant.getImageUri().isEmpty()){
                Intent intent = new Intent(PlantListActivity.this, MainActivity.class);
                intent.putExtra("plantName", plant.getName());
                intent.putExtra("deviceip", plant.getIp());
                intent.putExtra("image", plant.getImageUri());
                intent.putExtra("mac", plant.getMac());
                intent.putExtra("bitmap", plant.getBi());
                Log.d("Angel", "STARTED OVERVIEW INTENT");
                startActivityForResult(intent, PLANT_OVERVIEW);
            }

        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddPlant = findViewById(R.id.fabAddPlant);
        fabAddPlant.setOnClickListener(v -> {
            Intent intent = new Intent(PlantListActivity.this, AddPlantActivity.class);
            startActivityForResult(intent, 1); // Starta AddPlantActivity
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            String b64image = data.getStringExtra("b64image");
            String ip = data.getStringExtra("ip");
            String mac = data.getStringExtra("mac");

            if (name != null && b64image != null && ip != null && mac != null) {
                Plant createdPlant = new Plant(name, b64image, ip, mac, null);
                plants.add(createdPlant);
                adapter.notifyDataSetChanged();  // Uppdatera RecyclerView

                Map<String, Object> updates = new HashMap<>();
                updates.put("name", name);
                updates.put("image", b64image);
                updates.put("ip", ip);
                updates.put("mac", mac);
                updates.put("logs", null);

                // Update the child node with multiple values
                user.child(createdPlant.getMac()).updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ANGEL", "PLANT IMAGE UPDATED IN FIREBASE");
                        })
                        .addOnFailureListener(e -> {
                            System.err.println("Failed to update data: " + e.getMessage());
                        });
            } else {
                Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Retrieve the Bitmap from the Intent
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Log.d("ANGEL", "GOT BACK DATA FROM CAMERA INTENT");
            // Convert Bitmap to Base64
            String base64String = bitmapToBase64(imageBitmap);
            pressedPlant.setImageUri(base64String);
            pressedPlant.setImageBitmap(imageBitmap);
            adapter.notifyDataSetChanged();
            user.child(pressedPlant.getMac()).child("image").setValue(base64String).addOnSuccessListener(e -> {
                Log.d("ANGEL", "PLANT IMAGE UPDATED IN FIREBASE");
            });
            Log.d("ANGEL","PLANT IMAGE HAS BEEN UPDATED IN APP" +  base64String);
        }


    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64String) {
        // Decode Base64 string into a byte array
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

        // Convert the byte array into a Bitmap
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
