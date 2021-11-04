package com.example.fyp.ui.businessreport;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AllFragment extends Fragment {
    private TextView tvTotalPrice, tvtotalAppointment;
    private ListView lvTotalReport;
    private ProgressBar pbLoading;
    private SwipeRefreshLayout srlReport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allreport, container, false);
        tvTotalPrice = view.findViewById(R.id.tv_totalPrice);
        tvtotalAppointment = view.findViewById(R.id.tv_totalAppointment);
        lvTotalReport = view.findViewById(R.id.lv_totalReport);
        pbLoading = view.findViewById(R.id.pb_loading);
        srlReport = view.findViewById(R.id.srl_report);

        GetReport getReport = new GetReport(StartActivity.user.getUsername());
        getReport.execute();

        srlReport.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetReport getReport = new GetReport(StartActivity.user.getUsername());
                getReport.execute();
                srlReport.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        GetReport getReport = new GetReport(StartActivity.user.getUsername());
        getReport.execute();
    }

    private class GetReport extends AsyncTask {
        private final String username;
        private String sresponse = "", sresponse2 = "";
        private boolean isConnectionFail;

        private GetReport(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            lvTotalReport.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "bossgetserviceAPI.php?username=" + username)
                    .method("GET", null)
                    .build();
            Request request2 = new Request.Builder()
                    .url(Connection.getUrl() + "gettotalreportAPI.php?boss=" + username)
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
                Response response2 = client.newCall(request2).execute();
                sresponse2 = Objects.requireNonNull(response2.body()).string();
                Log.e("businessreport1", sresponse);
                Log.e("businessreport2", sresponse2);
            } catch (IOException e) {
                e.printStackTrace();
                isConnectionFail = true;
            }
            return null;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            pbLoading.setVisibility(View.GONE);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(requireContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (!sresponse.equals("Error")) {
                ArrayList<Service> serviceArrayList = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    Log.e("sresponse", sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Service service = new Service();
                        service.setServiceName(jsonObject.getString("serviceName"));
                        service.setServiceCount(0);
                        serviceArrayList.add(service);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = new JSONArray(sresponse2);
                    float totalPrice = 0;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        for (int j = 0; j < serviceArrayList.size(); j++) {
                            if (jsonObject.getString("serviceName").equals(serviceArrayList.get(j).getServiceName())) {
                                serviceArrayList.get(j).setServiceCount(serviceArrayList.get(j).getServiceCount() + 1);
                            }
                        }
                        totalPrice += jsonObject.getDouble("servicePrice");
                    }
                    tvTotalPrice.setText(String.format("%.2f", totalPrice));
                    tvtotalAppointment.setText(String.valueOf(jsonArray.length()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ReportAdapter reportAdapter = new ReportAdapter(requireActivity(), serviceArrayList);
                lvTotalReport.setAdapter(reportAdapter);
            }
        }
    }
}