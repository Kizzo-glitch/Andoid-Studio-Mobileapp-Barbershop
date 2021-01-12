package com.example.lengendsbarbershop.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.AppDatabase;
import com.example.lengendsbarbershop.R;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity2 extends AppCompatActivity {

    private String branchId, address, bookingDetails;
    private Button buttonPlaceBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment2);
        getSupportActionBar().setTitle("");

        // Get Booking Data
        Intent intent = getIntent();
        branchId = intent.getStringExtra("branchId");
        address = intent.getStringExtra("address");
        bookingDetails = intent.getStringExtra("bookingDetails");

        final CardInputWidget mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);

        buttonPlaceBooking = (Button) findViewById(R.id.button_place_booking);
        buttonPlaceBooking.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                final Card card = mCardInputWidget.getCard();
                if (card == null) {
                    Toast.makeText(getApplicationContext(), "Card cannot be blank", Toast.LENGTH_LONG).show();
                } else {

                    // Disable the place booking
                    setButtonPlaceBooking("LOADING...", false);
                    /*buttonPlaceBooking.setText("LOADING...");
                    buttonPlaceBooking.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
                    buttonPlaceBooking.setClickable(false);*/

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Stripe stripe = new Stripe(getApplicationContext(), "pk_test_51HozvYDLhbo6BjXRT4AP2k4u87gRk1OX9SSFZaPzAODRTmsxmQiQ3fGkr7ozQQLaOwK3UtQiye5UDb4SBQf871t3008uo6JWfX");
                            stripe.createToken(
                                    card,
                                    new TokenCallback() {
                                        @Override
                                        public void onError(Exception error) {
                                            // Show localized error message
                                            Toast.makeText(getApplicationContext(),
                                                    error.getLocalizedMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            // Enable the place booking
                                            setButtonPlaceBooking("PLACE BOOKING", true);
                                            /*buttonPlaceBooking.setText("PLACE BOOKING");
                                            buttonPlaceBooking.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                                            buttonPlaceBooking.setClickable(true);*/
                                        }

                                        @Override
                                        public void onSuccess(Token token) {
                                            // Make a booking
                                            addBooking(token.getId());

                                        }
                                    }
                            );
                            return null;
                        }
                    }.execute();
                }
            }
        });
    }

    private void addBooking(final String stripeToken) {

        String url = getString(R.string.API_URL) + "/customer/booking/add/";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("BOOKING ADDED", response.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getString("status").equals("success")) {
                                deleteTray();
                                
                                // Jump to the booking screen
                                Intent intent = new Intent(getApplicationContext(), CustomerMainActivity2.class);
                                intent.putExtra("screen", "booking");
                                startActivity(intent);

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        jsonObject.getString("error"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Enable the place booking
                        setButtonPlaceBooking("PLACE BOOKING", true);
                        /*buttonPlaceBooking.setText("PLACE BOOKING");
                        buttonPlaceBooking.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        buttonPlaceBooking.setClickable(true);*/
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        // Enable the place booking
                        setButtonPlaceBooking("PLACE BOOKING", true);
                        /*buttonPlaceBooking.setText("PLACE BOOKING");
                        buttonPlaceBooking.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        buttonPlaceBooking.setClickable(true);*/

                        Toast.makeText(getApplicationContext(),
                                error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                final SharedPreferences sharedPreferences = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
                Map<String, String> params = new HashMap<String, String>();
                params.put("access_token", sharedPreferences.getString("token", ""));
                params.put("branch_id", branchId);
                params.put("address", address);
                params.put("booking_details", bookingDetails);
                params.put("stripe_token", stripeToken);

                return params;
            }
        };

        postRequest.setRetryPolicy(
                new DefaultRetryPolicy(0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    private void setButtonPlaceBooking(String text, boolean isEnable) {
        buttonPlaceBooking.setText(text);
        buttonPlaceBooking.setClickable(isEnable);
        if (isEnable) {
            buttonPlaceBooking.setBackgroundColor(getResources().getColor(R.color.colorGreen));
        } else {
            buttonPlaceBooking.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void deleteTray() {
        final AppDatabase db = AppDatabase.getAppDatabase(this);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.trayDao().deleteAll();
                return null;
            }
        }.execute();
    }
}