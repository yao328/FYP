package com.example.fyp.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fyp.R;

public class CategoryAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private final Activity activity;
    private final int[] categoryImg = {R.drawable.house, R.drawable.car, R.drawable.beauty, R.drawable.medical,
            R.drawable.education, R.drawable.pet, R.drawable.other};
    public static String[] categoryName = {"House", "Car", "Beauty", "Medical", "Education", "Pet", "Other"};

    public CategoryAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return categoryImg.length;
    }

    @Override
    public Object getItem(int i) {
        return categoryImg[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null) {
            view = inflater.inflate(R.layout.item_category, null);
        }

        ImageView ivCategory = view.findViewById(R.id.iv_category);
        TextView tvCategory = view.findViewById(R.id.tv_category);

        ivCategory.setImageResource(categoryImg[i]);
        tvCategory.setText(categoryName[i]);

        return view;
    }
}
