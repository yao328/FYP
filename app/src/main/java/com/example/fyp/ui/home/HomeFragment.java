package com.example.fyp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fyp.R;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        GridView gvCategory = view.findViewById(R.id.gv_category);

        CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity());
        gvCategory.setAdapter(categoryAdapter);
        gvCategory.setOnItemClickListener((adapterView, view1, i, l) -> {
            /*Intent intent = new Intent(getActivity(), SelectLocationActivity.class);
            intent.putExtra("type", CategoryAdapter.categoryName[i]);
            startActivity(intent);*/
            Intent intent = new Intent(getActivity(), ShopActivity.class);
            intent.putExtra("type", CategoryAdapter.categoryName[i]);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}