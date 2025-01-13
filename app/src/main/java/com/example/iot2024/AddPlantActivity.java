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
        EditText editMacAddress = findViewById(R.id.macAddress);
        EditText editIpAddress = findViewById(R.id.ipAddress);
        Button btnComplete = findViewById(R.id.btnComplete);

        btnComplete.setOnClickListener(v -> {
            String name = editPlantName.getText().toString();
            String mac = editMacAddress.getText().toString();
            String ip = editIpAddress.getText().toString();


            if (!name.isEmpty() && !mac.isEmpty() && !ip.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("name", name);
                resultIntent.putExtra("mac", mac);
                resultIntent.putExtra("ip", ip);
                resultIntent.putExtra("b64image", "");
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(AddPlantActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
