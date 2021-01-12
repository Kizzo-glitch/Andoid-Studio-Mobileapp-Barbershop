package com.example.lengendsbarbershop.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.lengendsbarbershop.Objects.Tray;
import com.example.lengendsbarbershop.R;

import java.util.ArrayList;

public class TrayAdapter  extends BaseAdapter {

    private Activity activity;
    private ArrayList<Tray> trayList;

    public TrayAdapter(Activity activity, ArrayList<Tray> trayList) {
        this.activity = activity;
        this.trayList = trayList;
    }


    @Override
    public int getCount() {
        return trayList.size();
    }

    @Override
    public Object getItem(int position) {
        return trayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.list_item_booked, null);
        }

        TextView serviceName = (TextView) convertView.findViewById(R.id.tray_service_name);
        TextView serviceQuantity = (TextView) convertView.findViewById(R.id.tray_service_quantity);
        TextView serviceSubtotal = (TextView) convertView.findViewById(R.id.tray_service_subtotal);

        Tray tray = trayList.get(position);
        serviceName.setText(tray.getServiceName());
        serviceQuantity.setText(tray.getServiceQuantity() + "");
        serviceSubtotal.setText("R" + (tray.getServicePrice() * tray.getServiceQuantity()));
        return convertView;
    }
}
