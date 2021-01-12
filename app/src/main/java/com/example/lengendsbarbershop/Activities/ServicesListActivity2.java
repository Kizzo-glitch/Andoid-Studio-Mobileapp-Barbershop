package com.example.lengendsbarbershop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.Adapters.ServiceAdapter;
import com.example.lengendsbarbershop.Objects.Serve;
import com.example.lengendsbarbershop.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ServicesListActivity2 extends AppCompatActivity {
    private ArrayList<Serve> serviceArrayList;
    private ServiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_list2);

        Intent intent = getIntent();
        String branchId = intent.getStringExtra("branchId");
        String branchName = intent.getStringExtra("branchName");

        getSupportActionBar().setTitle(branchName);

        serviceArrayList = new ArrayList<Serve>();
        adapter = new ServiceAdapter(this, serviceArrayList, branchId);

        ListView listView = (ListView) findViewById(R.id.service_list);
        listView.setAdapter(adapter);
        /*listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                return LayoutInflater.from(ServicesListActivity2.this).inflate(R.layout.list_item_service, null);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ServicesListActivity2.this, ServiceDetailActivity2.class);
                startActivity(intent);
            }
        });*/

        // Get Services List
        getServices(branchId);
    }

    private void getServices(String branchId) {
        String url = getString(R.string.API_URL) + "/customer/services/" + branchId + "/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SERVICES LIST", response.toString());

                        // Convert JSON data to JSON Array
                        JSONArray servicesJSONArray = null;

                        try {
                            servicesJSONArray = response.getJSONArray("services");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Convert Json Array to Services Array
                        Gson gson = new Gson();
                        Serve[] services = gson.fromJson(servicesJSONArray.toString(), Serve[].class);

                        // Refresh ListView with up-to-date data
                        serviceArrayList.clear();
                        serviceArrayList.addAll(new ArrayList<Serve>(Arrays.asList(services)));
                        adapter.notifyDataSetChanged();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);

    }
}