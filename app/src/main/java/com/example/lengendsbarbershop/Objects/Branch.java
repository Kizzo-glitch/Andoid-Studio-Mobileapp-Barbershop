package com.example.lengendsbarbershop.Objects;

public class Branch {
    private String id, name, address, logo;

    public Branch(String id, String name, String address, String logo) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.logo = logo;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLogo() {
        return logo;
    }
}
