package com.example.lengendsbarbershop.Objects;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Tray {
    @PrimaryKey (autoGenerate = true)
    public int id;

    @ColumnInfo(name = "service_id")
    public String serviceId;

    @ColumnInfo(name = "service_name")
    public String serviceName;

    @ColumnInfo(name = "service_price")
    public float servicePrice;

    @ColumnInfo(name = "service_quantity")
    public int serviceQuantity;

    @ColumnInfo(name = "branch_id")
    public String branchId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public float getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(float servicePrice) {
        this.servicePrice = servicePrice;
    }

    public int getServiceQuantity() {
        return serviceQuantity;
    }

    public void setServiceQuantity(int serviceQuantity) {
        this.serviceQuantity = serviceQuantity;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }


}
