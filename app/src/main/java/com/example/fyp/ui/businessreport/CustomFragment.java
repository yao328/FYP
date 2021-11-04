package com.example.fyp.ui.businessreport;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomFragment extends Fragment {
    private TextView tvTotalPrice, tvtotalAppointment, tvFromdate, tvTodate;
    private ListView lvTotalReport;
    private ProgressBar pbLoading;
    private ImageButton btnSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom, container, false);
        tvFromdate = view.findViewById(R.id.tv_fromdate);
        tvTodate = view.findViewById(R.id.tv_todate);
        btnSearch = view.findViewById(R.id.btn_search);
        tvTotalPrice = view.findViewById(R.id.tv_totalPrice);
        tvtotalAppointment = view.findViewById(R.id.tv_totalAppointment);
        lvTotalReport = view.findViewById(R.id.lv_totalReport);
        pbLoading = view.findViewById(R.id.pb_loading);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");

        tvFromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext());
                datePickerDialog.show();
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar calendar = Calendar.getInstance(timeZone);
                        calendar.set(i, i1, i2);
                        tvFromdate.setText("From: " + simpleDateFormat.format(calendar.getTime()));
                    }
                });
            }
        });

        tvTodate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext());
                datePickerDialog.show();
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar calendar = Calendar.getInstance(timeZone);
                        calendar.set(i, i1, i2);
                        tvTodate.setText("To: " + simpleDateFormat.format(calendar.getTime()));
                    }
                });
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetReport getReport = new GetReport(StartActivity.user.getUsername(),
                        tvFromdate.getText().toString().replace("From: ", ""),
                        tvTodate.getText().toString().replace("To: ", ""));
                getReport.execute();
            }
        });


        return view;
    }

    private class GetReport extends AsyncTask {
        private final String username, fromdate, todate;
        private String sresponse = "", sresponse2 = "";
        private boolean isConnectionFail;

        public GetReport(String username, String fromdate, String todate) {
            this.username = username;
            this.fromdate = fromdate;
            this.todate = todate;
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
                    .url(Connection.getUrl() + "getmonthreportAPI.php?boss=" + username + "&fromdate=" + fromdate + " 00:00:00&todate="
                            + todate + " 23:59:59")
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
                Response response2 = client.newCall(request2).execute();
                sresponse2 = Objects.requireNonNull(response2.body()).string();
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
            Log.e("url", Connection.getUrl() + "getmonthreportAPI.php?boss=" + username + "&fromdate=" + fromdate + "&todate=" + todate);
            if (isConnectionFail || sresponse.equals("Fail") || sresponse2.equals("Fail")) {
                Toast.makeText(requireContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse2.equals("Error")) {
                Toast.makeText(requireContext(), "No result found.", Toast.LENGTH_SHORT).show();
            } else {
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