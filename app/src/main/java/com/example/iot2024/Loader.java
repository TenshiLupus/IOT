package com.example.iot2024;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class Loader extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loader);
        Button ib = findViewById(R.id.startButton);

        //First button that will be displayed to the user
        ib.setOnClickListener(e -> {
            Intent intent = new Intent(Loader.this, Login.class);
            startActivity(intent);
        });
    }
}