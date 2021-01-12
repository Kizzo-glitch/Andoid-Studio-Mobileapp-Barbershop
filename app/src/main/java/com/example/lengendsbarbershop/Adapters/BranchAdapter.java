package com.example.lengendsbarbershop.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lengendsbarbershop.Activities.ServicesListActivity2;
import com.example.lengendsbarbershop.Objects.Branch;
import com.example.lengendsbarbershop.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BranchAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Branch> branchList;

    public BranchAdapter(Activity activity, ArrayList<Branch> branchList) {
        this.activity = activity;
        this.branchList = branchList;
    }

    @Override
    public int getCount() {
        return branchList.size();
    }

    @Override
    public Object getItem(int position) {
        return branchList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null){
            view = LayoutInflater.from(activity).inflate(R.layout.list_item_branch, null);
        }

        final Branch branch = branchList.get(position);
        TextView branName = view.findViewById(R.id.bran_name);
        TextView branAddress = view.findViewById(R.id.bran_address);
        ImageView branLogo = view.findViewById(R.id.branch_logo);

        branName.setText(branch.getName());
        branAddress.setText(branch.getAddress());
        Picasso.get().load(branch.getLogo()).fit().into(branLogo);
        //Picasso.get(activity.getApplicationContext()).load(branch.getLogo()).fit().into(branLogo);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ServicesListActivity2.class);
                intent.putExtra("branchId", branch.getId());
                intent.putExtra("branchName", branch.getName());
                activity.startActivity(intent);
            }
        });
        return view;
    }
}
