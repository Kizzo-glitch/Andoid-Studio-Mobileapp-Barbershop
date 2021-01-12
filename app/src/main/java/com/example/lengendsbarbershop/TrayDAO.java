package com.example.lengendsbarbershop;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.lengendsbarbershop.Objects.Tray;

import java.util.List;

@Dao
public interface TrayDAO {
    @Query("SELECT * FROM tray")
    List<Tray> getAll();

    @Insert
    void insertAll(Tray... trays);

    @Query("DELETE FROM tray")
    void deleteAll();

    @Query("SELECT * FROM tray WHERE service_id = :serviceId")
    Tray getTray(String serviceId);

    @Query("UPDATE tray SET service_quantity = service_quantity + :serviceQty WHERE id = :trayId")
    void updateTray(int trayId, int serviceQty);
}
