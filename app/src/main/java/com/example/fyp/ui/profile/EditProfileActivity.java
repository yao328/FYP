package com.example.fyp.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.SignUpActivity;
import com.example.fyp.StartActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private Button btnSave;
    private ImageView ivEditPic;
    private Uri ivEditPicUri;
    private TextInputLayout tipFullname, tipAge, tipMobile, tipAddress;
    private EditText etFullname, etAge, etMobile, etAddress;
    private Spinner spnState;
    private final String[] states = {"Select your state", "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Perak",
            "Perlis", "Pulau Pinang", "Sabah", "Sarawak", "Selangor", "Terengganu", "Wilayah Persekutuan Kuala Lumpur",
            "Labuan Federal Territory", "Putrajaya"};
    private ActivityResultLauncher<Intent> openCamera, selectImage, crop;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        ivEditPic = findViewById(R.id.iv_editPic);
        tipFullname = findViewById(R.id.tip_eFullName);
        tipAge = findViewById(R.id.tip_eAge);
        tipMobile = findViewById(R.id.tip_eMobile);
        tipAddress = findViewById(R.id.tip_eAddress);
        EditText etUsername = findViewById(R.id.et_eUsername);
        etFullname = findViewById(R.id.et_eFullName);
        etAge = findViewById(R.id.et_eAge);
        etMobile = findViewById(R.id.et_eMobile);
        etAddress = findViewById(R.id.et_eAddress);
        spnState = findViewById(R.id.spn_eState);
        pbLoading = findViewById(R.id.pb_loading);

        btnBack.setOnClickListener(view -> finish());

        Glide.with(EditProfileActivity.this)
                .load(StartActivity.user.getProfilePic())
                .into(ivEditPic);
        etUsername.setText(StartActivity.user.getUsername());
        etFullname.setText(StartActivity.user.getFullName());
        etAge.setText(String.valueOf(StartActivity.user.getAge()));
        etAddress.setText(StartActivity.user.getAddress());
        etMobile.setText(StartActivity.user.getMobile());

        selectImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                Uri oriUri = intent.getData();
                File croppedimg = new File(getCacheDir(), "cropped.jpg");
                try {
                    Files.deleteIfExists(Paths.get(croppedimg.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                crop.launch(UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1).getIntent(EditProfileActivity.this));
            }
        });

        openCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                Uri oriUri = getImageUri(EditProfileActivity.this, bitmap);
                File croppedimg = new File(getCacheDir(), "cropped.jpg");
                try {
                    Files.deleteIfExists(Paths.get(croppedimg.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                crop.launch(UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1).getIntent(EditProfileActivity.this));
            }
        });

        crop = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                ivEditPic.setImageURI(null);
                ivEditPicUri = UCrop.getOutput(intent);
                ivEditPic.setImageURI(ivEditPicUri);
            }
        });

        ivEditPic.setOnClickListener(view -> uploadPopup());

        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(EditProfileActivity.this,
                android.R.layout.simple_spinner_dropdown_item, states);
        spnState.setAdapter(stringArrayAdapter);
        for (int i = 0; i < states.length; i++) {
            if (StartActivity.user.getState().equals(states[i])) {
                spnState.setSelection(i);
            }
        }

        etMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                if (!etMobile.getText().toString().startsWith("+60")) {
                    etMobile.setText("+60");
                }
            }
        });

        btnSave.setOnClickListener(view -> {
            boolean isError = false;

            tipFullname.setErrorEnabled(false);
            tipAge.setErrorEnabled(false);
            tipAddress.setErrorEnabled(false);
            tipMobile.setErrorEnabled(false);

            if (etFullname.getText().toString().replace(" ", "").isEmpty()) {
                tipFullname.setError("This field cannot be blank!");
                isError = true;
            }
            if (etAge.getText().toString().replace(" ", "").isEmpty()) {
                tipAge.setError("This field cannot be blank!");
                isError = true;
            } else if (Integer.parseInt(etAge.getText().toString()) > 100 || Integer.parseInt(etAge.getText().toString()) < 18) {
                tipAge.setError("Age must be between 18 and 100!");
                isError = true;
            }
            if (etAddress.getText().toString().replace(" ", "").isEmpty()) {
                tipAddress.setError("This field cannot be blank!");
                isError = true;
            } else if (spnState.getSelectedItem().toString().equals(states[0])) {
                tipAddress.setError("Please select your state!");
                isError = true;
            }
            if (etMobile.getText().toString().equals("+60")) {
                tipMobile.setError("This field cannot be blank!");
                isError = true;
            } else if (etMobile.getText().toString().length() < 11) {
                tipMobile.setError("Invalid format");
                isError = true;
            }

            if (!isError) {
                EditProfile editProfile = new EditProfile(StartActivity.user.getUsername(), etFullname.getText().toString(),
                        Integer.parseInt(etAge.getText().toString()), etAddress.getText().toString(),
                        spnState.getSelectedItem().toString(), etMobile.getText().toString(), ivEditPicUri);
                editProfile.execute();
            }
        });
    }

    private void uploadPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        final AlertDialog alertDialog;
        final View view = getLayoutInflater().inflate(R.layout.popup_uploadimg, null);
        Button btnOpenCamera, btnSelectImg, btnUploadImgCan;

        btnOpenCamera = view.findViewById(R.id.btn_openCamera);
        btnSelectImg = view.findViewById(R.id.btn_selectImg);
        btnUploadImgCan = view.findViewById(R.id.btn_uploadImgCan);

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.show();

        btnOpenCamera.setOnClickListener(view1 -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            openCamera.launch(intent);
            alertDialog.dismiss();
        });

        btnSelectImg.setOnClickListener(view12 -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            selectImage.launch(intent);
            alertDialog.dismiss();
        });

        btnUploadImgCan.setOnClickListener(view13 -> alertDialog.dismiss());
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SS:aa");
        simpleDateFormat.setTimeZone(timeZone);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, simpleDateFormat.format(calendar.getTime()), null);
        return Uri.parse(path);
    }

    private String getPath(Uri imgUri) {
        if (imgUri == null) {
            return null;
        } else {
            String[] projection = {MediaStore.Images.Media.DATA};
            @SuppressLint("Recycle")
            Cursor cursor = getContentResolver().query(imgUri, projection, null, null, null);

            if (cursor != null) {
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                return cursor.getString(col_index);
            }
        }
        return imgUri.getPath();
    }

    @SuppressLint("StaticFieldLeak")
    private class EditProfile extends AsyncTask {
        private final String username, fullName, address, state, mobile;
        private final int age;
        private final Uri profilePicUri;
        private String sresponse="";
        private boolean isConnectionFail;

        public EditProfile(String username, String fullName, int age, String address, String state, String mobile, Uri profilePicUri) {
            this.username = username;
            this.fullName = fullName;
            this.age = age;
            this.address = address;
            this.state = state;
            this.mobile = mobile;
            this.profilePicUri = profilePicUri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            btnBack.setEnabled(false);
            btnSave.setEnabled(false);
            ivEditPic.setEnabled(false);
            etFullname.setEnabled(false);
            etAge.setEnabled(false);
            etMobile.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            if (profilePicUri != null) {
                @SuppressLint("SdCardPath")
                File file = new File(getPath(profilePicUri).replace("/storage/emulated/0/", "/sdcard/"));
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("username", username)
                        .addFormDataPart("fullName", fullName)
                        .addFormDataPart("age", String.valueOf(age))
                        .addFormDataPart("address", address)
                        .addFormDataPart("state", state)
                        .addFormDataPart("mobile", mobile)
                        .addFormDataPart("profilePic", file.getName(),
                                RequestBody.create(MediaType.parse("application/octet-stream"),
                                        new File(file.getPath())))
                        .build();
                Request request = new Request.Builder()
                        .url(Connection.getUrl() + "editprofileAPI.php")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    sresponse = Objects.requireNonNull(response.body()).string();
                } catch (IOException e) {
                    e.printStackTrace();
                    isConnectionFail = true;
                }
            } else {
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("username", username)
                        .addFormDataPart("fullName", fullName)
                        .addFormDataPart("age", String.valueOf(age))
                        .addFormDataPart("address", address)
                        .addFormDataPart("state", state)
                        .addFormDataPart("mobile", mobile)
                        .build();
                Request request = new Request.Builder()
                        .url(Connection.getUrl() + "editprofilenopicAPI.php")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    sresponse = Objects.requireNonNull(response.body()).string();
                } catch (IOException e) {
                    e.printStackTrace();
                    isConnectionFail = true;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            pbLoading.setVisibility(View.GONE);
            btnBack.setEnabled(true);
            btnSave.setEnabled(true);
            ivEditPic.setEnabled(true);
            etFullname.setEnabled(true);
            etAge.setEnabled(true);
            etMobile.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(EditProfileActivity.this, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(EditProfileActivity.this, "Fail to edit profile. ", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("OK")) {
                Toast.makeText(EditProfileActivity.this, "Done editing your profile.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProfileActivity.this, StartActivity.class));
            }
        }
    }
}