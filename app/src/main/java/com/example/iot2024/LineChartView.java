package com.example.iot2024;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class LineChartView extends AppCompatActivity {


    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_line_chart_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button backButton = findViewById(R.id.mainButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LineChartView.this, MainActivity.class);
                startActivity(intent);
            }
        });

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        DatabaseReference rpi =  myRef.child("48:ee:0c:f2:c9:13");

        // Read data
        rpi.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the value

                DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                List<DataSnapshot> lastHour = new ArrayList<>();
                LocalDateTime dn = LocalDateTime.now();
                TemporalAmount ta = Duration.ofHours(1);
                LocalDateTime ld = dn.minus(ta);


                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String d = ds.getKey();


                    LocalDateTime date = LocalDateTime.parse(d, sdf);
                    if(!date.isBefore(ld) && !date.isAfter(dn)){
                        lastHour.add(ds);
                    }

                    //String value = dataSnapshot.getValue(String.class);
                    //System.out.println("Value is: " + value);
                }
                for(DataSnapshot ds : lastHour){
                    String d = ds.getKey();
                    Object moistureobj = ds.child("moisture").getValue();
                    Object moisture = moistureobj != null ? moistureobj.toString() : "empty";

                    Object lightobj = ds.child("light").getValue();
                    Object light = lightobj != null ? lightobj.toString() : "empty";
                    System.out.println(d);
                    System.out.println(moisture);
                    System.out.println(light);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                System.out.println("Failed to read value: " + error.toException());
            }
        });


    }
}