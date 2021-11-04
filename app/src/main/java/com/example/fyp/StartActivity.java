package com.example.fyp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StartActivity extends AppCompatActivity {
    public static boolean isStart = true;
    public static User user = new User();
    private Button btnGotoSignIn, btnGotoSignUp;
    private ProgressBar pbLoading;
    private final UserDatabase userDatabase = new UserDatabase(StartActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnGotoSignIn = findViewById(R.id.btn_goToSignIn);
        btnGotoSignUp = findViewById(R.id.btn_goToSignUp);
        pbLoading = findViewById(R.id.pb_loading);

        Cursor cursor = userDatabase.getData();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex("username"));
            @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));
            SignIn signIn = new SignIn(username, password);
            signIn.execute();
        }

        btnGotoSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(StartActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        btnGotoSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(StartActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class SignIn extends AsyncTask {
        private final String username, password;
        private String sresponse = "";
        private boolean isConnectionFail;

        public SignIn(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnGotoSignIn.setEnabled(false);
            btnGotoSignUp.setEnabled(false);
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "loginAPI.php?username=" + username + "&password=" + password)
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
                Log.e("start response", sresponse);
            } catch (IOException e) {
                e.printStackTrace();
                isConnectionFail = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            btnGotoSignIn.setEnabled(true);
            btnGotoSignUp.setEnabled(true);
            pbLoading.setVisibility(View.GONE);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(StartActivity.this, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(StartActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                    user.setUsername(jsonObject.getString("username"));
                    user.setPassword(jsonObject.getString("password"));
                    user.setFullName(jsonObject.getString("fullName"));
                    user.setGender(!jsonObject.getString("gender").equals("0"));
                    user.setAge(jsonObject.getInt("age"));
                    user.setAddress(jsonObject.getString("address"));
                    user.setState(jsonObject.getString("state"));
                    user.setLat(jsonObject.optDouble("lat"));
                    user.setLon(jsonObject.optDouble("lon"));
                    user.setMobile(jsonObject.getString("mobile"));
                    user.setProfilePic(Connection.getUrl() + jsonObject.getString("profilePic"));
                    user.setIdentity(jsonObject.getInt("identity"));
                    startForegroundService(new Intent(StartActivity.this, ChatService.class));
                    switch (user.getIdentity()) {
                        case 1:
                            startForegroundService(new Intent(StartActivity.this, NewStaffService.class));
                            break;
                        case 2:
                            startForegroundService(new Intent(StartActivity.this, AssignNewBookingService.class));
                            break;
                        case 3:
                            startForegroundService(new Intent(StartActivity.this, NewBookingService.class));
                            break;
                    }
                    userDatabase.deleteDatabase();
                    userDatabase.insertUserData(username, password, user.getIdentity());
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}