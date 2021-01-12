package com.example.lengendsbarbershop.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class StatisticFragmentD extends Fragment {
    private BarChart chart;

    public StatisticFragmentD() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistic_d, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        chart = getActivity().findViewById(R.id.chart1);

        getBarberRevenue();
    }

    private void dummyChart(JSONObject response) {

        JSONObject revenueJSONObject = null;

        try {
            revenueJSONObject = response.getJSONObject("revenue");
            List<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0f, revenueJSONObject.getInt("Mon")));
            entries.add(new BarEntry(1f, revenueJSONObject.getInt("Tue")));
            entries.add(new BarEntry(2f, revenueJSONObject.getInt("Wed")));
            entries.add(new BarEntry(3f, revenueJSONObject.getInt("Thu")));
            entries.add(new BarEntry(4f, revenueJSONObject.getInt("Fri")));
            entries.add(new BarEntry(5f, revenueJSONObject.getInt("Sat")));
            entries.add(new BarEntry(6f, revenueJSONObject.getInt("Sun")));


            BarDataSet set = new BarDataSet(entries, "Revenue by day");
            set.setColor(getResources().getColor(R.color.colorAccent));

            BarData data = new BarData(set);
            data.getBarWidth();
            chart.setFitBars(true);
            chart.invalidate();

            // The labels that should be drawn on the axis
            final String[] days = new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            IAxisValueFormatter formatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return days[(int) value];
                }
            };

            XAxis xAxis = chart.getXAxis();
            xAxis.setGranularity(1F);
            xAxis.setValueFormatter((ValueFormatter) formatter);

            chart.setDescription(null);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);

            YAxis yAxisRight = chart.getAxisRight();
            yAxisRight.setEnabled(false);

            YAxis yAxisLeft = chart.getAxisLeft();
            yAxisLeft.setAxisMinimum((float) 0.0);
            yAxisLeft.setAxisMaximum((float) 100.0);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getBarberRevenue() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);
        String url = getString(R.string.API_URL) + "/barber/revenue/?access_token=" + sharedPreferences.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("GET BARBER REVENUE", response.toString());

                        dummyChart(response);
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