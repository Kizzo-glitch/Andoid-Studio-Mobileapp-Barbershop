package com.example.lengendsbarbershop.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.Fragments.BookingFragmentD;
import com.example.lengendsbarbershop.Utils.CircleTransform;
import com.example.lengendsbarbershop.Fragments.DeliveryFragmentD;
import com.example.lengendsbarbershop.R;
import com.example.lengendsbarbershop.Fragments.StatisticFragmentD;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class BarberMainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_main2);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_driver);
        navigationView = findViewById(R.id.navigationViewDriver);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,
                R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        // load default fragment
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment,new BookingFragmentD());
        fragmentTransaction.commit();

        // Get the User's Info

        sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);

        View header = navigationView.getHeaderView(0);
        ImageView customer_avatar = (ImageView) header.findViewById(R.id.customer_avatar);
        TextView customer_name = (TextView) header.findViewById(R.id.customer_name);

        customer_name.setText(sharedPref.getString("name", ""));
        Picasso.get().load(sharedPref.getString("avatar", "")).transform(new CircleTransform()).into(customer_avatar);


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_booking) {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new BookingFragmentD());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (item.getItemId() == R.id.nav_delivery){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new DeliveryFragmentD());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if (item.getItemId() == R.id.nav_statistic) {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new StatisticFragmentD());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if (item.getItemId() == R.id.nav_logout) {
            logoutToServer(sharedPref.getString("token", ""));
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("token");
            editor.apply();

            finishAffinity();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /*@Override
    public void onBackPressed() {

    }*/

    private void logoutToServer(final String token) {

        String url = getString(R.string.API_URL) + "/social/revoke-token";

        StringRequest postRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Execute code
                        Log.d("RESPONSE FROM SERVER", response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", token);
                params.put("client_id", getString(R.string.CLIENT_ID));
                params.put("client_secret", getString(R.string.CLIENT_SECRET));

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }
}