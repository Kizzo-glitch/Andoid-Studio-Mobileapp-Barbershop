package com.example.lengendsbarbershop.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.Activities.BarberMainActivity2;
import com.example.lengendsbarbershop.Fragments.DeliveryFragmentD;
import com.example.lengendsbarbershop.Objects.Booking;
import com.example.lengendsbarbershop.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookingAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Booking> bookingList;

    public BookingAdapter(Activity activity, ArrayList<Booking> bookingList) {
        this.activity = activity;
        this.bookingList = bookingList;
    }

    @Override
    public int getCount() {
        return bookingList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.list_item_booking_d, null);
        }

        final Booking booking = bookingList.get(position);

        TextView branchName = (TextView) convertView.findViewById(R.id.branch_name);
        TextView customerName = (TextView) convertView.findViewById(R.id.customer_name);
        TextView customerAddress = (TextView) convertView.findViewById(R.id.customer_address);
        ImageView customerImage = (ImageView) convertView.findViewById(R.id.customer_image);

        branchName.setText(booking.getBranchName());
        customerName.setText(booking.getCustomerName());
        customerAddress.setText(booking.getCustomerAddress());
        Picasso.get().load(booking.getCustomerImage()).fit().into(customerImage);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show an alert
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Pick this Booking");
                builder.setMessage("Would you like to attend this booking?");
                builder.setPositiveButton("Cancel", null);
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity.getApplicationContext(), "BOOKING PICKED", Toast.LENGTH_SHORT).show();

                        pickBooking(booking.getId());
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        return convertView;
    }

    private void pickBooking(final String bookingId) {

        String url = activity.getString(R.string.API_URL) + "/barber/booking/pick/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("BOOKING PICKED", response.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equals("success")) {
                                FragmentTransaction transaction = ((BarberMainActivity2) activity).getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.container_fragment, new DeliveryFragmentD()).commit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                        Toast.makeText(activity, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPreferences = activity.getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
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

        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(postRequest);
    }
}
