package com.example.iot2024;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plants;
    private OnViewMoreClickListener listener;
    private Context context;

    private static final int REQUEST_IMAGE_CAPTURE = 7;


    public interface OnViewMoreClickListener {
        void onViewMore(Plant plant);
    }

    public PlantAdapter(List<Plant> plants, OnViewMoreClickListener listener) {
        this.plants = plants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_item, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plants.get(position);

        holder.txtPlantName.setText(plant.getName());

        if (plant.getImageUri() != null) {
            holder.imgPlant.setImageURI(Uri.parse(plant.getImageUri()));
        }

        holder.imgPlant.setImageBitmap(plant.getBi());

        holder.btnViewMore.setOnClickListener(v -> listener.onViewMore(plant));
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlant;
        TextView txtPlantName;
        Button btnViewMore;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlant = itemView.findViewById(R.id.imgPlant);
            txtPlantName = itemView.findViewById(R.id.txtPlantName);
            btnViewMore = itemView.findViewById(R.id.btnViewMore);
        }
    }
}
