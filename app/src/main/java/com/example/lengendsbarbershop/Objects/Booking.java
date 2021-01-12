package com.example.lengendsbarbershop.Objects;

public class Booking {
    private String id;
    private String branchName;
    private String customerName;
    private String customerAddress;
    private String customerImage;

    public Booking(String id, String branchName, String customerName, String customerAddress, String customerImage) {
        this.id = id;
        this.branchName = branchName;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerImage = customerImage;
    }

    public String getId() {
        return id;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerImage() {
        return customerImage;
    }

}
