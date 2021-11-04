package com.example.fyp.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.fyp.R;
import com.example.fyp.databinding.ActivitySelectLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class SelectLocationActivity extends FragmentActivity implements OnMapReadyCallback {
    private ImageButton btnBack;
    private Button btnDone;
    private SearchView svSearchAddress;
    private ImageButton btnTrack;
    private TextView tvAddress;
    private ProgressBar pbLoading;
    public static String saddress;
    public static double slat, slon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.fyp.databinding.ActivitySelectLocationBinding binding = ActivitySelectLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    @Override
    public void onMapReady(GoogleMap googleMap) {

        btnBack = findViewById(R.id.btn_back);
        btnDone = findViewById(R.id.btn_done);
        svSearchAddress = findViewById(R.id.sv_searchAddress);
        btnTrack = findViewById(R.id.btn_track);
        tvAddress = findViewById(R.id.tv_address);
        pbLoading = findViewById(R.id.pb_loading);

        MarkerOptions markerOptions = new MarkerOptions().title("Your location");

        btnBack.setOnClickListener(view -> {
            slat = 0;
            slon = 0;
            saddress = "";
            finish();
        });

        svSearchAddress.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onQueryTextSubmit(String s) {
                Geocoder geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
                if (!s.isEmpty()) {
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(s, 1);
                        if (addressList != null && addressList.size() != 0) {
                            Address address = addressList.get(0);
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                            markerOptions.position(latLng);
                            googleMap.clear();
                            googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15), 2000, null);

                            tvAddress.setText("Your location: " + address.getAddressLine(0));
                            btnDone.setEnabled(true);
                            btnDone.setTextColor(Color.WHITE);
                        } else {
                            Toast.makeText(SelectLocationActivity.this, "No result found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        btnTrack.setOnClickListener(view -> {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (!EasyPermissions.hasPermissions(SelectLocationActivity.this, perms)) {
                EasyPermissions.requestPermissions(SelectLocationActivity.this, "Allow this app to use location?",
                        0, perms);
            } else {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(SelectLocationActivity.this, "You must turn on GPS!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SelectLocationActivity.this, "Getting your location...", Toast.LENGTH_SHORT).show();
                    pbLoading.setVisibility(View.VISIBLE);
                    btnBack.setEnabled(false);
                    btnDone.setEnabled(false);
                    svSearchAddress.setEnabled(false);
                    btnTrack.setEnabled(false);
                    tvAddress.setEnabled(false);

                    FusedLocationProviderClient fusedLocationProviderClient = LocationServices
                            .getFusedLocationProviderClient(SelectLocationActivity.this);
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
                            try {
                                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);
                                pbLoading.setVisibility(View.GONE);
                                btnBack.setEnabled(true);
                                btnDone.setEnabled(true);
                                svSearchAddress.setEnabled(true);
                                btnTrack.setEnabled(true);
                                tvAddress.setEnabled(true);

                                tvAddress.setText("Your location: " + addressList.get(0).getAddressLine(0));
                                LatLng here = new LatLng(location.getLatitude(), location.getLongitude());
                                markerOptions.position(here);
                                googleMap.clear();
                                googleMap.addMarker(markerOptions);
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(here, 15), 2000, null);

                                btnDone.setEnabled(true);
                                btnDone.setTextColor(Color.WHITE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        googleMap.setOnMapClickListener(latLng -> {
            markerOptions.position(latLng);
            googleMap.clear();
            googleMap.addMarker(markerOptions);

            Geocoder geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                tvAddress.setText("Your location: " + addressList.get(0).getAddressLine(0));
            } catch (IOException e) {
                e.printStackTrace();
            }

            btnDone.setEnabled(true);
            btnDone.setTextColor(Color.WHITE);
        });

        btnDone.setOnClickListener(view -> {
            slat = markerOptions.getPosition().latitude;
            slon = markerOptions.getPosition().longitude;
            saddress = tvAddress.getText().toString().replace("Your location: ", "");
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}