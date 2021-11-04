package com.example.fyp;

import static com.example.fyp.StartActivity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

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

public class SignInActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText etLUsername, etLPassword;
    private TextInputLayout tipEtLUsername, tipEtLPassword;
    private CheckBox cbRmbMe;
    private Button btnSignIn, btnForgot, btnGoToSignUp;
    private ProgressBar pbLoading;
    private final UserDatabase userDatabase = new UserDatabase(SignInActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnBack = findViewById(R.id.btn_back);
        etLUsername = findViewById(R.id.et_LUsername);
        etLPassword = findViewById(R.id.et_LPassword);
        tipEtLUsername = findViewById(R.id.et_LUsernameLayout);
        tipEtLPassword = findViewById(R.id.et_LPasswordLayout);
        cbRmbMe = findViewById(R.id.cb_rmbMe);
        btnSignIn = findViewById(R.id.btn_SignIn);
        btnForgot = findViewById(R.id.btn_forgot);
        btnGoToSignUp = findViewById(R.id.btn_goToSignUp);
        pbLoading = findViewById(R.id.pb_loading);

        btnBack.setOnClickListener(view -> finish());

        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotpasswordpopup();
            }

            private void forgotpasswordpopup() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                AlertDialog alertDialog;
                View view = getLayoutInflater().inflate(R.layout.popup_forgotpassword, null);
                builder.setView(view);
                alertDialog = builder.create();
                alertDialog.show();

                TextInputLayout tilUsername, tilEmail;
                EditText etUsername, etEmail;
                Button btnSubmit;
                ProgressBar pbLoading;

                tilUsername = view.findViewById(R.id.til_username);
                tilEmail = view.findViewById(R.id.til_email);
                etUsername = view.findViewById(R.id.et_username);
                etEmail = view.findViewById(R.id.et_email);
                btnSubmit = view.findViewById(R.id.btn_submit);
                pbLoading = view.findViewById(R.id.pb_loading);

                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    class VerifyUser extends AsyncTask {
                        private String sresponse = "";
                        private boolean isConnectionFail;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

                            pbLoading.setVisibility(View.VISIBLE);
                            etUsername.setEnabled(false);
                            etEmail.setEnabled(false);
                            btnSubmit.setEnabled(false);
                        }

                        @Override
                        protected Object doInBackground(Object[] objects) {
                            OkHttpClient client = new OkHttpClient().newBuilder()
                                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                                    .build();
                            Request request = new Request.Builder()
                                    .url(Connection.getUrl() + "forgotpasswordverifyAPI.php?username=" + etUsername.getText().toString()
                                            + "&email=" + etEmail.getText().toString())
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

                            pbLoading.setVisibility(View.GONE);
                            etUsername.setEnabled(true);
                            etEmail.setEnabled(true);
                            btnSubmit.setEnabled(true);

                            if (isConnectionFail || sresponse.equals("Fail")) {
                                Toast.makeText(SignInActivity.this, "Connection failed. Please try again later.",
                                        Toast.LENGTH_SHORT).show();
                            } else if (sresponse.equals("Error")) {
                                tilEmail.setError("Invalid username or email.");
                            } else {
                                Toast.makeText(SignInActivity.this, "New password has been sent to your email",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onClick(View view) {
                        boolean isError = false;
                        tilUsername.setErrorEnabled(false);
                        tilEmail.setErrorEnabled(false);

                        if (etUsername.getText().toString().replace(" ", "").isEmpty()) {
                            tilUsername.setError("This field cannot be blank!");
                            isError = true;
                        }
                        if (etEmail.getText().toString().replace(" ", "").isEmpty()) {
                            tilEmail.setError("This field cannot be blank!");
                            isError = true;
                        } else if (!isEmailValid(etEmail.getText().toString())) {
                            tilEmail.setError("Invalid email address!");
                            isError = true;
                        }

                        if (!isError) {
                            VerifyUser verifyUser = new VerifyUser();
                            verifyUser.execute();
                        }
                    }

                    private boolean isEmailValid(CharSequence email) {
                        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
                    }
                });
            }
        });

        btnSignIn.setOnClickListener(view -> {
            boolean isError = false;

            tipEtLUsername.setErrorEnabled(false);
            tipEtLPassword.setErrorEnabled(false);

            if (etLUsername.getText().toString().replace(" ", "").isEmpty()) {
                tipEtLUsername.setError("This field cannot be blank!");
                isError = true;
            }
            if (etLPassword.getText().toString().replace(" ", "").isEmpty()) {
                tipEtLPassword.setError("This field cannot be blank!");
                isError = true;
            }

            if (!isError) {
                SignIn signIn = new SignIn(etLUsername.getText().toString(), etLPassword.getText().toString());
                signIn.execute();
            }
        });

        btnGoToSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
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
            etLUsername.setEnabled(false);
            etLPassword.setEnabled(false);
            cbRmbMe.setEnabled(false);
            btnSignIn.setEnabled(false);
            btnBack.setEnabled(false);
            btnGoToSignUp.setEnabled(false);
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
            } catch (IOException e) {
                isConnectionFail = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            etLUsername.setEnabled(true);
            etLPassword.setEnabled(true);
            cbRmbMe.setEnabled(true);
            btnSignIn.setEnabled(true);
            btnBack.setEnabled(true);
            btnGoToSignUp.setEnabled(true);
            pbLoading.setVisibility(View.GONE);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(SignInActivity.this, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                tipEtLPassword.setError("Invalid username or password");
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setEmail(jsonObject.getString("email"));
                    user.setFullName(jsonObject.getString("fullName"));
                    user.setGender(!jsonObject.getString("gender").equals("0"));
                    user.setAge(jsonObject.getInt("age"));
                    user.setAddress(jsonObject.getString("address"));
                    user.setState(jsonObject.getString("state"));
                    user.setLat(jsonObject.getDouble("lat"));
                    user.setLon(jsonObject.getDouble("lon"));
                    user.setMobile(jsonObject.getString("mobile"));
                    user.setProfilePic(Connection.getUrl() + jsonObject.getString("profilePic"));
                    user.setIdentity(jsonObject.getInt("identity"));
                    if (cbRmbMe.isChecked()) {
                        userDatabase.insertUserData(user.getUsername(), user.getPassword(), user.getIdentity());
                        startForegroundService(new Intent(SignInActivity.this, ChatService.class));
                        switch (user.getIdentity()) {
                            case 1:
                                startForegroundService(new Intent(SignInActivity.this, NewStaffService.class));
                                break;
                            case 2:
                                startForegroundService(new Intent(SignInActivity.this, AssignNewBookingService.class));
                                break;
                            case 3:
                                startForegroundService(new Intent(SignInActivity.this, NewBookingService.class));
                                break;
                        }
                    } else if (!cbRmbMe.isChecked()) {
                        userDatabase.deleteDatabase();
                    }

                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}