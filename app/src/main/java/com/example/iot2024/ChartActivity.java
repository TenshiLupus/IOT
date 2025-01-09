package com.example.iot2024;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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
import java.util.Arrays;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private LineChart temperatureChart;
    private LineChart moistureLumenChart;
    private Button navigateButton; // Declare the button
    private FirebaseDatabase database;
    private List<String> xValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chartview);

        // Initialize views
        temperatureChart = findViewById(R.id.temperatureChart);
        moistureLumenChart = findViewById(R.id.moistureLumenChart);
        navigateButton = findViewById(R.id.button); // Initialize the button

        // Set button click listener
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to navigate to MainActivity
                Intent intent = new Intent(ChartActivity.this, MainActivity.class);
                startActivity(intent); // Start the activity
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

        Description description = new Description();
        description.setText("Levels");
        description.setPosition(150f, 15f);

        xValues = Arrays.asList("Jan", "Feb", "Mar", "Apr");

        // Set up Temperature Chart (Celsius)
        setupTemperatureChart(temperatureChart);

        // Set up Moisture and Lumen Chart (Percentage)
        setupMoistureLumenChart(moistureLumenChart);

        // Set temperature data (Celsius)
        List<Entry> tempEntries = new ArrayList<>();
        tempEntries.add(new Entry(0, 20f));
        tempEntries.add(new Entry(1, 22f));
        tempEntries.add(new Entry(2, 24f));
        tempEntries.add(new Entry(3, 26f));

        LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature (°C)");
        tempDataSet.setColor(Color.RED);

        LineData tempLineData = new LineData(tempDataSet);
        temperatureChart.setData(tempLineData);
        temperatureChart.invalidate();

        // Set moisture and lumen data (Percentage)
        List<Entry> moistureEntries = new ArrayList<>();
        moistureEntries.add(new Entry(0, 60f));
        moistureEntries.add(new Entry(1, 70f));
        moistureEntries.add(new Entry(2, 85f));
        moistureEntries.add(new Entry(3, 95f));

        List<Entry> lumenEntries = new ArrayList<>();
        lumenEntries.add(new Entry(0, 50f));
        lumenEntries.add(new Entry(1, 85f));
        lumenEntries.add(new Entry(2, 65f));
        lumenEntries.add(new Entry(3, 80f));

        LineDataSet moistureDataSet = new LineDataSet(moistureEntries, "Moisture (%)");
        moistureDataSet.setColor(Color.BLUE);

        LineDataSet lumenDataSet = new LineDataSet(lumenEntries, "Lumen (%)");
        lumenDataSet.setColor(Color.GREEN);

        LineData moistureLumenLineData = new LineData(moistureDataSet, lumenDataSet);
        moistureLumenChart.setData(moistureLumenLineData);
        moistureLumenChart.invalidate();
    }

    private void setupTemperatureChart(LineChart chart) {
        Description description = new Description();
        description.setText("Temperature Levels");
        chart.setDescription(description);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(4);
        xAxis.setGranularity(1f);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(-10f); // Adjust min temp (Celsius)
        yAxis.setAxisMaximum(40f);  // Adjust max temp (Celsius)
        yAxis.setLabelCount(6);

        chart.getAxisRight().setEnabled(false); // Disable right Y-axis
    }

    private void setupMoistureLumenChart(LineChart chart) {
        Description description = new Description();
        description.setText("Moisture and Lumen Levels");
        chart.setDescription(description);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(4);
        xAxis.setGranularity(1f);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0f); // Min value for percentage
        yAxis.setAxisMaximum(100f); // Max value for percentage
        yAxis.setLabelCount(11);

        chart.getAxisRight().setEnabled(false); // Disable right Y-axis
    }
}
