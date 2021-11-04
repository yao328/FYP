package com.example.fyp.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.fyp.R;

import org.apache.commons.text.StringEscapeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReviewAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<Review> reviewArrayList;

    public ReviewAdapter(Activity activity, ArrayList<Review> reviewArrayList) {
        this.activity = activity;
        this.reviewArrayList = reviewArrayList;
    }

    @Override
    public int getCount() {
        return Math.min(reviewArrayList.size(), 5);
    }

    @Override
    public Object getItem(int i) {
        return reviewArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.item_review, null);
        }

        Review review = reviewArrayList.get(i);
        RatingBar rbRate = view.findViewById(R.id.rb_rate);
        TextView tvDate, tvComment, tvUser;

        tvDate = view.findViewById(R.id.tv_date);
        tvComment = view.findViewById(R.id.tv_comment);
        tvUser = view.findViewById(R.id.tv_user);

        rbRate.setRating(Float.parseFloat(review.getRating()));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(review.getDate());
            if (date != null) {
                tvDate.setText(displayDateFormat.format(date));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvComment.setText(StringEscapeUtils.unescapeJava(review.getComment().replace("SLASH", "\\")));
        tvUser.setText("By: " + review.getUser());

        return view;
    }
}
