package com.example.fyp.ui.businessappointment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;
import com.example.fyp.ui.stafflist.ChatActivity;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppointmentFragment extends Fragment {
    private TabLayout tlBossAppointment;
    private ViewPager2 vpBossAppointment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);
        tlBossAppointment = view.findViewById(R.id.tl_bossAppointment);
        vpBossAppointment = view.findViewById(R.id.vp_bossAppointment);

        setHasOptionsMenu(true);

        FragmentAdapter fragmentAdapter = new FragmentAdapter(requireActivity().getSupportFragmentManager(), getLifecycle());
        vpBossAppointment.setAdapter(fragmentAdapter);
        tlBossAppointment.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpBossAppointment.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        vpBossAppointment.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tlBossAppointment.selectTab(tlBossAppointment.getTabAt(position));
            }
        });

        return view;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (StartActivity.user.getIdentity() == 2) {
            inflater.inflate(R.menu.staffchatmenu, menu);
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_chat) {
            GetBoss getBoss = new GetBoss();
            getBoss.execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetBoss extends AsyncTask {
        private String sresponse = "";
        private boolean isConnectionFail;

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "staffgetbossAPI.php?staff=" + StartActivity.user.getUsername())
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = response.body().string();
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
                Toast.makeText(requireContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(requireActivity(), ChatActivity.class);
                intent.putExtra("myusername", StartActivity.user.getUsername());
                intent.putExtra("targetusername", sresponse);
                startActivity(intent);
            }
        }
    }
}