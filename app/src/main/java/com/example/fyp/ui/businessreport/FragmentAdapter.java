package com.example.fyp.ui.businessreport;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter {
    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AllFragment();
            case 1:
                return new MonthFragment();
            case 2:
                return new YearFragment();
            case 3:
                return new CustomFragment();
        }
        return new AllFragment();
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}