package com.example.lengendsbarbershop.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.Adapters.BookingAdapter;
import com.example.lengendsbarbershop.Objects.Booking;
import com.example.lengendsbarbershop.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class BookingFragmentD extends Fragment {

    private BookingAdapter adapter;
    private ArrayList<Booking> bookingList;


    public BookingFragmentD() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booking_d, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bookingList = new ArrayList<Booking>();
        adapter = new BookingAdapter(this.getActivity(), bookingList);

        ListView bookingListView = (ListView) getActivity().findViewById(R.id.booking_list_d);
        bookingListView.setAdapter(adapter);

        // Get list of ready bookings to be delivered
        getReadyBookings();
    }

    private void getReadyBookings() {

        String url = getString(R.string.API_URL) + "/barber/bookings/ready/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("READY BOOKINGS LIST", response.toString());

                        // Get bookings in JSONArray type
                        try {
                            JSONArray bookingsJSONArray = response.getJSONArray("bookings");
                            for (int i = 0; i < bookingsJSONArray.length(); i++) {
                                JSONObject bookingObject = bookingsJSONArray.getJSONObject(i);

                                Booking booking = new Booking(
                                        bookingObject.getString("id"),
                                        bookingObject.getJSONObject("branch").getString("name"),
                                        bookingObject.getJSONObject("customer").getString("name"),
                                        bookingObject.getString("address"),
                                        bookingObject.getJSONObject("customer").getString("avatar")
                                );
                                bookingList.add(booking);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Update the ListView with fresh data
                        adapter.notifyDataSetChanged();
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
}