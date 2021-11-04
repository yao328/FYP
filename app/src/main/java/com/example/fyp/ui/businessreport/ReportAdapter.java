package com.example.fyp.ui.businessreport;

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

public class ReportAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<Service> serviceArrayList;

    public ReportAdapter(Activity activity, ArrayList<Service> serviceArrayList) {
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

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.item_reportservice, null);
        }

        Service service = serviceArrayList.get(i);
        TextView tvServiceName, tvServiceCount;
        tvServiceName = view.findViewById(R.id.tv_serviceName);
        tvServiceCount = view.findViewById(R.id.tv_serviceCount);

        tvServiceName.setText(service.getServiceName());
        tvServiceCount.setText(String.valueOf(service.getServiceCount()));

        return view;
    }
}
