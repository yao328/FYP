package com.example.fyp;

import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.fyp.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.fyp.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        switch (StartActivity.user.getIdentity()) {
            case 1: {
                navigationView.getMenu().findItem(R.id.nav_businessappointment).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_stafflist).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_editbusiness).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_businessreport).setVisible(false);
                break;
            }
            case 2: {
                navigationView.getMenu().findItem(R.id.nav_registerbusiness).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_stafflist).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_editbusiness).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_businessreport).setVisible(false);
                break;
            }
            case 3: {
                navigationView.getMenu().findItem(R.id.nav_registerbusiness).setVisible(false);
                break;
            }
        }
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_favshop, R.id.nav_registerbusiness, R.id.nav_businessappointment, R.id.nav_appointment,
                R.id.nav_stafflist, R.id.nav_editbusiness, R.id.nav_businessreport, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();

        View view = navigationView.getHeaderView(0);
        ImageView ivNavProfilePic = view.findViewById(R.id.iv_nav_profilePic);
        TextView tvNavUsername = view.findViewById(R.id.tv_nav_username);
        TextView tvNavFullname = view.findViewById(R.id.tv_nav_fullname);

        Glide.with(MainActivity.this)
                .load(StartActivity.user.getProfilePic())
                .thumbnail(Glide.with(MainActivity.this).load(R.drawable.loading))
                .into(ivNavProfilePic);
        tvNavUsername.setText(StartActivity.user.getUsername());
        tvNavFullname.setText(StartActivity.user.getFullName());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
            if (StartActivity.isStart) {
                finishAffinity();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}