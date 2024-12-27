package com.example.iot2024;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DetailFragment extends BottomSheetDialogFragment {
    private TextView description;
    private TextView soil;
    private TextView light;
    private TextView watering;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.details_fragment,container, false);
        Bundle args = getArguments();
        if (args != null) {
            String description = args.getString("description");
            String soil = args.getString("soil");
            String light = args.getString("light");
            String watering = args.getString("watering");

            // Update your TextViews
            TextView descriptionView = view.findViewById(R.id.description);
            TextView soilView = view.findViewById(R.id.soil);
            TextView lightView = view.findViewById(R.id.light);
            TextView wateringView = view.findViewById(R.id.watering);

            if (descriptionView != null) descriptionView.setText(description);
            if (soilView != null) soilView.setText(soil);
            if (lightView != null) lightView.setText(light);
            if (wateringView != null) wateringView.setText(watering);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    public void updateText(String textRow, String newText) {
        switch (textRow){
            case "description":
                description.setText(newText);
                break;
            case "soil":
                soil.setText(newText);
                break;
            case "light":
                light.setText(newText);
                break;
            case "watering":
                watering.setText(newText);
                break;
        }
    }


}
