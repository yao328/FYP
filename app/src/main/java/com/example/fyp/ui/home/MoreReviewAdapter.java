package com.example.fyp.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.fyp.R;

import org.apache.commons.text.StringEscapeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MoreReviewAdapter extends BaseAdapter implements Filterable {
    private final Activity activity;
    private final ArrayList<Review> reviewArrayList;
    private final ArrayList<Review> filteredReviewArrayList = new ArrayList<>();

    public MoreReviewAdapter(Activity activity, ArrayList<Review> reviewArrayList) {
        this.activity = activity;
        this.reviewArrayList = reviewArrayList;
        filteredReviewArrayList.addAll(reviewArrayList);
    }

    @Override
    public int getCount() {
        return filteredReviewArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredReviewArrayList.get(i);
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

        Review review = filteredReviewArrayList.get(i);
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

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Review> arrayList = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                arrayList.addAll(reviewArrayList);
            } else if (charSequence.toString().equals("5")) {
                for (int i = 0; i < reviewArrayList.size(); i++) {
                    Review review = reviewArrayList.get(i);
                    if (review.getRating().equals("5")) {
                        arrayList.add(review);
                    }
                }
            } else if (charSequence.toString().equals("4")) {
                for (int i = 0; i < reviewArrayList.size(); i++) {
                    Review review = reviewArrayList.get(i);
                    if (review.getRating().equals("4")) {
                        arrayList.add(review);
                    }
                }
            } else if (charSequence.toString().equals("3")) {
                for (int i = 0; i < reviewArrayList.size(); i++) {
                    Review review = reviewArrayList.get(i);
                    if (review.getRating().equals("3")) {
                        arrayList.add(review);
                    }
                }
            } else if (charSequence.toString().equals("2")) {
                for (int i = 0; i < reviewArrayList.size(); i++) {
                    Review review = reviewArrayList.get(i);
                    if (review.getRating().equals("2")) {
                        arrayList.add(review);
                    }
                }
            } else if (charSequence.toString().equals("1")) {
                for (int i = 0; i < reviewArrayList.size(); i++) {
                    Review review = reviewArrayList.get(i);
                    if (review.getRating().equals("1")) {
                        arrayList.add(review);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.count = arrayList.size();
            filterResults.values = arrayList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredReviewArrayList.clear();
            filteredReviewArrayList.addAll((ArrayList<Review>) filterResults.values);
            notifyDataSetChanged();
        }
    };
}
