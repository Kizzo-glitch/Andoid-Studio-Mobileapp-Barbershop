package com.example.lengendsbarbershop.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lengendsbarbershop.Activities.ServiceDetailActivity2;
import com.example.lengendsbarbershop.Objects.Serve;
import com.example.lengendsbarbershop.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ServiceAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Serve> serviceList;
    private String branchId;

    public ServiceAdapter(Activity activity, ArrayList<Serve> serviceList, String branchId) {
        this.activity = activity;
        this.serviceList = serviceList;
        this.branchId = branchId;
    }

    @Override
    public int getCount() {
        return serviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return serviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.list_item_service, null);
        }
        final Serve service = serviceList.get(position);

        TextView serviceName = (TextView) convertView.findViewById(R.id.service_name);
        TextView serviceDesc = (TextView) convertView.findViewById(R.id.service_description);
        TextView servicePrice = (TextView) convertView.findViewById(R.id.service_price);
        ImageView serviceImage = (ImageView) convertView.findViewById(R.id.service_image);

        serviceName.setText(service.getName());
        serviceDesc.setText(service.getShort_description());
        servicePrice.setText("R" + service.getPrice());
        Picasso.get().load(service.getImage()).fit().into(serviceImage);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ServiceDetailActivity2.class);
                intent.putExtra("branchId", branchId);
                intent.putExtra("serviceId", service.getId());
                intent.putExtra("serviceName", service.getName());
                intent.putExtra("serviceDescription", service.getShort_description());
                intent.putExtra("servicePrice", service.getPrice());
                intent.putExtra("serviceImage", service.getImage());
                activity.startActivity(intent);
            }
        });
        return convertView;
    }
}
