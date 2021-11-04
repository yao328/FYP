package com.example.fyp.ui.favshoplist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;
import com.example.fyp.ui.home.Shop;
import com.example.fyp.ui.home.ShopDetailActivity;
import com.example.fyp.ui.home.ShopListAdapter;

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

public class FavshopFragment extends Fragment {

    private SearchView svFilterName;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView serviceList;
    private ProgressBar pbLoading;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favshop, container, false);

        svFilterName = view.findViewById(R.id.sv_filterName);
        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);
        serviceList = view.findViewById(R.id.serviceList);
        pbLoading = view.findViewById(R.id.pb_loading);

        GetFavShopList getFavShopList = new GetFavShopList(StartActivity.user.getUsername());
        getFavShopList.execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        GetFavShopList getFavShopList = new GetFavShopList(StartActivity.user.getUsername());
        getFavShopList.execute();
    }

    private class GetFavShopList extends AsyncTask {
        private final String username;
        private String sresponse = "";
        private boolean isConnectionFail;
        private final ArrayList<String> favShopList = new ArrayList<>();

        public GetFavShopList(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
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
                    .url(Connection.getUrl() + "getfavshoplistAPI.php?username=" + username)
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
            svFilterName.setEnabled(true);
            swipeRefreshLayout.setEnabled(true);
            serviceList.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(requireContext(), "Connection failed. Please try again later. ", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(requireContext(), "Nothing to show. ", Toast.LENGTH_SHORT).show();
                serviceList.setAdapter(null);
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        favShopList.add(" OR `id` = '" + jsonObject.getString("shop") + "'");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringBuilder combine = new StringBuilder();
                for (int i = 0; i < favShopList.size(); i++) {
                    combine.append(favShopList.get(i));
                }
                GetFavShop getFavShop = new GetFavShop(combine.toString().replaceFirst(" OR `id` = ", ""));
                getFavShop.execute();

                swipeRefreshLayout.setOnRefreshListener(() -> {
                    svFilterName.clearFocus();
                    GetFavShopList getFavShopList = new GetFavShopList(StartActivity.user.getUsername());
                    getFavShopList.execute();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFavShop extends AsyncTask {
        private final String id;
        private final ArrayList<Shop> shopArrayList = new ArrayList<>();
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetFavShop(String id) {
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
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
                    .url(Connection.getUrl() + "getfavshopitemAPI.php?id=" + id)
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
            svFilterName.setEnabled(true);
            swipeRefreshLayout.setEnabled(true);
            serviceList.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(requireContext(), "Connection failed. Please try again later. ", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(requireContext(), "Nothing to show. ", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(requireContext(), "Nothing to show. ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    ShopListAdapter shopListAdapter = new ShopListAdapter(requireActivity(), shopArrayList);
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
                    serviceList.setOnItemClickListener((adapterView, view, i, l) -> {
                        Shop shop = shopArrayList.get(i);
                        Intent intent = new Intent(requireContext(), ShopDetailActivity.class);
                        intent.putExtra("shop", shop);
                        startActivity(intent);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}