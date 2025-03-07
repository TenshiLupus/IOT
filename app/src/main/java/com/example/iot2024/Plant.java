package com.example.iot2024;

import android.graphics.Bitmap;

public class Plant {
    private String name;
    private String species;
    private String imageUri;

    private String ip;

    private String mac;
    private Bitmap bi;

    //Class that represents a plants with its typical getters and setters for the relevant properties
    public Plant(String name, String imageUri, String ip, String mac, Bitmap bi) {
        this.name = name;
        this.imageUri = imageUri;
        this.ip = ip;
        this.mac = mac;
        this.bi = bi;
    }

    public String getName() {
        return name;
    }


    public String getImageUri() {
        return imageUri;
    }

    public String getIp() {return ip;}

    public String getMac() {return mac;}

    public Bitmap getBi() {return bi;}

    public void setName(String value) {this.name = value;}

    public void setImageUri(String value) {
        this.imageUri = value;
    }

    public void setImageBitmap(Bitmap value) {this.bi = value;}
}

