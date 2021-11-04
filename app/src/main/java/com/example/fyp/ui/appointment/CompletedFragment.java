package com.example.fyp.ui.appointment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.Appointment;
import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CompletedFragment extends Fragment {
    private TextView tvDate;
    private ImageButton btnDatePicker;
    private final TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private ListView lvAllAppointment;
    private AppointmentAdapter appointmentAdapter;
    private ProgressBar pbLoading;
    private SwipeRefreshLayout srlAllAppointment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed, container, false);
        tvDate = view.findViewById(R.id.tv_date);
        btnDatePicker = view.findViewById(R.id.btn_datePicker);
        lvAllAppointment = view.findViewById(R.id.lv_allAppointment);
        pbLoading = view.findViewById(R.id.pb_loading);
        srlAllAppointment = view.findViewById(R.id.srl_allAppointment);

        Calendar today = Calendar.getInstance(timeZone);
        tvDate.setText(simpleDateFormat.format(today.getTime()));

        btnDatePicker.setOnClickListener(view1 -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext());
            datePickerDialog.show();
            datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
                Calendar selectedDate = Calendar.getInstance(timeZone);
                selectedDate.set(i, i1, i2);
                tvDate.setText(simpleDateFormat.format(selectedDate.getTime()));

                @SuppressLint("SimpleDateFormat") SimpleDateFormat coverted =
                        new SimpleDateFormat("dd/MM/yyyy");
                appointmentAdapter.getFilter().filter(coverted.format(selectedDate.getTime()));
            });

            datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view2 -> {
                appointmentAdapter.getFilter().filter("");
                datePickerDialog.dismiss();
            });
        });

        GetAppointment getAppointment = new GetAppointment(StartActivity.user.getUsername());
        getAppointment.execute();

        srlAllAppointment.setOnRefreshListener(() -> {
            GetAppointment getAppointment1 = new GetAppointment(StartActivity.user.getUsername());
            getAppointment1.execute();
            srlAllAppointment.setRefreshing(false);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        GetAppointment getAppointment = new GetAppointment(StartActivity.user.getUsername());
        getAppointment.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAppointment extends AsyncTask {
        private final String username;
        private String sresponse="";
        private boolean isConnectionFail;

        private GetAppointment(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            btnDatePicker.setEnabled(false);
            lvAllAppointment.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "usergetcompletedappointmentAPI.php?user=" + username)
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
                isConnectionFail = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            pbLoading.setVisibility(View.GONE);
            btnDatePicker.setEnabled(true);
            lvAllAppointment.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(requireContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(requireContext(), "Nothing to show.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    ArrayList<Appointment> appointmentArrayList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Appointment appointment = new Appointment();
                        appointment.setId(jsonObject.getInt("id"));
                        appointment.setUser(jsonObject.getString("user"));
                        appointment.setUserName(jsonObject.getString("userName"));
                        appointment.setStaff(jsonObject.getString("staff"));
                        appointment.setStaffName(jsonObject.getString("staffName"));
                        appointment.setShopID(jsonObject.getInt("shopID"));
                        appointment.setAddress(jsonObject.getString("address"));
                        appointment.setLat(jsonObject.getDouble("lat"));
                        appointment.setLon(jsonObject.getDouble("lon"));
                        appointment.setServiceID(jsonObject.getInt("serviceID"));
                        appointment.setServiceName(jsonObject.getString("serviceName"));
                        appointment.setServicePrice(jsonObject.getString("servicePrice"));
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat databaseformat =
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat convertedformat =
                                new SimpleDateFormat("dd/MM/yyyy \n hh:mm aa");
                        Date date = databaseformat.parse(jsonObject.getString("serviceDateTime"));
                        if (date != null) {
                            appointment.setServiceDateTime(convertedformat.format(date));
                        }
                        appointment.setServiceRemark(jsonObject.getString("serviceRemark"));
                        appointment.setStatus(jsonObject.getInt("status"));
                        appointmentArrayList.add(appointment);
                    }
                    appointmentAdapter = new AppointmentAdapter(requireActivity(), appointmentArrayList);
                    lvAllAppointment.setAdapter(appointmentAdapter);
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}