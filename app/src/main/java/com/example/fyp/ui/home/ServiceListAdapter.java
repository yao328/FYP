package com.example.fyp.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.fyp.R;

import java.util.ArrayList;

public class ServiceListAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<Service> serviceArrayList;

    public ServiceListAdapter(Activity activity, ArrayList<Service> serviceArrayList) {
        this.activity = activity;
        this.serviceArrayList = serviceArrayList;
    }

    @Override
    public int getCount() {
        return serviceArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return serviceArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.item_selectservicelist, null);
        }

        TextView tvServiceName, tvServicePrice;
        tvServiceName = view.findViewById(R.id.tv_serviceName);
        tvServicePrice = view.findViewById(R.id.tv_servicePrice);

        Service service = serviceArrayList.get(i);
        tvServiceName.setText(service.getServiceName());
        tvServicePrice.setText("RM " + service.getServicePrice());

        return view;
    }
}
