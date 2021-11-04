package com.example.fyp.ui.stafflist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StaffListFragment extends Fragment {
    private SwipeRefreshLayout srlStaffList;
    private ListView lvStaffList;
    private ProgressBar pbLoading;
    private ArrayList<String> staffList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_list, container, false);
        srlStaffList = view.findViewById(R.id.srl_staffList);
        lvStaffList = view.findViewById(R.id.lv_staffList);
        pbLoading = view.findViewById(R.id.pb_loading);
        FloatingActionButton fabInvStaff = view.findViewById(R.id.fab_invStaff);

        GetStaffList getStaffList = new GetStaffList(StartActivity.user.getUsername());
        getStaffList.execute();

        srlStaffList.setOnRefreshListener(() -> {
            GetStaffList getStaffList1 = new GetStaffList(StartActivity.user.getUsername());
            getStaffList1.execute();
            srlStaffList.setRefreshing(false);
        });

        fabInvStaff.setOnClickListener(view1 -> invstaffpopup());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        GetStaffList getStaffList = new GetStaffList(StartActivity.user.getUsername());
        getStaffList.execute();
    }

    private void invstaffpopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Theme_FYP_NoActionBar);
        AlertDialog alertDialog;
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.popup_invstaff, null);
        builder.setView(view).setTitle("Invite staff")
                .setPositiveButton("Invite", null)
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
        alertDialog.getWindow().getAttributes().dimAmount = (float) 0.25;
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.show();

        EditText etSearchStaffUsername = view.findViewById(R.id.et_searchStaffUsername);
        ImageButton btnSearchStaffUsername = view.findViewById(R.id.btn_searchStaffUsername);
        Button btnInv = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        btnInv.setEnabled(false);

        btnSearchStaffUsername.setOnClickListener(view1 -> {
            InputMethodManager inputMethodManager = (InputMethodManager) requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(btnSearchStaffUsername.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            btnInv.setEnabled(false);

            if (!etSearchStaffUsername.getText().toString().isEmpty()) {
                GetStaffData getStaffData = new GetStaffData(etSearchStaffUsername.getText().toString(), view, btnInv, alertDialog);
                getStaffData.execute();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class GetStaffData extends AsyncTask {
        private final String username;
        private final View view;
        private final Button btnInv;
        private final AlertDialog alertDialog;
        private final TextView tvFullName, tvGender, tvAge, tvMobile, tvAddress;
        private final ProgressBar pbLoading;
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetStaffData(String username, View view, Button btnInv, AlertDialog alertDialog) {
            this.username = username;
            this.view = view;
            this.btnInv = btnInv;
            this.alertDialog = alertDialog;
            tvFullName = view.findViewById(R.id.tv_fullName);
            tvGender = view.findViewById(R.id.tv_gender);
            tvAge = view.findViewById(R.id.tv_age);
            tvMobile = view.findViewById(R.id.tv_mobile);
            tvAddress = view.findViewById(R.id.tv_address);
            pbLoading = view.findViewById(R.id.pb_loading);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbLoading.setVisibility(View.VISIBLE);
            tvFullName.setText("");
            tvGender.setText("");
            tvAge.setText("");
            tvMobile.setText("");
            tvAddress.setText("");
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "searchstaffAPI.php?username=" + username)
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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            pbLoading.setVisibility(View.GONE);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(view.getContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(view.getContext(), "Don't have this user.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if (jsonObject.getString("identity").equals("1")) {
                        tvFullName.setText(jsonObject.getString("fullName"));
                        if (jsonObject.getString("gender").equals("1")) {
                            tvGender.setText("Male");
                        } else {
                            tvGender.setText("Female");
                        }
                        tvAge.setText(jsonObject.getString("age"));
                        tvMobile.setText(jsonObject.getString("mobile"));
                        tvAddress.setText(jsonObject.getString("address"));

                        btnInv.setEnabled(true);
                        btnInv.setOnClickListener(view1 -> {
                            InvStaff invStaff = new InvStaff(StartActivity.user.getUsername(), username,
                                    tvFullName.getText().toString(), alertDialog);
                            invStaff.execute();
                        });
                    } else {
                        Toast.makeText(view.getContext(), "This user cannot be invited as a staff", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private class InvStaff extends AsyncTask {
            private final String boss, staff, staffName;
            private final AlertDialog alertDialog;
            private String sresponse = "";
            private boolean isConnectionFail;

            public InvStaff(String boss, String staff, String staffName, AlertDialog alertDialog) {
                this.boss = boss;
                this.staff = staff;
                this.staffName = staffName;
                this.alertDialog = alertDialog;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pbLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(30000, TimeUnit.MILLISECONDS)
                        .build();
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("boss", boss)
                        .addFormDataPart("staff", staff)
                        .addFormDataPart("staffName", staffName)
                        .build();
                Request request = new Request.Builder()
                        .url(Connection.getUrl() + "addstaffAPI.php")
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

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                pbLoading.setVisibility(View.GONE);

                if (isConnectionFail || sresponse.equals("Fail")) {
                    Toast.makeText(view.getContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
                } else if (sresponse.equals("Error")) {
                    Toast.makeText(view.getContext(), "Error, please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(view.getContext(), "Done.", Toast.LENGTH_SHORT).show();
                    GetStaffList getStaffList = new GetStaffList(StartActivity.user.getUsername());
                    getStaffList.execute();
                    alertDialog.dismiss();
                }
            }
        }
    }

    private class GetStaffList extends AsyncTask {
        private final String boss;
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetStaffList(String boss) {
            this.boss = boss;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            lvStaffList.setEnabled(false);
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
            lvStaffList.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(requireContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(requireContext(), "Error, please try again later.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        staffList.add(" OR `username` = '" + jsonObject.getString("staff") + "'");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringBuilder combine = new StringBuilder();
                for (int i = 0; i < staffList.size(); i++) {
                    combine.append(staffList.get(i));
                }

                GetStaff getStaff = new GetStaff(combine.toString().replaceFirst(" OR `username` = ", ""));
                getStaff.execute();
            }
        }

        private class GetStaff extends AsyncTask {
            private final String username;
            private String sresponse="";
            private boolean isConnectionFail;

            public GetStaff(String username) {
                this.username = username;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pbLoading.setVisibility(View.VISIBLE);
                lvStaffList.setEnabled(false);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(30000, TimeUnit.MILLISECONDS)
                        .build();
                Request request = new Request.Builder()
                        .url(Connection.getUrl() + "getstaffitemAPI.php?username=" + username)
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
                lvStaffList.setEnabled(true);

                if (isConnectionFail || sresponse.equals("Fail")) {
                    Toast.makeText(requireContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
                } else if (sresponse.equals("Error")) {
                    Toast.makeText(requireContext(), "Error, please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(sresponse);
                        ArrayList<Staff> staffArrayList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Staff staff = new Staff();
                            staff.setUsername(jsonObject.getString("username"));
                            staff.setFullName(jsonObject.getString("fullName"));
                            staff.setGender(jsonObject.getString("gender").equals("1"));
                            staff.setAge(Integer.parseInt(jsonObject.getString("age")));
                            staff.setAddress(jsonObject.getString("address"));
                            staff.setState(jsonObject.getString("state"));
                            staff.setLat(Double.parseDouble(jsonObject.getString("lat")));
                            staff.setLon(Double.parseDouble(jsonObject.getString("lon")));
                            staff.setMobile(jsonObject.getString("mobile"));
                            staff.setProfilePic(jsonObject.getString("profilePic"));
                            staff.setIdentity(Integer.parseInt(jsonObject.getString("identity")));
                            staffArrayList.add(staff);
                        }
                        StaffListAdapter staffListAdapter = new StaffListAdapter(requireActivity(), staffArrayList);
                        lvStaffList.setAdapter(staffListAdapter);
                        lvStaffList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Staff staff = staffArrayList.get(i);
                                Intent intent = new Intent(requireActivity(), ChatActivity.class);
                                intent.putExtra("staff", staff);
                                intent.putExtra("myusername", StartActivity.user.getUsername());
                                intent.putExtra("targetusername", staff.getUsername());
                                startActivity(intent);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}