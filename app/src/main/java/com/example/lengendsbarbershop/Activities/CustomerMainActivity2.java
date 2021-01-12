package com.example.lengendsbarbershop.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.example.lengendsbarbershop.Fragments.BookedServiceFragment;
import com.example.lengendsbarbershop.Fragments.BookingFragment;
import com.example.lengendsbarbershop.Fragments.BranchListFragment;
import com.example.lengendsbarbershop.R;
import com.example.lengendsbarbershop.Utils.CircleTransform;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomerMainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main2);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationViewCustomer);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,
                R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        Intent intent = getIntent();
        String screen = intent.getStringExtra("screen");

        if (Objects.equals(screen, "tray")) {
            // load default fragment
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new BookedServiceFragment());
            fragmentTransaction.commit();
        } else if (Objects.equals(screen, "booking")) {
            // load default fragment
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new BookingFragment());
            fragmentTransaction.commit();

        } else {
            // load default fragment
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new BranchListFragment());
            fragmentTransaction.commit();
        }

        // load default fragment
        /*fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_fragment,new BranchListFragment());
        fragmentTransaction.commit();*/

        SharedPreferences sharedPreferences = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);

        View header = navigationView.getHeaderView(0);
        ImageView customer_avatar = (ImageView) header.findViewById(R.id.customer_avatar);
        TextView customer_name = (TextView) header.findViewById(R.id.customer_name);

        customer_name.setText(sharedPreferences.getString("name", ""));
        Picasso.get().load(sharedPreferences.getString("avatar", ""))
                .transform(new CircleTransform())
                .into(customer_avatar);


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_branch) {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new BranchListFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (item.getItemId() == R.id.nav_booked_service){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new BookedServiceFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if (item.getItemId() == R.id.nav_book) {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment,new BookingFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }else if (item.getItemId() == R.id.nav_logout) {
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