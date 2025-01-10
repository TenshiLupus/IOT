package com.example.iot2024;

public class Plant {
    private String name;
    private String species;
    private String imageUri;

    private String ip;

    private String mac;

    public Plant(String name, String species, String imageUri, String ip, String mac) {
        this.name = name;
        this.species = species;
        this.imageUri = imageUri;
        this.ip = ip;
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getIp() {return ip;}

    public String getMac() {return mac;}
}

