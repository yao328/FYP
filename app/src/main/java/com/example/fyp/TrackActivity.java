package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fyp.ui.businessappointment.AllFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.fyp.databinding.ActivityTrackBinding;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TrackActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ImageButton btnBack, btnTrack;
    private ProgressBar pbLoading;
    private Appointment appointment;
    private Marker staffMarker = null;
    private Handler getstatusHandler;
    private Runnable getstatusRunnable;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.fyp.databinding.ActivityTrackBinding binding = ActivityTrackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnBack = findViewById(R.id.btn_back);
        btnTrack = findViewById(R.id.btn_track);
        pbLoading = findViewById(R.id.pb_loading);
        appointment = (Appointment) getIntent().getSerializableExtra("appointment");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        pbLoading.setVisibility(View.GONE);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LatLng customerLocation = new LatLng(appointment.getLat(), appointment.getLon());
        MarkerOptions customerMarker = new MarkerOptions().position(customerLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.home));
        if (StartActivity.user.getIdentity() == 1 || StartActivity.user.getIdentity() == 3) {
            if (StartActivity.user.getIdentity() == 1) {
                customerMarker.title("Your location");
            } else {
                customerMarker.title("Customer location");
            }
            googleMap.addMarker(customerMarker);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 16), 2000, null);

            contentGetStaffLocation();
        } else if (StartActivity.user.getIdentity() == 2) {
            getappointmentstatus();

            customerMarker.title("Customer location");
            googleMap.addMarker(customerMarker);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 16), 2000, null);

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationProviderClient = LocationServices
                    .getFusedLocationProviderClient(TrackActivity.this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void getappointmentstatus() {
        GetStatus getStatus = new GetStatus();
        getStatus.execute();
        refreshGetStatus();
    }

    private void refreshGetStatus() {
        getstatusHandler = new Handler();
        getstatusRunnable = new Runnable() {
            @Override
            public void run() {
                getappointmentstatus();
            }
        };
        getstatusHandler.postDelayed(getstatusRunnable, 5000);
    }

    private class GetStatus extends AsyncTask {
        private String sresponse = "";
        private boolean isConnectionFail;

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "checkappointmentstatusAPI.php?id=" + appointment.getId())
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

            if (!isConnectionFail && !sresponse.equals("Fail") && !sresponse.equals("Error")) {
                if (sresponse.equals("4")) {
                    finish();
                    getstatusHandler.removeCallbacks(getstatusRunnable);
                    fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
                }
            }
        }
    }

    private final LocationCallback locationCallBack = new LocationCallback() {
        PolylineOptions polylineOptions;

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                LatLng staffLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.e("stafflatlng", String.valueOf(staffLatLng));

                btnTrack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(staffLatLng, 16), 2000, null);
                        int distance = (int) SphericalUtil.computeDistanceBetween(staffLatLng,
                                new LatLng(appointment.getLat(), appointment.getLon()));
                        Toast.makeText(TrackActivity.this, distance + " m", Toast.LENGTH_SHORT).show();
                    }
                });

                UpdateLocation updateLocation = new UpdateLocation(location);
                updateLocation.execute();

                if (polylineOptions == null) {
                    GetRoute getRoute = new GetRoute(location);
                    getRoute.execute();
                }
            }
        }

        class UpdateLocation extends AsyncTask {
            private final Location location;

            UpdateLocation(Location location) {
                this.location = location;
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(30000, TimeUnit.MILLISECONDS)
                        .build();
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("username", StartActivity.user.getUsername())
                        .addFormDataPart("lat", String.valueOf(location.getLatitude()))
                        .addFormDataPart("lon", String.valueOf(location.getLongitude()))
                        .build();
                Request request = new Request.Builder()
                        .url(Connection.getUrl() + "updatelocationAPI.php")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        class GetRoute extends AsyncTask {
            private final Location location;
            private String sresponse = "";

            GetRoute(Location location) {
                this.location = location;
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .connectTimeout(30000, TimeUnit.MILLISECONDS)
                        .build();
                Request request = new Request.Builder()
                        .url("https://api.tomtom.com/routing/1/calculateRoute/" + location.getLatitude() + "," + location.getLongitude()
                                + ":" + appointment.getLat() + "," + appointment.getLon() + "/json?maxAlternatives=1" +
                                "&routeRepresentation=polyline&routeType=shortest&avoid=unpavedRoads&travelMode=car" +
                                "&key=L8cGXIk9czwXfj0pLBrIXYRcRAhFg1AH")
                        .method("GET", null)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    sresponse = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                Log.e("route", ": " + sresponse);
                try {
                    JSONObject jsonObject = new JSONObject(sresponse);
                    JSONArray jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                            .getJSONObject(0).getJSONArray("points");
                    polylineOptions = new PolylineOptions().color(Color.parseColor("#52C9FF"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        polylineOptions.add(new LatLng(jsonArray.getJSONObject(i).getDouble("latitude"),
                                jsonArray.getJSONObject(i).getDouble("longitude")));
                    }
                    googleMap.addPolyline(polylineOptions);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void contentGetStaffLocation() {
        GetStaffLocation getStaffLocation = new GetStaffLocation(appointment.getStaff());
        getStaffLocation.execute();
        refreshGetStaffLocation();
    }

    private void refreshGetStaffLocation() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                contentGetStaffLocation();
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private class GetStaffLocation extends AsyncTask {
        private final String staffUsername;
        private String sresponse = "";

        private GetStaffLocation(String staffUsername) {
            this.staffUsername = staffUsername;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getstaffitemAPI.php?username='" + staffUsername + "'")
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (!sresponse.equals("Fail") && !sresponse.equals("Error")) {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    LatLng staffLatLng = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lon"));
                    if (staffMarker == null) {
                        MarkerOptions staffMarkerOptions = new MarkerOptions()
                                .title("Staff")
                                .position(staffLatLng);
                        staffMarker = googleMap.addMarker(staffMarkerOptions);
                    } else {
                        staffMarker.setPosition(staffLatLng);
                    }

                    btnTrack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(staffLatLng, 16), 2000,
                                    null);
                            int distance = (int) SphericalUtil.computeDistanceBetween(staffLatLng,
                                    new LatLng(appointment.getLat(), appointment.getLon()));
                            Toast.makeText(TrackActivity.this, distance + " m", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}