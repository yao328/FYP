package com.example.fyp.ui.home;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.SetListViewHeight;
import com.example.fyp.StartActivity;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShopDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private ImageView ivLogo;
    private TextView tvShopName;
    private RatingBar rbAvrRating;
    private TextView tvTotalReview;
    private ImageButton btnFav;
    private TextView tvMobile, tvType, tvPayment, tvAddress, tvDescription;
    private Button btnSelectService;
    private ListView lvReview;
    private Button btnAllReview;
    private ProgressBar pbLoading;
    private boolean isFav;
    private TextView tvUserAddress;
    private Button btnConfirmBooking;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        btnBack = findViewById(R.id.btn_back);
        ivLogo = findViewById(R.id.iv_logo);
        tvShopName = findViewById(R.id.tv_shopName);
        rbAvrRating = findViewById(R.id.rb_avrRating);
        tvTotalReview = findViewById(R.id.tv_totalReview);
        btnFav = findViewById(R.id.btn_fav);
        tvMobile = findViewById(R.id.tv_mobile);
        tvType = findViewById(R.id.tv_type);
        tvPayment = findViewById(R.id.tv_payment);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        btnSelectService = findViewById(R.id.btn_selectService);
        lvReview = findViewById(R.id.lv_reviews);
        btnAllReview = findViewById(R.id.btn_allReview);
        pbLoading = findViewById(R.id.pb_loading);

        btnBack.setOnClickListener(view -> finish());

        Intent intent = getIntent();
        if (intent != null) {
            Shop shop = (Shop) intent.getSerializableExtra("shop");

            Glide.with(ShopDetailActivity.this)
                    .load(Connection.getUrl() + shop.getLogo())
                    .thumbnail(Glide.with(ShopDetailActivity.this).load(R.drawable.loading))
                    .into(ivLogo);
            tvShopName.setText(shop.getShopName());
            rbAvrRating.setRating((float) (shop.getTotalRating() / shop.getTotalReview()));
            tvTotalReview.setText("(" + shop.getTotalReview() + ")");
            tvMobile.setText(shop.getMobile());
            tvType.setText(shop.getType());
            tvPayment.setText(shop.getPayment());
            tvAddress.setText(shop.getAddress());
            tvDescription.setText(StringEscapeUtils.unescapeJava(shop.getDescription().replace("SLASH", "\\")));

            GetButtonFav getButtonFav = new GetButtonFav(StartActivity.user.getUsername(), shop.getId());
            getButtonFav.execute();

            GetServiceList getServiceList = new GetServiceList(shop.getId());
            getServiceList.execute();

            GetReview getReview = new GetReview(shop.getUsername());
            getReview.execute();

            btnFav.setOnClickListener(view -> {
                if (isFav) {
                    UnfavShop unfavShop = new UnfavShop(StartActivity.user.getUsername(), shop.getId());
                    unfavShop.execute();
                } else {
                    FavShop favShop = new FavShop(StartActivity.user.getUsername(), shop.getId());
                    favShop.execute();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String saddress = SelectLocationActivity.saddress;
        if (saddress != null && !saddress.equals("")) {
            tvUserAddress.setText(saddress);
            btnConfirmBooking.setEnabled(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetServiceList extends AsyncTask {
        private final int shopID;
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetServiceList(int shopID) {
            this.shopID = shopID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            btnBack.setEnabled(false);
            ivLogo.setEnabled(false);
            tvShopName.setEnabled(false);
            rbAvrRating.setEnabled(false);
            tvTotalReview.setEnabled(false);
            btnFav.setEnabled(false);
            tvMobile.setEnabled(false);
            tvType.setEnabled(false);
            tvPayment.setEnabled(false);
            tvAddress.setEnabled(false);
            tvDescription.setEnabled(false);
            btnSelectService.setEnabled(false);
            lvReview.setEnabled(false);
            btnAllReview.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getserviceAPI.php?shopID=" + shopID)
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
            btnBack.setEnabled(true);
            ivLogo.setEnabled(true);
            tvShopName.setEnabled(true);
            rbAvrRating.setEnabled(true);
            tvTotalReview.setEnabled(true);
            btnFav.setEnabled(true);
            tvMobile.setEnabled(true);
            tvType.setEnabled(true);
            tvPayment.setEnabled(true);
            tvAddress.setEnabled(true);
            tvDescription.setEnabled(true);
            btnSelectService.setEnabled(true);
            lvReview.setEnabled(true);
            btnAllReview.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ShopDetailActivity.this, "Connection failed. Please try again later.",
                        Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                btnSelectService.setText("Select services (0 services)");
                Toast.makeText(ShopDetailActivity.this, "Error to get service.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    btnSelectService.setText("Select services (" + jsonArray.length() + " services)");
                    btnSelectService.setOnClickListener(view -> selectServicePopup(jsonArray));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
        private void selectServicePopup(JSONArray jsonArray) {
            Booking booking = new Booking();
            AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailActivity.this, R.style.Theme_FYP_NoActionBar);
            AlertDialog alertDialog;
            View view = getLayoutInflater().inflate(R.layout.popup_bookservice, null);
            builder.setView(view);
            alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
            alertDialog.getWindow().getAttributes().dimAmount = (float) 0.25;
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            alertDialog.getWindow().setGravity(Gravity.BOTTOM);

            ArrayList<Service> serviceArrayList = new ArrayList<>();
            ListView lvSelectservicelist = view.findViewById(R.id.lv_selectservicelist);
            Button btnConfirmService = view.findViewById(R.id.btn_confirmService);

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Service service = new Service();
                    service.setId(jsonObject.getInt("id"));
                    service.setUsername(jsonObject.getString("username"));
                    service.setShopID(jsonObject.getInt("shopID"));
                    service.setServiceName(jsonObject.getString("serviceName"));
                    service.setServicePrice(jsonObject.getString("servicePrice"));
                    serviceArrayList.add(service);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ServiceListAdapter serviceListAdapter = new ServiceListAdapter(ShopDetailActivity.this, serviceArrayList);
            lvSelectservicelist.setAdapter(serviceListAdapter);
            lvSelectservicelist.setOnItemClickListener((adapterView, view1, i, l) -> {
                btnConfirmService.setEnabled(true);
                btnConfirmService.setTextColor(Color.WHITE);
                btnConfirmService.setBackgroundResource(R.drawable.buttonbackground);
                btnConfirmService.setOnClickListener(view2 -> {
                    booking.setShopID(serviceArrayList.get(i).getShopID());
                    booking.setServiceID(serviceArrayList.get(i).getId());
                    booking.setServicename(serviceArrayList.get(i).getServiceName());
                    booking.setServiceprice(serviceArrayList.get(i).getServicePrice());

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ShopDetailActivity.this, R.style.Theme_FYP_NoActionBar);
                    AlertDialog alertDialog1;
                    View view3 = getLayoutInflater().inflate(R.layout.popup_selectdatetime, null);
                    builder1.setView(view3);
                    alertDialog1 = builder1.create();
                    alertDialog1.show();
                    alertDialog1.setCanceledOnTouchOutside(false);
                    alertDialog1.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
                    alertDialog1.getWindow().getAttributes().dimAmount = (float) 0.25;
                    alertDialog1.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    alertDialog1.getWindow().setGravity(Gravity.BOTTOM);

                    ImageButton btnBack;
                    EditText tvSelectDate, tvSelectTime, tvServiceRemark;
                    Button btnNext;

                    btnBack = view3.findViewById(R.id.btn_back);
                    tvSelectDate = view3.findViewById(R.id.tv_selectDate);
                    tvSelectTime = view3.findViewById(R.id.tv_selectTime);
                    tvServiceRemark = view3.findViewById(R.id.et_remark);
                    btnNext = view3.findViewById(R.id.btn_next);

                    btnBack.setOnClickListener(view4 -> alertDialog1.dismiss());
                    TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
                    Calendar calendar = Calendar.getInstance(timeZone);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");

                    tvSelectDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" +
                            calendar.get(Calendar.YEAR));
                    tvSelectDate.setOnClickListener(view4 -> {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(ShopDetailActivity.this);
                        datePickerDialog.show();
                        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                        Calendar nextmonth = Calendar.getInstance(timeZone);
                        nextmonth.set(calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.getDatePicker().setMaxDate(nextmonth.getTimeInMillis());
                        datePickerDialog.setOnDateSetListener((datePicker, i12, i1, i2) -> tvSelectDate.setText(i2 + "/" + (i1 + 1) + "/" + i12));
                    });

                    calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
                    calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - calendar.get(Calendar.MINUTE) % 5);
                    tvSelectTime.setText(simpleDateFormat.format(calendar.getTime()));
                    tvSelectTime.setOnClickListener(view4 -> {
                        @SuppressLint({"PrivateResource", "SimpleDateFormat"})
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                ShopDetailActivity.this, (timePicker, i13, i1) -> {
                            Calendar time = Calendar.getInstance(timeZone);
                            Calendar current = Calendar.getInstance(timeZone);
                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
                            try {
                                Date date = simpleDateFormat1.parse(tvSelectDate.getText().toString());
                                Calendar convertdate = Calendar.getInstance();
                                if (date != null) {
                                    convertdate.setTime(date);
                                }
                                time.set(Calendar.YEAR, convertdate.get(Calendar.YEAR));
                                time.set(Calendar.MONTH, convertdate.get(Calendar.MONTH));
                                time.set(Calendar.DAY_OF_MONTH, convertdate.get(Calendar.DAY_OF_MONTH));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            time.set(Calendar.HOUR_OF_DAY, i13);
                            time.set(Calendar.MINUTE, i1 - i1 % 5);
                            if (time.getTimeInMillis() - current.getTimeInMillis() <= 3.6e+6) {
                                Toast.makeText(ShopDetailActivity.this, "Invalid time", Toast.LENGTH_SHORT).show();
                            } else {
                                tvSelectTime.setText(simpleDateFormat.format(time.getTime()));
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                        timePickerDialog.show();
                    });

                    btnNext.setOnClickListener(view4 -> {
                        booking.setServicedate(tvSelectDate.getText().toString());
                        booking.setServicetime(tvSelectTime.getText().toString());
                        booking.setServiceremark(tvServiceRemark.getText().toString());

                        AlertDialog.Builder builder2 = new AlertDialog.Builder(ShopDetailActivity.this, R.style.Theme_FYP_NoActionBar);
                        AlertDialog alertDialog2;
                        View view5 = getLayoutInflater().inflate(R.layout.popup_confirmbooking, null);
                        builder2.setView(view5);
                        alertDialog2 = builder2.create();
                        alertDialog2.show();
                        alertDialog2.setCanceledOnTouchOutside(false);
                        alertDialog2.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                (int) (getResources().getDisplayMetrics().heightPixels * 0.7));
                        alertDialog2.getWindow().getAttributes().dimAmount = (float) 0.25;
                        alertDialog2.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        alertDialog2.getWindow().setGravity(Gravity.BOTTOM);

                        ImageButton btnBack1 = view5.findViewById(R.id.btn_back);
                        TextView tvServiceName, tvServicePrice, tvServiceDate, tvServiceTime, tvServiceRemark1;
                        Button btnSelectLocation;

                        tvUserAddress = view5.findViewById(R.id.tv_address);
                        tvServiceName = view5.findViewById(R.id.tv_serviceName);
                        tvServicePrice = view5.findViewById(R.id.tv_servicePrice);
                        tvServiceDate = view5.findViewById(R.id.tv_serviceDate);
                        tvServiceTime = view5.findViewById(R.id.tv_serviceTime);
                        tvServiceRemark1 = view5.findViewById(R.id.tv_serviceRemark);
                        btnSelectLocation = view5.findViewById(R.id.btn_selectLocation);
                        btnConfirmBooking = view5.findViewById(R.id.btn_confirmBooking);

                        btnBack1.setOnClickListener(view6 -> alertDialog2.dismiss());

                        tvServiceName.setText(booking.getServicename());
                        tvServicePrice.setText("RM " + booking.getServiceprice());
                        tvServiceDate.setText(booking.getServicedate());
                        tvServiceTime.setText(booking.getServicetime());
                        tvServiceRemark1.setText(booking.getServiceremark());

                        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(ShopDetailActivity.this, SelectLocationActivity.class));
                            }
                        });

                        btnConfirmBooking.setOnClickListener(view6 -> {
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat databasedateformat, oridatformat;
                            oridatformat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                            databasedateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = null;
                            try {
                                date = oridatformat.parse(booking.getServicedate() + " " + booking.getServicetime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            DoneBooking doneBooking = new DoneBooking(btnBack1, btnConfirmBooking, StartActivity.user.getUsername(),
                                    StartActivity.user.getFullName(), booking.getShopID(), SelectLocationActivity.saddress,
                                    booking.getServiceID(), booking.getServicename(), booking.getServiceprice(),
                                    databasedateformat.format(Objects.requireNonNull(date)), booking.getServiceremark());
                            doneBooking.execute();

                            alertDialog.dismiss();
                            alertDialog1.dismiss();
                            alertDialog2.dismiss();
                        });
                    });
                });
            });
        }

        private class DoneBooking extends AsyncTask {
            private final ImageButton btnBack;
            private final Button btnConfirmBooking;
            private final String user, userName;
            private final int shopID;
            private final String address;
            private final int serviceID;
            private final String serviceName, servicePrice, serviceDateTime, serviceRemark;
            private String sresponse = "";
            private boolean isConnectionFail;

            public DoneBooking(ImageButton btnBack, Button btnConfirmBooking, String user, String userName, int shopID,
                               String address, int serviceID, String serviceName, String servicePrice,
                               String serviceDateTime, String serviceRemark) {
                this.btnBack = btnBack;
                this.btnConfirmBooking = btnConfirmBooking;
                this.user = user;
                this.userName = userName;
                this.shopID = shopID;
                this.address = address;
                this.serviceID = serviceID;
                this.serviceName = serviceName;
                this.servicePrice = servicePrice;
                this.serviceDateTime = serviceDateTime;
                this.serviceRemark = serviceRemark;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pbLoading.setVisibility(View.VISIBLE);
                btnBack.setEnabled(false);
                btnConfirmBooking.setEnabled(false);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(30000, TimeUnit.MILLISECONDS)
                        .build();
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("user", user)
                        .addFormDataPart("userName", userName)
                        .addFormDataPart("shopID", String.valueOf(shopID))
                        .addFormDataPart("address", address)
                        .addFormDataPart("lat", String.valueOf(SelectLocationActivity.slat))
                        .addFormDataPart("lon", String.valueOf(SelectLocationActivity.slon))
                        .addFormDataPart("serviceID", String.valueOf(serviceID))
                        .addFormDataPart("serviceName", serviceName)
                        .addFormDataPart("servicePrice", servicePrice)
                        .addFormDataPart("serviceDateTime", serviceDateTime)
                        .addFormDataPart("serviceRemark", serviceRemark)
                        .build();
                Request request = new Request.Builder()
                        .url(Connection.getUrl() + "bookingAPI.php")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    sresponse = Objects.requireNonNull(response.body()).string();
                    Log.e("shopID", String.valueOf(shopID));
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
                btnConfirmBooking.setEnabled(true);

                if (isConnectionFail || sresponse.equals("Fail")) {
                    Toast.makeText(ShopDetailActivity.this, "Connection failed. Please try again later.",
                            Toast.LENGTH_SHORT).show();
                } else if (sresponse.equals("Error")) {
                    Toast.makeText(ShopDetailActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                } else if (sresponse.equals("OK")) {
                    Toast.makeText(ShopDetailActivity.this, "Done.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetButtonFav extends AsyncTask {
        private final String username;
        private final int shop;
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetButtonFav(String username, int shop) {
            this.username = username;
            this.shop = shop;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btnFav.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getfavshopAPI.php?username=" + username + "&shop=" + shop)
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

            btnFav.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ShopDetailActivity.this, "Connection failed. Please try again later.",
                        Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                isFav = false;
                btnFav.setImageResource(R.drawable.ic_unfav);
            } else if (sresponse.equals("OK")) {
                isFav = true;
                btnFav.setImageResource(R.drawable.ic_fav);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UnfavShop extends AsyncTask {
        private final String username;
        private final int shop;
        private String sresponse = "";
        private boolean isConnectionFail;

        public UnfavShop(String username, int shop) {
            this.username = username;
            this.shop = shop;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "unfavshopAPI.php?username=" + username + "&shop=" + shop)
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

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ShopDetailActivity.this, "Connection failed. Please try again later.",
                        Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(ShopDetailActivity.this, "Error.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("OK")) {
                isFav = false;
                btnFav.setImageResource(R.drawable.ic_unfav);
                Toast.makeText(ShopDetailActivity.this, "Removed this shop from your favourite list.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FavShop extends AsyncTask {
        private final String username;
        private final int shop;
        private String sresponse = "";
        private boolean isConnectionFail;

        public FavShop(String username, int shop) {
            this.username = username;
            this.shop = shop;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "favshopAPI.php?username=" + username + "&shop=" + shop)
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

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ShopDetailActivity.this, "Connection failed. Please try again later.",
                        Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(ShopDetailActivity.this, "Error.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("OK")) {
                isFav = true;
                btnFav.setImageResource(R.drawable.ic_fav);
                Toast.makeText(ShopDetailActivity.this, "Added this shop to your favourite list.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class Booking {
        private int serviceID, shopID;
        private String servicename, serviceprice, servicedate, servicetime, serviceremark;

        public int getServiceID() {
            return serviceID;
        }

        public void setServiceID(int serviceID) {
            this.serviceID = serviceID;
        }

        public int getShopID() {
            return shopID;
        }

        public void setShopID(int shopID) {
            this.shopID = shopID;
        }

        public String getServicename() {
            return servicename;
        }

        public void setServicename(String servicename) {
            this.servicename = servicename;
        }

        public String getServiceprice() {
            return serviceprice;
        }

        public void setServiceprice(String serviceprice) {
            this.serviceprice = serviceprice;
        }

        public String getServicedate() {
            return servicedate;
        }

        public void setServicedate(String servicedate) {
            this.servicedate = servicedate;
        }

        public String getServicetime() {
            return servicetime;
        }

        public void setServicetime(String servicetime) {
            this.servicetime = servicetime;
        }

        public String getServiceremark() {
            return serviceremark;
        }

        public void setServiceremark(String serviceremark) {
            this.serviceremark = serviceremark;
        }
    }

    private class GetReview extends AsyncTask {
        private final String boss;
        private String sresponse = "";
        private boolean isConnectionFail;

        private GetReview(String boss) {
            this.boss = boss;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            btnBack.setEnabled(false);
            ivLogo.setEnabled(false);
            tvShopName.setEnabled(false);
            rbAvrRating.setEnabled(false);
            tvTotalReview.setEnabled(false);
            btnFav.setEnabled(false);
            tvMobile.setEnabled(false);
            tvType.setEnabled(false);
            tvPayment.setEnabled(false);
            tvAddress.setEnabled(false);
            tvDescription.setEnabled(false);
            btnSelectService.setEnabled(false);
            lvReview.setEnabled(false);
            btnAllReview.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getallreviewAPI.php?boss=" + boss)
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
            btnBack.setEnabled(true);
            ivLogo.setEnabled(true);
            tvShopName.setEnabled(true);
            rbAvrRating.setEnabled(true);
            tvTotalReview.setEnabled(true);
            btnFav.setEnabled(true);
            tvMobile.setEnabled(true);
            tvType.setEnabled(true);
            tvPayment.setEnabled(true);
            tvAddress.setEnabled(true);
            tvDescription.setEnabled(true);
            btnSelectService.setEnabled(true);
            lvReview.setEnabled(false);
            btnAllReview.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ShopDetailActivity.this, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (!sresponse.equals("Error")) {
                ArrayList<Review> reviewArrayList = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Review review = new Review();
                        review.setId(jsonObject.getString("id"));
                        review.setBoss(jsonObject.getString("boss"));
                        review.setUser(jsonObject.getString("user"));
                        review.setRating(jsonObject.getString("rating"));
                        review.setComment(jsonObject.getString("comment"));
                        review.setDate(jsonObject.getString("date"));
                        reviewArrayList.add(review);
                    }
                    btnAllReview.setText("See All Reviews (" + jsonArray.length() + ") >");
                    ReviewAdapter reviewAdapter = new ReviewAdapter(ShopDetailActivity.this, reviewArrayList);
                    lvReview.setAdapter(reviewAdapter);
                    SetListViewHeight.setListViewHeightBasedOnItems(lvReview);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                btnAllReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ShopDetailActivity.this, RatingActivity.class);
                        intent.putExtra("reviewArrayList", reviewArrayList);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}