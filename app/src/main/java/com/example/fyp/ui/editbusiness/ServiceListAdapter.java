package com.example.fyp.ui.editbusiness;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.SetListViewHeight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServiceListAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<Service> serviceArrayList;
    private final ListView listView;

    public ServiceListAdapter(Activity activity, ArrayList<Service> serviceArrayList, ListView listView) {
        this.activity = activity;
        this.serviceArrayList = serviceArrayList;
        this.listView = listView;
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
            view = inflater.inflate(R.layout.item_servicelist, null);
        }

        TextView etServiceName, etPrice;
        ImageButton btnDelete;

        etServiceName = view.findViewById(R.id.tv_eServiceName);
        etPrice = view.findViewById(R.id.tv_ePrice);
        btnDelete = view.findViewById(R.id.btn_deleteService);

        Service service = serviceArrayList.get(i);
        etServiceName.setText(service.getServiceName());
        etPrice.setText(service.getServicePrice());
        btnDelete.setOnClickListener(view1 -> {
            if (serviceArrayList.size() > 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Delete service");
                builder.setMessage("Are you sure you want to delete this service?");
                builder.setIcon(R.drawable.warning);
                builder.setPositiveButton("Yes", (dialogInterface, j) -> {
                    DeleteService deleteService = new DeleteService(service.getId(), i, serviceArrayList, activity, listView);
                    deleteService.execute();
                });
                builder.setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.dismiss());
                builder.show();
            } else {
                Toast.makeText(activity, "You must keep at least 1 service", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteService extends AsyncTask {
        private final int id, position;
        private final ArrayList<Service> serviceArrayList;
        private final Activity activity;
        private final ListView listView;
        private String sresponse="";
        private boolean isConnectionFail;

        public DeleteService(int id, int position, ArrayList<Service> serviceArrayList, Activity activity, ListView listView) {
            this.id = id;
            this.position = position;
            this.serviceArrayList = serviceArrayList;
            this.activity = activity;
            this.listView = listView;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("id", String.valueOf(id))
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "deleteserviceAPI.php")
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
                Toast.makeText(activity, "Error to delete this service.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("OK")) {
                serviceArrayList.remove(position);
                SetListViewHeight.setListViewHeightBasedOnItems(listView);
                notifyDataSetChanged();
            }
        }
    }
}
