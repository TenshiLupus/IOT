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

    private Button ib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loader);


        ib = findViewById(R.id.startButton);

        ib.setOnClickListener(e -> {
            Intent intent = new Intent(Loader.this, Login.class);
            startActivity(intent);
        });
    }
}