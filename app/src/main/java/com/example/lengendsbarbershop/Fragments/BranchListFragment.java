package com.example.lengendsbarbershop.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lengendsbarbershop.Objects.Branch;
import com.example.lengendsbarbershop.Adapters.BranchAdapter;
import com.example.lengendsbarbershop.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;


public class BranchListFragment extends Fragment {

    private ArrayList<Branch> branchArrayList;
    private BranchAdapter adapter;
    private Branch[] branches = new Branch[]{};

    public BranchListFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_branch_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        branchArrayList = new ArrayList<Branch>();
        adapter = new BranchAdapter(this.getActivity(), branchArrayList);

        ListView branchListView = (ListView) getActivity().findViewById(R.id.branch_list);
        branchListView.setAdapter(adapter);

        // Get list of branches
        getBranch();
        addSearchFunction();
    }

    private void getBranch(){
        String url = getString(R.string.API_URL) + "/customer/branches/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("BRANCHES LIST", response.toString());

                        // Convert JSON data to JSON Array
                        JSONArray branchesJSONArray = null;

                        try {
                            branchesJSONArray = response.getJSONArray("branches");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Convert Json Array to Branches Array
                        Gson gson = new Gson();
                        branches = gson.fromJson(branchesJSONArray.toString(), Branch[].class);

                        // Refresh ListView with up-to-date data
                        branchArrayList.clear();
                        branchArrayList.addAll(new ArrayList<Branch>(Arrays.asList(branches)));
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

    private void addSearchFunction(){
        final EditText searchInput = getActivity().findViewById(R.id.bran_search);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("SEARCH", s.toString());
                //`update the Branch List
                branchArrayList.clear();
                for (Branch branch : branches) {
                    if (branch.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                        branchArrayList.add(branch);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}