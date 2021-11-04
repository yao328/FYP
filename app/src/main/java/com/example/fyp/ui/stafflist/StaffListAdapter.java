package com.example.fyp.ui.stafflist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StaffListAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<Staff> staffArrayList;

    public StaffListAdapter(Activity activity, ArrayList<Staff> staffArrayList) {
        this.activity = activity;
        this.staffArrayList = staffArrayList;
    }

    @Override
    public int getCount() {
        return staffArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return staffArrayList.get(i);
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
            view = inflater.inflate(R.layout.item_stafflist, null);
        }

        ImageView ivStaffPic = view.findViewById(R.id.iv_staffPic);
        TextView tvStaffName = view.findViewById(R.id.tv_staffName);
        ImageButton btnDeleteStaff = view.findViewById(R.id.btn_deleteStaff);
        btnDeleteStaff.setFocusable(false);
        btnDeleteStaff.setFocusableInTouchMode(false);

        Staff staff = staffArrayList.get(i);

        Glide.with(activity)
                .load(Connection.getUrl() + staff.getProfilePic())
                .thumbnail(Glide.with(activity).load(R.drawable.loading))
                .into(ivStaffPic);
        tvStaffName.setText(staff.getFullName());

        btnDeleteStaff.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Delete staff")
                    .setIcon(R.drawable.warning)
                    .setMessage("Are you sure to remove this staff from your company?")
                    .setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.dismiss())
                    .setPositiveButton("Yes", (dialogInterface, i1) -> {
                        DeleteStaff deleteStaff = new DeleteStaff(StartActivity.user.getUsername(), staff.getUsername(), i);
                        deleteStaff.execute();
                    })
                    .show();
        });

        return view;
    }

    private class DeleteStaff extends AsyncTask {
        private final String boss, staff;
        private final int position;
        private String sresponse="";
        private boolean isConnectionFail;

        public DeleteStaff(String boss, String staff, int position) {
            this.boss = boss;
            this.staff = staff;
            this.position = position;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("boss", boss)
                    .addFormDataPart("staff", staff)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "deletestaffAPI.php")
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

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(activity, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(activity, "Error, please try again later.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Deleted staff.", Toast.LENGTH_SHORT).show();
                staffArrayList.remove(position);
                notifyDataSetChanged();
            }
        }
    }
}
