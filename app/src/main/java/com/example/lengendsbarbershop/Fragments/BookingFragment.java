package com.example.lengendsbarbershop.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.Adapters.TrayAdapter;
import com.example.lengendsbarbershop.Objects.Tray;
import com.example.lengendsbarbershop.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class BookingFragment extends Fragment implements OnMapReadyCallback {

    private ArrayList<Tray> trayList;
    private TrayAdapter adapter;
    private Button buttonStatus;

    private GoogleMap mMap;
    private Timer timer = new Timer();
    private Marker barberMarker;
    private static final int DEFAULT_ZOOM = 15;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        trayList = new ArrayList<Tray>();
        adapter = new TrayAdapter(this.getActivity(), trayList);

        ListView listView = (ListView) getActivity().findViewById(R.id.booking_list);
        listView.setAdapter(adapter);

        buttonStatus = (Button) getActivity().findViewById(R.id.status);

        // Get latest Booking
        getLatestBooking();

        // Obtain the SupportMapFragment and get notifies when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.service_map);
        mapFragment.getMapAsync((com.google.android.libraries.maps.OnMapReadyCallback) this);

        // Get the BarberLocation
        getBarberLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getBarberLocation();
            }
        };

        timer.scheduleAtFixedRate(task, 0, 2000);
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

    private void getLatestBooking() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
        String url = getString(R.string.API_URL) + "/customer/booking/latest/?access_token=" + sharedPreferences.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("LATEST BOOKING", response.toString());

                        //Get Booking details in JSONArray
                        JSONArray bookingDetailArray = null;
                        String status = "";

                        try {
                            bookingDetailArray = response.getJSONObject("booking").getJSONArray("booking_details");
                            status = response.getJSONObject("booking").getString("status");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (bookingDetailArray == null || bookingDetailArray.length() == 0) {
                            // Show the message
                            TextView alertView = new TextView(getActivity());
                            alertView.setText("You have no booking");
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

                        // Add thi to the ListView
                        for (int i = 0; i < bookingDetailArray.length(); i++) {
                            Tray tray = new Tray();
                            try {
                                JSONObject bookingDetail = bookingDetailArray.getJSONObject(i);
                                tray.setServiceName(bookingDetail.getJSONObject("service").getString("name"));
                                tray.setServicePrice(bookingDetail.getJSONObject("service").getInt("price"));
                                tray.setServiceQuantity(bookingDetail.getInt("quantity"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            trayList.add(tray);
                        }

                        // Update ListView
                        adapter.notifyDataSetChanged();

                        // Update the Status View
                        buttonStatus.setText(status);

                        // Show Branch and Customer on the map
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

    private void getBarberLocation() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
        String url = getString(R.string.API_URL) + "/customer/barber/location/?access_token=" + sharedPreferences.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("BARBER LOCATION", response.toString());

                        try {
                            String[] location = response.getString("location").split(",");
                            String lat = location[0];
                            String lng = location[1];

                            LatLng barbPos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                            try {
                                barberMarker.remove();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            barberMarker = mMap.addMarker(new MarkerOptions()
                                    .position(barbPos).title("Barber Location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_car)));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}