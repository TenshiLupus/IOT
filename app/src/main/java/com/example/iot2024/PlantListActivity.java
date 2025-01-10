package com.example.iot2024;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;

public class PlantListActivity extends AppCompatActivity {

    private List<Plant> plants = new ArrayList<>();
    private PlantAdapter adapter;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_list);

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

        DatabaseReference rpi =  myRef.child(userid);
        ArrayList<DataSnapshot> devices = new ArrayList<>();

        // Read data
        rpi.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the value

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String mac = ds.getKey();
                    String ip = "";
                    String image = "";
                    for(DataSnapshot properties : ds.getChildren()){

                        String d = properties.getKey();
                        switch (d){
                            case "image":
                                image = properties.getValue(String.class);
                                Log.d("ANGEL VALUES", image);
                                break;
                            case "ip":
                                ip = properties.getValue(String.class);
                                Log.d("ANGEL VALUES", ip);
                        }

                    }

                    plants.add(new Plant("Angel", "waw", image, ip, mac));
                    plants.add(new Plant("Default Plant", "Default Species", null, "", "na"));
                    adapter.notifyDataSetChanged();
                    System.out.println(plants);

                }


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
            Intent intent = new Intent(PlantListActivity.this, MainActivity.class);
            intent.putExtra("plantName", plant.getName());
            intent.putExtra("plantSpecies", plant.getSpecies());
            intent.putExtra("deviceip", plant.getIp());
            intent.putExtra("image", plant.getImageUri());
            intent.putExtra("mac", plant.getMac());
            startActivity(intent);
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
            String species = data.getStringExtra("species");
            String imageUri = data.getStringExtra("imageUri");
            String ip = data.getStringExtra("ip");
            String mac = data.getStringExtra("mac");

            if (name != null && species != null) {
                plants.add(new Plant(name, species, imageUri, ip, mac));
                adapter.notifyDataSetChanged();  // Uppdatera RecyclerView
            } else {
                Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
