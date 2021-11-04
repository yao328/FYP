package com.example.fyp.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.fyp.R;

import java.util.ArrayList;

public class RatingActivity extends AppCompatActivity {
    private ArrayList<Review> reviewArrayList;
    private Button btnAll;
    private LinearLayout btn5star, btn4star, btn3star, btn2star, btn1star;
    private TextView tv5star, tv4star, tv3star, tv2star, tv1star;
    private ListView lvReview;
    private MoreReviewAdapter moreReviewAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        reviewArrayList = (ArrayList<Review>) getIntent().getSerializableExtra("reviewArrayList");
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnAll = findViewById(R.id.btn_all);
        btn5star = findViewById(R.id.btn_5star);
        btn4star = findViewById(R.id.btn_4star);
        btn3star = findViewById(R.id.btn_3star);
        btn2star = findViewById(R.id.btn_2star);
        btn1star = findViewById(R.id.btn_1star);
        tv5star = findViewById(R.id.tv_5star);
        tv4star = findViewById(R.id.tv_4star);
        tv3star = findViewById(R.id.tv_3star);
        tv2star = findViewById(R.id.tv_2star);
        tv1star = findViewById(R.id.tv_1star);
        lvReview = findViewById(R.id.lv_reviews);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnAll.setText("All\n" + "(" + reviewArrayList.size() + ")");

        int fivestarcounter = 0;
        for (int i = 0; i < reviewArrayList.size(); i++) {
            Review review = reviewArrayList.get(i);
            if (review.getRating().equals("5")) {
                fivestarcounter++;
            }
        }
        tv5star.setText("(" + fivestarcounter + ")");

        int fourstarcounter = 0;
        for (int i = 0; i < reviewArrayList.size(); i++) {
            Review review = reviewArrayList.get(i);
            if (review.getRating().equals("4")) {
                fourstarcounter++;
            }
        }
        tv4star.setText("(" + fourstarcounter + ")");

        int threestarcounter = 0;
        for (int i = 0; i < reviewArrayList.size(); i++) {
            Review review = reviewArrayList.get(i);
            if (review.getRating().equals("3")) {
                threestarcounter++;
            }
        }
        tv3star.setText("(" + threestarcounter + ")");

        int twostarcounter = 0;
        for (int i = 0; i < reviewArrayList.size(); i++) {
            Review review = reviewArrayList.get(i);
            if (review.getRating().equals("2")) {
                twostarcounter++;
            }
        }
        tv2star.setText("(" + twostarcounter + ")");

        int onestarcounter = 0;
        for (int i = 0; i < reviewArrayList.size(); i++) {
            Review review = reviewArrayList.get(i);
            if (review.getRating().equals("1")) {
                onestarcounter++;
            }
        }
        tv1star.setText("(" + onestarcounter + ")");

        moreReviewAdapter = new MoreReviewAdapter(RatingActivity.this, reviewArrayList);
        lvReview.setAdapter(moreReviewAdapter);

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View[] views = {btnAll, btn5star, btn4star, btn3star, btn2star, btn1star};

                for (View value : views) {
                    value.setBackgroundColor(Color.parseColor("#0C000000"));
                }

                btnAll.setBackgroundResource(R.drawable.border);
                moreReviewAdapter.getFilter().filter("");
            }
        });
        btn5star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View[] views = {btnAll, btn5star, btn4star, btn3star, btn2star, btn1star};

                for (View value : views) {
                    value.setBackgroundColor(Color.parseColor("#0C000000"));
                }

                btn5star.setBackgroundResource(R.drawable.border);
                moreReviewAdapter.getFilter().filter("5");
            }
        });
        btn4star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View[] views = {btnAll, btn5star, btn4star, btn3star, btn2star, btn1star};

                for (View value : views) {
                    value.setBackgroundColor(Color.parseColor("#0C000000"));
                }

                btn4star.setBackgroundResource(R.drawable.border);
                moreReviewAdapter.getFilter().filter("4");
            }
        });
        btn3star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View[] views = {btnAll, btn5star, btn4star, btn3star, btn2star, btn1star};

                for (View value : views) {
                    value.setBackgroundColor(Color.parseColor("#0C000000"));
                }

                btn3star.setBackgroundResource(R.drawable.border);
                moreReviewAdapter.getFilter().filter("3");
            }
        });
        btn2star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View[] views = {btnAll, btn5star, btn4star, btn3star, btn2star, btn1star};

                for (View value : views) {
                    value.setBackgroundColor(Color.parseColor("#0C000000"));
                }

                btn2star.setBackgroundResource(R.drawable.border);
                moreReviewAdapter.getFilter().filter("2");
            }
        });
        btn1star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View[] views = {btnAll, btn5star, btn4star, btn3star, btn2star, btn1star};

                for (View value : views) {
                    value.setBackgroundColor(Color.parseColor("#0C000000"));
                }

                btn1star.setBackgroundResource(R.drawable.border);
                moreReviewAdapter.getFilter().filter("1");
            }
        });
    }
}