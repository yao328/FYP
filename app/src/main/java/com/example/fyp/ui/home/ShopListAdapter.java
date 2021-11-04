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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.fyp.Connection;
import com.example.fyp.R;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

public class ShopListAdapter extends BaseAdapter implements Filterable {
    private final Activity activity;
    private final ArrayList<Shop> shopArrayList;
    private final ArrayList<Shop> filteredShopArrayList = new ArrayList<>();

    public ShopListAdapter(Activity activity, ArrayList<Shop> shopArrayList) {
        this.activity = activity;
        this.shopArrayList = shopArrayList;
        filteredShopArrayList.addAll(shopArrayList);
    }

    @Override
    public int getCount() {
        return filteredShopArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredShopArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.item_shoplist, null);
        }

        ImageView ivLogo;
        TextView tvShopName, tvMobile, tvAddress, tvDescription, tvTotalReview;
        RatingBar rbAvrRating;

        ivLogo = view.findViewById(R.id.iv_logo);
        tvShopName = view.findViewById(R.id.tv_shopName);
        tvMobile = view.findViewById(R.id.tv_mobile);
        tvAddress = view.findViewById(R.id.tv_address);
        tvDescription = view.findViewById(R.id.tv_description);
        tvTotalReview = view.findViewById(R.id.tv_totalReview);
        rbAvrRating = view.findViewById(R.id.rb_avrRating);

        Shop shop = filteredShopArrayList.get(i);

        Glide.with(activity)
                .load(Connection.getUrl() + shop.getLogo())
                .thumbnail(Glide.with(activity)
                        .load(R.drawable.loading))
                .into(ivLogo);
        tvShopName.setText(shop.getShopName());
        tvMobile.setText(shop.getMobile());
        tvAddress.setText(shop.getAddress());
        tvDescription.setText(StringEscapeUtils.unescapeJava(shop.getDescription().replace("SLASH", "\\")));
        double rating = shop.getTotalRating() / shop.getTotalReview();
        rbAvrRating.setRating((float) rating);
        tvTotalReview.setText("(" + shop.getTotalReview() + ")");

        return view;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Shop> filterlist = new ArrayList<>();

            if (charSequence.toString().contains("NONE")) {
                filterlist.addAll(shopArrayList);
            } else {
                if (charSequence.toString().contains("FILTERRATING")) {
                    String filterString = charSequence.toString().replace("FILTERRATING", "").toLowerCase().trim();
                    for (int i = 0; i < shopArrayList.size(); i++) {
                        if (shopArrayList.get(i).getShopName().toLowerCase().contains(filterString)) {
                            filterlist.add(shopArrayList.get(i));
                        }
                    }
                    filterlist.sort((shop, t1) -> {
                        double rating = shop.getTotalRating() / shop.getTotalReview();
                        double rating2 = t1.getTotalRating() / t1.getTotalReview();
                        return Double.compare(rating2, rating);
                    });
                } else if (charSequence.toString().contains("FILTERREVIEW")) {
                    String filterString = charSequence.toString().replace("FILTERREVIEW", "").toLowerCase().trim();
                    for (int i = 0; i < shopArrayList.size(); i++) {
                        if (shopArrayList.get(i).getShopName().toLowerCase().contains(filterString)) {
                            filterlist.add(shopArrayList.get(i));
                        }
                    }
                    filterlist.sort((shop, t1) -> Integer.compare(t1.getTotalReview(), shop.getTotalReview()));
                } else {
                    String filterString = charSequence.toString().toLowerCase().trim();
                    for (int i = 0; i < shopArrayList.size(); i++) {
                        if (shopArrayList.get(i).getShopName().toLowerCase().contains(filterString)) {
                            filterlist.add(shopArrayList.get(i));
                        }
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.count = filterlist.size();
            filterResults.values = filterlist;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredShopArrayList.clear();
            filteredShopArrayList.addAll((ArrayList<Shop>) filterResults.values);
            notifyDataSetChanged();
        }
    };
}
