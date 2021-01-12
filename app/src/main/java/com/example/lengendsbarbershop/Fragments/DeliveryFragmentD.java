package com.example.lengendsbarbershop.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.R;
import com.example.lengendsbarbershop.Utils.CircleTransform;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.maps.SupportMapFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DeliveryFragmentD extends Fragment implements OnMapReadyCallback {

    private TextView customerName;
    private TextView customerAddress;
    private ImageView customerImage;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private Marker barberMarker;
    private LocationCallback locationCallback;

    private String bookingId;

    public DeliveryFragmentD() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_delivery_d, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        customerName = (TextView) getActivity().findViewById(R.id.customer_name);
        customerAddress = (TextView) getActivity().findViewById(R.id.customer_address);
        customerImage = (ImageView) getActivity().findViewById(R.id.customer_image);

        // Obtain the SupportMapFragment and get notifies when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.delivery_map);
        mapFragment.getMapAsync((com.google.android.libraries.maps.OnMapReadyCallback) this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Get the latest booking details
        getLatestBooking();

        // Handle the Complete Booking Button
        handleButtonCompleteBooking();

    }

    private void getLatestBooking() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
        String url = getString(R.string.API_URL) + "/barber/booking/latest/?access_token=" + sharedPreferences.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("GET LATEST BOOKING", response.toString());

                        // Get booking details in JSONArray type
                        JSONObject latestBookJSONObject = null;

                        bookingId = null;
                        Boolean bookingIsDelivered = null;

                        try {
                            latestBookJSONObject = response.getJSONObject("booking");

                            bookingId = latestBookJSONObject.getString("id");
                            bookingIsDelivered = latestBookJSONObject.getString("status").equals("delivered");

                            customerName.setText(latestBookJSONObject.getJSONObject("customer").getString("name"));
                            customerAddress.setText(latestBookJSONObject.getString("address"));
                            Picasso.get().load(latestBookJSONObject.getJSONObject("customer").getString("avatar"))
                                    .transform(new CircleTransform())
                                    .into(customerImage);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Check if there is no outstanding booking then display the message
                        if (latestBookJSONObject == null || bookingId == null || bookingIsDelivered) {
                            TextView alertText  = new TextView(getActivity());
                            alertText.setText("You have no oustanding bookings");
                            alertText.setTextSize(17);
                            alertText.setId(alertText.generateViewId());

                            ConstraintLayout constraintLayout = (ConstraintLayout) getActivity().findViewById(R.id.delivery_layout);
                            constraintLayout.removeAllViews();
                            constraintLayout.addView(alertText);

                            ConstraintSet set = new ConstraintSet();
                            set.clone(constraintLayout);
                            set.connect(alertText.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM);
                            set.connect(alertText.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP);
                            set.connect(alertText.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT);
                            set.connect(alertText.getId(), ConstraintSet.RIGHT, constraintLayout.getId(), ConstraintSet.RIGHT);
                            set.applyTo(constraintLayout);
                        }

                        // Draw route map between locations
                        drawRouteOnMap(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(jsonObjectRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get the user's permission
        getLocationPermission();

        // Get the device's location and the position on the map
        getDeviceLocation();

        // Listen location update
        startLocationUpdate();
    }

    private void drawRouteOnMap(JSONObject response) {
        try {
            String branchAddress = response.getJSONObject("booking").getJSONObject("branch").getString("address");
            String bookingAddress = response.getJSONObject("booking").getString("address");
            Geocoder coder = new Geocoder(getActivity());
            ArrayList<Address> branAdresses = (ArrayList<Address>) coder.getFromLocationName(branchAddress, 1);
            ArrayList<Address> servAdresses = (ArrayList<Address>) coder.getFromLocationName(bookingAddress, 1);

            if (!branAdresses.isEmpty() && !servAdresses.isEmpty()) {
                LatLng branchPos = new LatLng(branAdresses.get(0).getLatitude(), branAdresses.get(0).getLongitude());
                LatLng servicePos = new LatLng(servAdresses.get(0).getLatitude(), servAdresses.get(0).getLongitude());

                DrawRouteMaps.getInstance(getActivity()).draw(branchPos, servicePos, mMap);
                DrawMarker.getInstance(getActivity()).draw(mMap, branchPos, R.drawable.pin_branch, "Branch Location");
                DrawMarker.getInstance(getActivity()).draw(mMap, servicePos, R.drawable.pin_customer, "Customer Location");

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(branchPos)
                        .include(servicePos).build();
                Point displaySize = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
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
            DeliveryFragmentD.this.requestPermissions(
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
                                LatLng pos = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                barberMarker = mMap.addMarker(new MarkerOptions().position(pos).
                                        title("Barber Location").
                                        icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car))
                                );

                                updateBarberLocation(lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
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

                    // Listen location update
                    startLocationUpdate();
                }
            }
        }
    }

    private void startLocationUpdate() {
        try {
            if (locationPermissionGranted) {
                // STEP 1: Set up a location request
                final LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                // STEP 2: Define the location update callback
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }

                        for (Location location : locationResult.getLocations()) {
                            // Update UI with location data
                            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                            try {
                                barberMarker.remove();
                            } catch (Exception e) {
                            }

                            barberMarker = mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car))
                            );

                            updateBarberLocation(lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());

                            Log.d("NEW DRIVER LOCATION:", Double.toString(pos.latitude) + "," + Double.toString(pos.longitude));
                        }
                    }
                };

                // STEP 3: Request location updates
                mFusedLocationProviderClient.requestLocationUpdates(
                        locationRequest, locationCallback, null
                );
            }
        } catch (SecurityException e) {
            Log.d("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location update
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void updateBarberLocation(final String location) {

        String url = getString(R.string.API_URL) + "/barber/location/update/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("UPDATE BARBER LOCATION", response);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        // Enable the place booking
                        Log.d("ERROR MESSAGE", error.toString());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", sharedPreferences.getString("token", ""));
                params.put("location", location);

                return params;
            }
        };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(postRequest);
    }

    private void handleButtonCompleteBooking() {
        Button buttonCompleteBooking = (Button) getActivity().findViewById(R.id.button_complete);
        buttonCompleteBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show an alert
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Complete Booking");
                builder.setMessage("Is this Booking completed?");
                builder.setPositiveButton("Cancel", null);
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(), "BOOKING COMPLETED", Toast.LENGTH_SHORT).show();

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void completeBooking(final String bookingId) {
        String url = getString(R.string.API_URL) + "/barber/booking/complete/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("UPDATE BARBER LOCATION", response);

                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container_fragment, new BranchListFragment()).commit();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        // Enable the place booking
                        Log.d("ERROR MESSAGE", error.toString());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", sharedPreferences.getString("token", ""));
                params.put("booking_id", bookingId);

                return params;
            }
        };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(postRequest);
    }
}