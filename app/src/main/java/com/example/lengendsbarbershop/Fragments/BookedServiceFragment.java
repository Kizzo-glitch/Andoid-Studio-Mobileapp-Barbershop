package com.example.lengendsbarbershop.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lengendsbarbershop.Activities.PaymentActivity2;
import com.example.lengendsbarbershop.Adapters.TrayAdapter;
import com.example.lengendsbarbershop.AppDatabase;
import com.example.lengendsbarbershop.Objects.Tray;
import com.example.lengendsbarbershop.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BookedServiceFragment extends Fragment implements OnMapReadyCallback {

    private AppDatabase db;
    private ArrayList<Tray> trayList;
    private TrayAdapter adapter;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private GoogleMap map;
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private EditText address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booked_service, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialise DB
        db = AppDatabase.getAppDatabase(getContext());
        listTray();

        trayList = new ArrayList<>();
        adapter = new TrayAdapter(this.getActivity(), trayList);
        ListView listView = (ListView) getActivity().findViewById(R.id.booked_list);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.tray_map);
        mapFragment.getMapAsync((com.google.android.libraries.maps.OnMapReadyCallback) this);

        //Address EditText
        address = (EditText) getActivity().findViewById(R.id.tray_address);

        // Handle Map Address
        handleMapAddress();

        // Handle add Payment Button Click Event
        handleAddPayment();
    }

    @SuppressLint("StaticFieldLeak")
    private void listTray() {
        new AsyncTask<Void, Void, List<Tray>>() {

            @Override
            protected List<Tray> doInBackground(Void... voids) {
                return db.trayDao().getAll();
            }

            @Override
            protected void onPostExecute(List<Tray> trays) {
                super.onPostExecute(trays);
                if (!trays.isEmpty()) {
                    // Refresh the listview
                    trayList.clear();
                    trayList.addAll(trays);
                    adapter.notifyDataSetChanged();

                    // Calculate the total
                    float total = 0;
                    for (Tray tray: trays) {
                        total += tray.getServiceQuantity() * tray.getServicePrice();
                    }

                    TextView totalView = (TextView) getActivity().findViewById(R.id.tray_total);
                    totalView.setText("R" + total);
                } else {
                    // Display a message
                    TextView alertView = new TextView(getActivity());
                    alertView.setText("You're cart is empty. Please book a service");
                    alertView.setTextSize(17);
                    alertView.setGravity(Gravity.CENTER);
                    alertView.setLayoutParams(
                            new TableLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));

                    LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.tray_layout);
                    linearLayout.removeAllViews();
                    linearLayout.addView(alertView);
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;

                    // Get last known location of the device ans set the position of the map
                    getDeviceLocation();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map = map;

        getLocationPermission();

        // Get last known location of the device ans set the position of the map
        getDeviceLocation();


    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            BookedServiceFragment.this.requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                map.addMarker(new MarkerOptions().position(
                                        new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())
                                ));

                                // Set address field from the position on the map
                                Geocoder coder = new Geocoder(getActivity());
                                try {
                                    ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocation(
                                            lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1
                                    );

                                    if (addresses.isEmpty()) {
                                        address.setText(addresses.get(0).getAddressLine(0));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void handleMapAddress() {
        address.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Geocoder coder = new Geocoder(getActivity());

                    try {
                        ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName(v.getText().toString(), 1);
                        if (!addresses.isEmpty()) {
                            double lat = addresses.get(0).getLatitude();
                            double lng = addresses.get(0).getLongitude();

                            LatLng pos = new LatLng(lat, lng);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, DEFAULT_ZOOM));
                            map.clear();
                            map.addMarker(new MarkerOptions().position(pos));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    private void handleAddPayment() {
        Button buttonAddPayment = (Button) getActivity().findViewById(R.id.button_add_payment);
        buttonAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (address.getText().toString().equals("")) {
                    address.setError("Adress cannot be blank");
                } else {
                    Intent intent = new Intent(getContext(), PaymentActivity2.class);
                    intent.putExtra("branchId", trayList.get(0).getBranchId());
                    intent.putExtra("address", address.getText().toString());

                    ArrayList<HashMap<String, Integer>> bookingDetails = new ArrayList<HashMap<String, Integer>>();
                    for (Tray tray: trayList) {
                        HashMap<String, Integer> map = new HashMap<String, Integer>();
                        map.put("serviceId", Integer.parseInt(tray.getServiceId()));
                        map.put("quantity", tray.serviceQuantity);
                        bookingDetails.add(map);
                    }
                    intent.putExtra("bookingDetails", new Gson().toJson(bookingDetails));
                    startActivity(intent);
                }
            }
        });
    }
}