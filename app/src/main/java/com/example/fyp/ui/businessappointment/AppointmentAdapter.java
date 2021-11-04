package com.example.fyp.ui.businessappointment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppointmentAdapter extends BaseAdapter implements Filterable {
    private final Activity activity;
    private final ArrayList<Appointment> appointmentArrayList;
    private final ArrayList<Appointment> filterArrayList = new ArrayList<>();

    public AppointmentAdapter(Activity activity, ArrayList<Appointment> appointmentArrayList) {
        this.activity = activity;
        this.appointmentArrayList = appointmentArrayList;
        filterArrayList.addAll(appointmentArrayList);
    }

    @Override
    public int getCount() {
        return filterArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return filterArrayList.get(i);
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
            view = inflater.inflate(R.layout.item_appointment, null);
        }

        ImageView ivStatus;
        TextView tvStaff, tvServiceName, tvRemark, tvAddress, tvPrice, tvDatetime;
        Button btnSelectStaff;

        ivStatus = view.findViewById(R.id.iv_status);
        tvStaff = view.findViewById(R.id.tv_staff);
        tvServiceName = view.findViewById(R.id.tv_serviceName);
        tvRemark = view.findViewById(R.id.tv_remark);
        tvAddress = view.findViewById(R.id.tv_address);
        tvPrice = view.findViewById(R.id.tv_price);
        tvDatetime = view.findViewById(R.id.tv_datetime);
        btnSelectStaff = view.findViewById(R.id.btn_selectStaff);

        switch (StartActivity.user.getIdentity()) {
            case 1:
            case 2:
                btnSelectStaff.setVisibility(View.GONE);
                break;
            case 3:
                btnSelectStaff.setVisibility(View.VISIBLE);
                break;
        }

        Appointment appointment = filterArrayList.get(i);
        switch (appointment.getStatus()) {
            case 0:
                ivStatus.setImageResource(R.drawable.ic_cancel);
                break;
            case 1:
                ivStatus.setImageResource(R.drawable.ic_new);
                break;
            case 2:
                ivStatus.setImageResource(R.drawable.ic_bluetime);
                break;
            case 3:
                ivStatus.setImageResource(R.drawable.ic_running);
                btnSelectStaff.setVisibility(View.GONE);
                break;
            case 4:
                ivStatus.setImageResource(R.drawable.ic_greentick);
                btnSelectStaff.setVisibility(View.GONE);
                break;
        }
        if (appointment.getStaffName().isEmpty()) {
            tvStaff.setText("Staff: None");
        } else {
            tvStaff.setText("Staff: " + appointment.getStaffName());
            btnSelectStaff.setText("Change staff");
        }
        tvServiceName.setText(appointment.getServiceName());
        tvRemark.setText("Remark: " + appointment.getServiceRemark());
        tvAddress.setText("\n" + appointment.getAddress());
        tvPrice.setText(appointment.getServicePrice());
        tvDatetime.setText(appointment.getServiceDateTime());

        View finalView = view;
        btnSelectStaff.setOnClickListener(view1 -> {
            Log.e("name", appointment.getServiceName());
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Select staff")
                    .setPositiveButton("Confirm", null)
                    .setNegativeButton("Cancel", (dialogInterface, i1) -> dialogInterface.dismiss());
            AlertDialog alertDialog;
            View view2 = activity.getLayoutInflater().inflate(R.layout.popup_selectstaff, null);
            builder.setView(view2);
            alertDialog = builder.create();
            alertDialog.show();

            RadioGroup radioGroup = view2.findViewById(R.id.rg_selectStaff);
            Button btnConfirm = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            btnConfirm.setEnabled(false);

            radioGroup.setOnCheckedChangeListener((radioGroup1, i12) -> {
                btnConfirm.setEnabled(true);

                btnConfirm.setOnClickListener(view3 -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy \n hh:mm aa", Locale.getDefault());
                    try {
                        Date date = simpleDateFormat.parse(appointment.getServiceDateTime());
                        Calendar nowCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
                        Calendar appointmentCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
                        if (date != null) {
                            appointmentCalendar.setTime(date);
                            if (appointmentCalendar.getTimeInMillis() - nowCalendar.getTimeInMillis() <= 3.6e+6) {
                                Toast.makeText(activity, "Change staff is only available before 1 hour of appointment",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                RadioButton radioButton = radioGroup1.findViewById(i12);
                                AssignTask assignTask = new AssignTask(radioButton, appointment.getId(), finalView);
                                assignTask.execute();
                                alertDialog.dismiss();
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });
            });

            GetStaff getStaff = new GetStaff(StartActivity.user.getUsername(), view2);
            getStaff.execute();
        });

        return view;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Appointment> appointments = new ArrayList<>();

            if (!charSequence.toString().isEmpty()) {
                for (int i = 0; i < appointmentArrayList.size(); i++) {
                    if (appointmentArrayList.get(i).getServiceDateTime().contains(charSequence.toString())) {
                        appointments.add(appointmentArrayList.get(i));
                    }
                }
            } else {
                appointments.addAll(appointmentArrayList);
            }

            FilterResults filterResults = new FilterResults();
            filterResults.count = appointments.size();
            filterResults.values = appointments;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filterArrayList.clear();
            filterArrayList.addAll((ArrayList<Appointment>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    @SuppressLint("StaticFieldLeak")
    private class GetStaff extends AsyncTask {
        private final String boss;
        private final RadioGroup rgSelectStaff;
        private final ProgressBar pbLoading;
        private String sresponse="";
        private boolean isConnectionFail;

        private GetStaff(String boss, View view) {
            this.boss = boss;
            rgSelectStaff = view.findViewById(R.id.rg_selectStaff);
            pbLoading = view.findViewById(R.id.pb_loading);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            rgSelectStaff.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getstafflistAPI.php?boss=" + boss)
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
            rgSelectStaff.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(activity, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(activity, "Please invite staff before perform this.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        RadioButton radioButton = new RadioButton(activity);
                        radioButton.setText(jsonObject.getString("staffName"));
                        radioButton.setTag(jsonObject.getString("staff"));
                        rgSelectStaff.addView(radioButton);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AssignTask extends AsyncTask {
        private final RadioButton radioButton;
        private final View view;
        private final String staff, staffName;
        private final int id;
        private String sresponse="";
        private boolean isConnectionFail;

        public AssignTask(RadioButton radioButton, int id, View view) {
            this.radioButton = radioButton;
            this.id = id;
            this.view = view;
            staff = radioButton.getTag().toString();
            staffName = radioButton.getText().toString();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("staff", staff)
                    .addFormDataPart("staffName", staffName)
                    .addFormDataPart("id", String.valueOf(id))
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "assigntaskAPI.php")
                    .method("POST", body)
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(activity, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(activity, "Error, please try again later.", Toast.LENGTH_SHORT).show();
            } else {
                ImageView ivStatus;
                TextView tvStaff;
                Button btnSelectStaff;

                ivStatus = view.findViewById(R.id.iv_status);
                tvStaff = view.findViewById(R.id.tv_staff);
                btnSelectStaff = view.findViewById(R.id.btn_selectStaff);

                ivStatus.setImageResource(R.drawable.ic_bluetime);
                tvStaff.setText("Staff: " + radioButton.getText().toString());
                btnSelectStaff.setText("Change staff");
            }

        }
    }
}
