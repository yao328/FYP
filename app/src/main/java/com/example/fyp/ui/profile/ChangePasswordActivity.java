package com.example.fyp.ui.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;
import com.example.fyp.UserDatabase;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextInputLayout tipNew, tipCurr, tipCon;
    private EditText etCurrPass, etNewPass, etConPass;
    private Button btnChange;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        btnBack = findViewById(R.id.btn_back);
        tipNew = findViewById(R.id.tip_enewpassword);
        tipCurr = findViewById(R.id.tip_ecurrentpassword);
        tipCon = findViewById(R.id.tip_econpassword);
        etCurrPass = findViewById(R.id.et_eCurrPassword);
        etNewPass = findViewById(R.id.et_eNewPassword);
        etConPass = findViewById(R.id.et_eConPassword);
        btnChange = findViewById(R.id.btn_changePassword);
        pbLoading = findViewById(R.id.pb_loading);

        btnBack.setOnClickListener(view -> finish());

        btnChange.setOnClickListener(view -> {
            boolean isError = false;
            tipCurr.setErrorEnabled(false);
            tipNew.setErrorEnabled(false);
            tipCon.setErrorEnabled(false);

            if (etCurrPass.getText().toString().replace(" ", "").isEmpty()) {
                tipCurr.setError("This field cannot be blank!");
                isError = true;
            }
            if (etNewPass.getText().toString().replace(" ", "").isEmpty()) {
                tipNew.setError("This field cannot be blank!");
                isError = true;
            } else if (etNewPass.getText().toString().length() < 10) {
                tipNew.setError("Password too short. Must be at least 10 characters!");
                isError = true;
            }
            if (etConPass.getText().toString().replace(" ", "").isEmpty()) {
                tipCon.setError("This field cannot be blank!");
                isError = true;
            } else if (!etNewPass.getText().toString().equals(etConPass.getText().toString())) {
                tipCon.setError("Password does not match!");
                isError = true;
            }

            if (!isError) {
                ChangePass changePass = new ChangePass(StartActivity.user.getUsername(), etCurrPass.getText().toString(),
                        etNewPass.getText().toString());
                changePass.execute();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class ChangePass extends AsyncTask {
        private final String username, oldpass, newpass;
        private String sresponse="";
        private boolean isConnectionFail;

        public ChangePass(String username, String oldpass, String newpass) {
            this.username = username;
            this.oldpass = oldpass;
            this.newpass = newpass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            btnBack.setEnabled(false);
            etCurrPass.setEnabled(false);
            etNewPass.setEnabled(false);
            etConPass.setEnabled(false);
            btnChange.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("oldpassword", oldpass)
                    .addFormDataPart("newpassword", newpass)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "changepassAPI.php")
                    .method("POST", body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
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
            btnBack.setEnabled(true);
            etCurrPass.setEnabled(true);
            etNewPass.setEnabled(true);
            etConPass.setEnabled(true);
            btnChange.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ChangePasswordActivity.this, "Connection failed. Please try again later.",
                        Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(ChangePasswordActivity.this, "Error!",
                        Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Wrong pass")) {
                tipCurr.setError("Wrong password!");
            } else if (sresponse.equals("OK")) {
                UserDatabase userDatabase = new UserDatabase(ChangePasswordActivity.this);
                userDatabase.deleteDatabase();
                userDatabase.insertUserData(username, newpass, StartActivity.user.getIdentity());
                Toast.makeText(ChangePasswordActivity.this, "Done changing your password.",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ChangePasswordActivity.this, StartActivity.class));

            }
        }
    }
}