package com.example.lengendsbarbershop.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lengendsbarbershop.AppDatabase;
import com.example.lengendsbarbershop.Objects.Tray;
import com.example.lengendsbarbershop.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ServiceDetailActivity2 extends AppCompatActivity {
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail2);

        final Intent intent = getIntent();
        final String branchId = intent.getStringExtra("branchId");
        final String serviceId = intent.getStringExtra("serviceId");
        final String serviceName = intent.getStringExtra("serviceName");
        String serviceDescription = intent.getStringExtra("serviceDescription");
        final Float servicePrice = intent.getFloatExtra("servicePrice", 0);
        String serviceImage = intent.getStringExtra("serviceImage");

        getSupportActionBar().setTitle(serviceName);

        TextView name = (TextView) findViewById(R.id.service_name);
        TextView desc = (TextView) findViewById(R.id.service_description);
        final TextView price = (TextView) findViewById(R.id.service_price);
        ImageView image = (ImageView) findViewById(R.id.service_image);

        name.setText(serviceName);
        desc.setText(serviceDescription);
        price.setText("R" + servicePrice);
        Picasso.get().load(serviceImage).fit().into(image);

        // Declare buttons
        final TextView labelQuantity = (TextView) findViewById(R.id.label_quantity);
        Button buttonIncrease = (Button) findViewById(R.id.button_increase);
        Button buttonDecrease = (Button) findViewById(R.id.button_decrease);
        Button buttonTray = (Button) findViewById(R.id.button_add_tray);

        // Handle Button Increase
        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());

                qty = qty + 1;
                labelQuantity.setText(qty + "");
                price.setText("R" + (qty * servicePrice));

            }
        });

        // Handle Button Decrease
        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());
                if (qty > 1) {
                    qty = qty + 1;
                    labelQuantity.setText(qty + "");
                    price.setText("R" + (qty * servicePrice));
                }
            }
        });

        // Initialize DB
        db = AppDatabase.getAppDatabase(this);

        // Handle Button Add To Tray
        buttonTray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(labelQuantity.getText().toString());
                validateTray(serviceId, serviceName, servicePrice, qty, branchId);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.service_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("StaticFieldLeak")
    private void insertTray(final String serviceId, final String serviceName, final float servicePrice, final int serviceQty, final String branchId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Tray tray = new Tray();
                tray.setServiceId(serviceId);
                tray.setServiceName(serviceName);
                tray.setServicePrice(servicePrice);
                tray.setServiceQuantity(serviceQty);
                tray.setBranchId(branchId);

                db.trayDao().insertAll(tray);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "SERVICE ADDED", Toast.LENGTH_SHORT).show();;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (R.id.button_add_tray == id) {
            Intent intent = new Intent(getApplicationContext(), CustomerMainActivity2.class);
            intent.putExtra("screen", "tray");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteTray() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.trayDao().deleteAll();
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void updateTray(final int trayId, final int serviceQty) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.trayDao().updateTray(trayId, serviceQty);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "CART UPDATED", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void validateTray(final String serviceId, final String serviceName, final float servicePrice, final int serviceQuantity, final String branchId) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                List<Tray> allTray = db.trayDao().getAll();

                if (allTray.isEmpty() || allTray.get(0).getBranchId().equals(branchId)) {
                    Tray tray = db.trayDao().getTray(serviceId);

                    if (tray == null) {
                        //Service doesn't exist
                        return "NOT_EXIST";
                    } else {
                        // Service exist in current tray
                        return tray.getId() + "";
                    }
                } else {
                    // Book service from another branch
                    return "DIFFERENT_BRANCH";
                }
            }

            @Override
            protected void onPostExecute(final String result) {
                super.onPostExecute(result);

                if (result.equals("DIFFERENT_BRANCH")) {
                    // Show alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(ServiceDetailActivity2.this);
                    builder.setTitle("Start New Cart");
                    builder.setMessage("You're booking a service from another branch. Would you like to clear the current cart");
                    builder.setPositiveButton("Cancel", null);
                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteTray();
                            insertTray(serviceId, serviceName, servicePrice, serviceQuantity, branchId);
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                } else if (result.equals("NOT_EXIST")) {
                    insertTray(serviceId, serviceName, servicePrice, serviceQuantity, branchId);
                } else {
                    // Show alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(ServiceDetailActivity2.this);
                    builder.setTitle("Add More");
                    builder.setMessage("Your cart already has a booking for this service. Do you want to add more");
                    builder.setPositiveButton("No", null);
                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateTray(Integer.parseInt(result), serviceQuantity);
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }.execute();
    }
}