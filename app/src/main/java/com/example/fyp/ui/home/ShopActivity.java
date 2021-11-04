package com.example.fyp.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShopActivity extends AppCompatActivity {
    private ImageButton btnBack, btnFilter;
    private SearchView svFilterName;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView serviceList;
    private ProgressBar pbLoading;
    private final RadiobuttonDatabase radiobuttonDatabase = new RadiobuttonDatabase(ShopActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        btnBack = findViewById(R.id.btn_back);
        btnFilter = findViewById(R.id.btn_filter);
        svFilterName = findViewById(R.id.sv_filterName);
        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        serviceList = findViewById(R.id.serviceList);
        pbLoading = findViewById(R.id.pb_loading);

        radiobuttonDatabase.deleteDatabase();
        btnBack.setOnClickListener(view -> finish());

        Bundle bundle = getIntent().getExtras();
        GetServiceList getServiceList = new GetServiceList(bundle.getString("type"), StartActivity.user.getState());
        getServiceList.execute();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            svFilterName.clearFocus();
            GetServiceList getServiceList1 = new GetServiceList(bundle.getString("type"), StartActivity.user.getState());
            getServiceList1.execute();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetServiceList extends AsyncTask {
        private final String type, state;
        private final ArrayList<Shop> shopArrayList = new ArrayList<>();
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetServiceList(String type, String state) {
            this.type = type;
            this.state = state;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            btnBack.setEnabled(false);
            btnFilter.setEnabled(false);
            svFilterName.setEnabled(false);
            swipeRefreshLayout.setEnabled(false);
            serviceList.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "servicelistAPI.php?type=" + type + "&state=" + state)
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
                Log.e("shop log", sresponse);
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
            btnBack.setEnabled(true);
            btnFilter.setEnabled(true);
            svFilterName.setEnabled(true);
            swipeRefreshLayout.setEnabled(true);
            serviceList.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ShopActivity.this, "Connection failed. Please try again later. ", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(ShopActivity.this, "Nothing to show. ", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Shop shop = new Shop();
                        shop.setId(jsonObject.getInt("id"));
                        shop.setUsername(jsonObject.getString("username"));
                        shop.setLogo(jsonObject.getString("logo"));
                        shop.setShopName(jsonObject.getString("shopName"));
                        shop.setType(jsonObject.getString("type"));
                        shop.setPayment(jsonObject.getString("payment"));
                        shop.setMobile(jsonObject.getString("mobile"));
                        shop.setAddress(jsonObject.getString("address"));
                        shop.setState(jsonObject.getString("state"));
                        shop.setDescription(jsonObject.getString("description"));
                        shop.setTotalRating(jsonObject.getInt("totalRating"));
                        shop.setTotalReview(jsonObject.getInt("totalReview"));
                        if (!shop.getUsername().equals(StartActivity.user.getUsername())) {
                            shopArrayList.add(shop);
                        }
                        if (shop.getUsername().equals(StartActivity.user.getUsername()) && jsonArray.length() == 1) {
                            Toast.makeText(ShopActivity.this, "Nothing to show. ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    ShopListAdapter shopListAdapter = new ShopListAdapter(ShopActivity.this, shopArrayList);
                    serviceList.setAdapter(shopListAdapter);
                    svFilterName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String s) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String s) {
                            if (s == null || s.length() == 0) {
                                s = "NONE";
                            }
                            shopListAdapter.getFilter().filter(s);
                            return false;
                        }
                    });
                    btnFilter.setOnClickListener(view -> filterPopup(shopListAdapter));
                    serviceList.setOnItemClickListener((adapterView, view, i, l) -> {
                        Shop shop = shopArrayList.get(i);
                        Intent intent = new Intent(ShopActivity.this, ShopDetailActivity.class);
                        intent.putExtra("shop", shop);
                        startActivity(intent);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("Range")
    private void filterPopup(ShopListAdapter shopListAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
        AlertDialog alertDialog;
        View view = getLayoutInflater().inflate(R.layout.popup_filtershop, null);
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.show();

        RadioGroup rgFilter;
        RadioButton rbNone, rbRating, rbReview;

        rgFilter = view.findViewById(R.id.rg_filter);
        rbNone = view.findViewById(R.id.rb_none);
        rbRating = view.findViewById(R.id.rb_rating);
        rbReview = view.findViewById(R.id.rb_review);

        Cursor cursor = radiobuttonDatabase.getData();
        int option = 0;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            option = cursor.getInt(cursor.getColumnIndex("option"));
        }
        if (option == 1) {
            rbNone.setChecked(true);
        } else if (option == 2) {
            rbRating.setChecked(true);
        } else if (option == 3) {
            rbReview.setChecked(true);
        }

        rgFilter.setOnCheckedChangeListener((radioGroup, i) -> {
            if (radioGroup.getCheckedRadioButtonId() == rbNone.getId()) {
                shopListAdapter.getFilter().filter("NONE");
                shopListAdapter.notifyDataSetChanged();
                svFilterName.clearFocus();
                radiobuttonDatabase.insertData(1);
            } else if (radioGroup.getCheckedRadioButtonId() == rbRating.getId()) {
                shopListAdapter.getFilter().filter("FILTERRATING" + svFilterName.getQuery());
                shopListAdapter.notifyDataSetChanged();
                radiobuttonDatabase.insertData(2);
            } else if (radioGroup.getCheckedRadioButtonId() == rbReview.getId()) {
                shopListAdapter.getFilter().filter("FILTERREVIEW" + svFilterName.getQuery());
                shopListAdapter.notifyDataSetChanged();
                radiobuttonDatabase.insertData(3);
            }
        });
    }
}