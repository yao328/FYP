package com.example.fyp.ui.businessreport;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fyp.R;
import com.google.android.material.tabs.TabLayout;

public class MainFragment extends Fragment {
    private TabLayout tlBusinessReport;
    private ViewPager2 vpBusinessReport;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        tlBusinessReport = view.findViewById(R.id.tl_businessReport);
        vpBusinessReport = view.findViewById(R.id.vp_businessReport);

        FragmentAdapter fragmentAdapter = new FragmentAdapter(requireActivity().getSupportFragmentManager(), getLifecycle());
        vpBusinessReport.setAdapter(fragmentAdapter);
        tlBusinessReport.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpBusinessReport.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        vpBusinessReport.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tlBusinessReport.selectTab(tlBusinessReport.getTabAt(position));
            }
        });

        return view;
    }

}