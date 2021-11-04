package com.example.fyp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class SignUpActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private String state = "";
    private ImageView ivUploadimg;
    private Uri imgUri;
    private SwitchCompat swGender;
    private EditText etRUsername, etRPassword, etRConpassword, etREmail, etRFullname, etRAge, etRAddress, etRMobile;
    private TextInputLayout tipUsername, tipPassword, tipConpassword, tipEmail, tipFullname, tipAge, tipAddress, tipMobile;
    private Spinner spnState;
    private final String[] states = {"Select your state", "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Perak",
            "Perlis", "Pulau Pinang", "Sabah", "Sarawak", "Selangor", "Terengganu", "Wilayah Persekutuan Kuala Lumpur",
            "Labuan Federal Territory", "Putrajaya"};
    private ImageButton btnTrack;
    private Button btnSignup;
    private ProgressBar pbLoading;
    private ActivityResultLauncher<Intent> openCamera, selectImage;

    @SuppressLint({"SetTextI18n", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnBack = findViewById(R.id.btn_back);
        ivUploadimg = findViewById(R.id.iv_uploadImg);
        swGender = findViewById(R.id.sw_gender);
        etRUsername = findViewById(R.id.et_RUsername);
        etRPassword = findViewById(R.id.et_RPassword);
        etRConpassword = findViewById(R.id.et_RConPassword);
        etREmail = findViewById(R.id.et_REmail);
        etRFullname = findViewById(R.id.et_RFullName);
        etRAge = findViewById(R.id.et_RAge);
        etRAddress = findViewById(R.id.et_RAddress);
        etRMobile = findViewById(R.id.et_RMobile);
        tipUsername = findViewById(R.id.usernamelayout);
        tipPassword = findViewById(R.id.passwordlayout);
        tipConpassword = findViewById(R.id.conpasswordlayout);
        tipEmail = findViewById(R.id.emaillayout);
        tipFullname = findViewById(R.id.fullnamelayout);
        tipAge = findViewById(R.id.agelayout);
        tipAddress = findViewById(R.id.addresslayout);
        tipMobile = findViewById(R.id.mobilelayout);
        spnState = findViewById(R.id.spn_state);
        btnTrack = findViewById(R.id.btn_track);
        btnSignup = findViewById(R.id.btn_signup);
        pbLoading = findViewById(R.id.pb_loading);

        btnBack.setOnClickListener(view -> finish());

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
                UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1)
                        .start(SignUpActivity.this);
            }
        });

        openCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                Uri oriUri = getImageUri(SignUpActivity.this, bitmap);
                File croppedimg = new File(getCacheDir(), "cropped.jpg");
                try {
                    Files.deleteIfExists(Paths.get(croppedimg.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1)
                        .start(SignUpActivity.this);
            }
        });

        ivUploadimg.setOnClickListener(view -> {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            if (!EasyPermissions.hasPermissions(SignUpActivity.this, permissions)) {
                EasyPermissions.requestPermissions(SignUpActivity.this, "Allow this application to access your gallery?", 0, permissions);
            } else {
                uploadImgPopup();
            }
        });

        swGender.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                compoundButton.setText("Male");
            } else {
                compoundButton.setText("Female");
            }
        });

        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(SignUpActivity.this,
                android.R.layout.simple_spinner_dropdown_item, states);
        spnState.setAdapter(stringArrayAdapter);

        btnTrack.setOnClickListener(view -> {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (!EasyPermissions.hasPermissions(SignUpActivity.this, perms)) {
                EasyPermissions.requestPermissions(SignUpActivity.this, "Allow this app to use location?", 0, perms);
            } else {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(SignUpActivity.this, "You must turn on GPS!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpActivity.this, "Getting your location...", Toast.LENGTH_SHORT).show();
                    pbLoading.setVisibility(View.VISIBLE);
                    btnBack.setEnabled(false);
                    ivUploadimg.setEnabled(false);
                    swGender.setEnabled(false);
                    etRUsername.setEnabled(false);
                    etRPassword.setEnabled(false);
                    etRConpassword.setEnabled(false);
                    etREmail.setEnabled(false);
                    etRFullname.setEnabled(false);
                    etRAge.setEnabled(false);
                    etRAddress.setEnabled(false);
                    btnTrack.setEnabled(false);
                    etRMobile.setEnabled(false);
                    btnSignup.setEnabled(false);
                    FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SignUpActivity.this);
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(SignUpActivity.this, Locale.getDefault());
                            String address;
                            try {
                                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                address = addressList.get(0).getAddressLine(0);
                                state = addressList.get(0).getAdminArea();
                                pbLoading.setVisibility(View.GONE);
                                btnBack.setEnabled(true);
                                ivUploadimg.setEnabled(true);
                                swGender.setEnabled(true);
                                etRUsername.setEnabled(true);
                                etRPassword.setEnabled(true);
                                etRConpassword.setEnabled(true);
                                etREmail.setEnabled(true);
                                etRFullname.setEnabled(true);
                                etRAge.setEnabled(true);
                                etRAddress.setEnabled(true);
                                btnTrack.setEnabled(true);
                                etRMobile.setEnabled(true);
                                btnSignup.setEnabled(true);
                                etRAddress.setText(address);
                                for (int i = 0; i < states.length; i++) {
                                    if (state.equals(states[i])) {
                                        spnState.setSelection(i);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        etRMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().startsWith("+60")) {
                    etRMobile.setText("+60");
                }
            }
        });

        btnSignup.setOnClickListener(view -> {
            boolean isError = false;

            tipUsername.setErrorEnabled(false);
            tipPassword.setErrorEnabled(false);
            tipConpassword.setErrorEnabled(false);
            tipEmail.setErrorEnabled(false);
            tipFullname.setErrorEnabled(false);
            tipAge.setErrorEnabled(false);
            tipAddress.setErrorEnabled(false);
            tipMobile.setErrorEnabled(false);

            if (ivUploadimg.getTag().equals("original")) {
                isError = true;
                Toast.makeText(SignUpActivity.this, "Please upload image.", Toast.LENGTH_SHORT).show();
            }
            if (etRUsername.getText().toString().replace(" ", "").isEmpty()) {
                tipUsername.setError("This field cannot be blank!");
                isError = true;
            }
            if (etRPassword.getText().toString().replace(" ", "").isEmpty()) {
                tipPassword.setError("This field cannot be blank!");
                isError = true;
            } else if (etRPassword.getText().toString().length() < 10) {
                tipPassword.setError("Password too short. Must be at least 10 characters!");
                isError = true;
            }
            if (etRConpassword.getText().toString().replace(" ", "").isEmpty()) {
                tipConpassword.setError("This field cannot be blank!");
                isError = true;
            } else if (!etRConpassword.getText().toString().equals(etRPassword.getText().toString())) {
                tipConpassword.setError("Password does not match!");
                isError = true;
            }
            if (etREmail.getText().toString().replace(" ", "").isEmpty()) {
                tipEmail.setError("This field cannot be blank!");
                isError = true;
            } else if (!isEmailValid(etREmail.getText().toString())) {
                tipEmail.setError("Invalid email address!");
                isError = true;
            }
            if (etRFullname.getText().toString().replace(" ", "").isEmpty()) {
                tipFullname.setError("This field cannot be blank!");
                isError = true;
            }
            if (etRAge.getText().toString().replace(" ", "").isEmpty()) {
                tipAge.setError("This field cannot be blank!");
                isError = true;
            } else if (Integer.parseInt(etRAge.getText().toString()) > 100 || Integer.parseInt(etRAge.getText().toString()) < 18) {
                tipAge.setError("Age must be between 18 and 100!");
                isError = true;
            }
            if (etRAddress.getText().toString().replace(" ", "").isEmpty()) {
                tipAddress.setError("This field cannot be blank!");
                isError = true;
            } else if (spnState.getSelectedItem().toString().equals(states[0])) {
                tipAddress.setError("Please select your state!");
                isError = true;
            }
            if (etRMobile.getText().toString().equals("+60")) {
                tipMobile.setError("This field cannot be blank!");
                isError = true;
            } else if (etRMobile.getText().toString().length() < 11) {
                tipMobile.setError("Invalid format");
                isError = true;
            }

            if (!isError) {
                Register register = new Register(etRUsername.getText().toString(), etRPassword.getText().toString(),
                        etREmail.getText().toString(), etRFullname.getText().toString(), swGender.isChecked(),
                        Integer.parseInt(etRAge.getText().toString()), etRAddress.getText().toString(),
                        spnState.getSelectedItem().toString(), etRMobile.getText().toString(), imgUri);
                register.execute();
            }
        });
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

    public void uploadImgPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            imgUri = UCrop.getOutput(data);
            ivUploadimg.setImageURI(null);
            ivUploadimg.setImageURI(imgUri);
            ivUploadimg.setTag("uploaded");
            Log.e("cropped path", getPath(imgUri));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, SignUpActivity.this);
    }

    private String getPath(Uri imgUri) {
        if (imgUri == null) {
            return null;
        } else {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(imgUri, projection, null, null, null);

            if (cursor != null) {
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                cursor.close();

                return cursor.getString(col_index);
            }
        }
        return imgUri.getPath();
    }

    @SuppressLint("StaticFieldLeak")
    private class Register extends AsyncTask {
        private final String username, password, email, fullname, address, mobile, state;
        private final int age;
        private final boolean gender;
        private final Uri imgUri;
        private String sresponse = "";
        private boolean isConnectionFail;

        public Register(String username, String password, String email, String fullname, boolean gender, int age,
                        String address, String state, String mobile, Uri imgUri) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.fullname = fullname;
            this.gender = gender;
            this.age = age;
            this.address = address;
            this.state = state;
            this.mobile = mobile;
            this.imgUri = imgUri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            btnBack.setEnabled(false);
            ivUploadimg.setEnabled(false);
            swGender.setEnabled(false);
            etRUsername.setEnabled(false);
            etRPassword.setEnabled(false);
            etRConpassword.setEnabled(false);
            etREmail.setEnabled(false);
            etRFullname.setEnabled(false);
            etRAge.setEnabled(false);
            etRAddress.setEnabled(false);
            btnTrack.setEnabled(false);
            etRMobile.setEnabled(false);
            btnSignup.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String sgender;
            if (gender) {
                sgender = "1";
            } else {
                sgender = "0";
            }

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            @SuppressLint("SdCardPath")
            File file = new File(getPath(imgUri).replace("/storage/emulated/0/", "/sdcard/"));
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("password", password)
                    .addFormDataPart("email", email)
                    .addFormDataPart("fullName", fullname)
                    .addFormDataPart("gender", sgender)
                    .addFormDataPart("age", String.valueOf(age))
                    .addFormDataPart("address", address)
                    .addFormDataPart("state", state)
                    .addFormDataPart("mobile", mobile)
                    .addFormDataPart("profilePic", file.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(file.getPath())))
                    .addFormDataPart("identity", "1")
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "registerAPI.php")
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
            ivUploadimg.setEnabled(true);
            swGender.setEnabled(true);
            etRUsername.setEnabled(true);
            etRPassword.setEnabled(true);
            etRConpassword.setEnabled(true);
            etREmail.setEnabled(true);
            etRFullname.setEnabled(true);
            etRAge.setEnabled(true);
            etRAddress.setEnabled(true);
            btnTrack.setEnabled(true);
            etRMobile.setEnabled(true);
            btnSignup.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(SignUpActivity.this, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(SignUpActivity.this, "This username or email has been used. ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignUpActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}