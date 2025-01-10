package com.example.iot2024;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddPlantActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        EditText editPlantName = findViewById(R.id.editPlantName);
        EditText editPlantSpecies = findViewById(R.id.editPlantSpecies);
        Button btnComplete = findViewById(R.id.btnComplete);

        btnComplete.setOnClickListener(v -> {
            String name = editPlantName.getText().toString();
            String species = editPlantSpecies.getText().toString();

            if (!name.isEmpty() && !species.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("name", name);
                resultIntent.putExtra("species", species);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(AddPlantActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
