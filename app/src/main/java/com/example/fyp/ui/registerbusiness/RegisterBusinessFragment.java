package com.example.fyp.ui.registerbusiness;

import static android.content.Context.LOCATION_SERVICE;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;
import com.vanniktech.emoji.EmojiPopup;
import com.yalantis.ucrop.UCrop;

import org.apache.commons.text.StringEscapeUtils;

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

public class RegisterBusinessFragment extends Fragment {

    private ImageView ivUploadshopimg;
    private Uri imgUri;
    private EditText etRShopname, etRShopPayment, etRShopMobile, etRShopAddress, etShopDescription;
    private TextInputLayout tipShopname, tipShopType, tipShopPayment, tipMobile, tipAddress, tipDescription;
    private Spinner spnType, spnState;
    private final String[] shopTypes = {"Select shop type", "House", "Car", "Beauty", "Medical", "Education", "Pet", "Other"};
    private final String[] states = {"Select your state", "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Perak",
            "Perlis", "Pulau Pinang", "Sabah", "Sarawak", "Selangor", "Terengganu", "Wilayah Persekutuan Kuala Lumpur",
            "Labuan Federal Territory", "Putrajaya"};
    private Button btnSignup;
    private ImageButton btnTrack;
    private ProgressBar pbLoading;
    private ActivityResultLauncher<Intent> openCamera, selectImage, crop;
    private String state = "";

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StartActivity.isStart = false;
        View view = inflater.inflate(R.layout.fragment_register_business, container, false);

        ivUploadshopimg = view.findViewById(R.id.iv_uploadShopImg);
        etRShopname = view.findViewById(R.id.et_RShopName);
        etRShopPayment = view.findViewById(R.id.et_RShopPayment);
        etRShopMobile = view.findViewById(R.id.et_RShopMobile);
        etRShopAddress = view.findViewById(R.id.et_RShopAddress);
        spnState = view.findViewById(R.id.spn_state);
        btnTrack = view.findViewById(R.id.btn_track);
        etShopDescription = view.findViewById(R.id.et_RShopDescription);
        tipShopname = view.findViewById(R.id.shopnamelayout);
        tipShopType = view.findViewById(R.id.shoptypelayout);
        tipShopPayment = view.findViewById(R.id.shoppaymentlayout);
        tipMobile = view.findViewById(R.id.shopmobilelayout);
        tipAddress = view.findViewById(R.id.shopaddresslayout);
        tipDescription = view.findViewById(R.id.shopdescriptionlayout);
        spnType = view.findViewById(R.id.spn_shopType);
        ImageButton btnEmoji = view.findViewById(R.id.btn_emoji);
        btnSignup = view.findViewById(R.id.btn_signup2);
        pbLoading = view.findViewById(R.id.pb_loading);

        selectImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                Uri oriUri = intent.getData();
                File croppedimg = new File(requireContext().getCacheDir(), "cropped.jpg");
                try {
                    Files.deleteIfExists(Paths.get(croppedimg.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                crop.launch(UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1).getIntent(requireContext()));
            }
        });

        openCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                Uri oriUri = getImageUri(requireContext(), bitmap);
                File croppedimg = new File(requireContext().getCacheDir(), "cropped.jpg");
                try {
                    Files.deleteIfExists(Paths.get(croppedimg.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                crop.launch(UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1).getIntent(requireContext()));
            }
        });

        crop = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            imgUri = UCrop.getOutput(Objects.requireNonNull(result.getData()));
            ivUploadshopimg.setImageURI(null);
            ivUploadshopimg.setImageURI(imgUri);
            ivUploadshopimg.setTag("uploaded");
            Log.e("cropped path", getPath(imgUri));
        });

        ivUploadshopimg.setOnClickListener(view1 -> {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA};
            if (!EasyPermissions.hasPermissions(requireContext(), perms)) {
                EasyPermissions.requestPermissions(requireActivity(), "Allow this app to use camera and read / write storage?",
                        0, perms);
            } else {
                uploadImgPopup();
            }
        });

        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, shopTypes);
        spnType.setAdapter(stringArrayAdapter);

        etRShopMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                if (!etRShopMobile.getText().toString().startsWith("+60")) {
                    etRShopMobile.setText("+60");
                }
            }
        });

        ArrayAdapter<String> stringArrayAdapter1 = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, states);
        spnState.setAdapter(stringArrayAdapter1);

        btnTrack.setOnClickListener(view12 -> {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (!EasyPermissions.hasPermissions(requireContext(), perms)) {
                EasyPermissions.requestPermissions(requireActivity(), "Allow this app to use location?", 0, perms);
            } else {
                LocationManager locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(getContext(), "You must turn on GPS!", Toast.LENGTH_SHORT).show();
                } else {
                    etRShopAddress.setText("");
                    Toast.makeText(getContext(), "Getting your location...", Toast.LENGTH_SHORT).show();
                    pbLoading.setVisibility(View.VISIBLE);
                    ivUploadshopimg.setEnabled(false);
                    etRShopname.setEnabled(false);
                    spnType.setEnabled(false);
                    etRShopPayment.setEnabled(false);
                    etRShopMobile.setEnabled(false);
                    etRShopAddress.setEnabled(false);
                    spnState.setEnabled(false);
                    btnTrack.setEnabled(false);
                    etShopDescription.setEnabled(false);
                    btnSignup.setEnabled(false);
                    FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            String address;
                            try {
                                List<Address> addressList = geocoder.getFromLocation(lat, lon, 1);
                                address = addressList.get(0).getAddressLine(0);
                                state = addressList.get(0).getAdminArea();
                                pbLoading.setVisibility(View.GONE);
                                ivUploadshopimg.setEnabled(true);
                                etRShopname.setEnabled(true);
                                spnType.setEnabled(true);
                                etRShopPayment.setEnabled(true);
                                etRShopMobile.setEnabled(true);
                                etRShopAddress.setEnabled(true);
                                spnState.setEnabled(true);
                                btnTrack.setEnabled(true);
                                etShopDescription.setEnabled(true);
                                btnSignup.setEnabled(true);
                                etRShopAddress.setText(address);
                                Log.e("state", state);
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

        EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(view).build(etShopDescription);
        btnEmoji.setOnClickListener(view13 -> emojiPopup.toggle());

        btnSignup.setOnClickListener(view14 -> {
            boolean isError = false;

            tipShopname.setErrorEnabled(false);
            tipShopType.setErrorEnabled(false);
            tipShopPayment.setErrorEnabled(false);
            tipMobile.setErrorEnabled(false);
            tipAddress.setErrorEnabled(false);
            tipDescription.setErrorEnabled(false);

            if (ivUploadshopimg.getTag().equals("original")) {
                isError = true;
                Toast.makeText(getContext(), "Please upload image.", Toast.LENGTH_SHORT).show();
            }
            if (etRShopname.getText().toString().replace(" ", "").isEmpty()) {
                tipShopname.setError("This field cannot be blank!");
                isError = true;
            }
            if (spnType.getSelectedItem().toString().equals(shopTypes[0])) {
                tipShopType.setError("This field cannot be blank!");
                isError = true;
            }
            if (etRShopPayment.getText().toString().replace(" ", "").isEmpty()) {
                tipShopPayment.setError("This field cannot be blank!");
                isError = true;
            }
            if (etRShopMobile.getText().toString().equals("+60")) {
                tipMobile.setError("This field cannot be blank!");
                isError = true;
            } else if (etRShopMobile.getText().toString().length() < 11) {
                tipMobile.setError("Invalid format");
                isError = true;
            }
            if (etRShopAddress.getText().toString().replace(" ", "").isEmpty()) {
                tipAddress.setError("This field cannot be blank!");
                isError = true;
            } else if (spnState.getSelectedItem().toString().equals(states[0])) {
                tipAddress.setError("Please select your shop's state!");
                isError = true;
            }
            if (etShopDescription.getText().toString().replace(" ", "").isEmpty()) {
                tipDescription.setError("This field cannot be blank!");
                isError = true;
            }

            if (!isError) {
                Register register = new Register(StartActivity.user.getUsername(), imgUri, etRShopname.getText().toString(),
                        spnType.getSelectedItem().toString(), etRShopPayment.getText().toString(), etRShopMobile.getText().toString(),
                        etRShopAddress.getText().toString(), spnState.getSelectedItem().toString(), StringEscapeUtils.escapeJava(etShopDescription.getText().toString()).replace("\\", "SLASH"));
                register.execute();
            }
        });
        return view;
    }

    private void uploadImgPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final AlertDialog alertDialog;
        final View view = getLayoutInflater().inflate(R.layout.popup_uploadimg, null);
        Button btnOpenCamera, btnSelectImg, btnUploadImgCan;

        btnOpenCamera = view.findViewById(R.id.btn_openCamera);
        btnSelectImg = view.findViewById(R.id.btn_selectImg);
        btnUploadImgCan = view.findViewById(R.id.btn_uploadImgCan);

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
            Cursor cursor = requireActivity().getContentResolver().query(imgUri, projection, null, null, null);

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
        private final String username, shopName, type, payment, mobile, address, state, description;
        private final Uri imgUri;
        private String sresponse = "";
        private boolean isConnectionFail;

        public Register(String username, Uri imgUri, String shopName, String type, String payment, String mobile, String address, String state, String description) {
            this.username = username;
            this.imgUri = imgUri;
            this.shopName = shopName;
            this.type = type;
            this.payment = payment;
            this.mobile = mobile;
            this.address = address;
            this.state = state;
            this.description = description;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLoading.setVisibility(View.VISIBLE);
            ivUploadshopimg.setEnabled(false);
            etRShopname.setEnabled(false);
            spnType.setEnabled(false);
            etRShopPayment.setEnabled(false);
            etRShopMobile.setEnabled(false);
            etRShopAddress.setEnabled(false);
            spnState.setEnabled(false);
            btnTrack.setEnabled(false);
            etShopDescription.setEnabled(false);
            btnSignup.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            @SuppressLint("SdCardPath")
            File file = new File(getPath(imgUri).replace("/storage/emulated/0/", "/sdcard/"));
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("logo", file.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(file.getPath())))
                    .addFormDataPart("username", username)
                    .addFormDataPart("shopName", shopName)
                    .addFormDataPart("type", type)
                    .addFormDataPart("payment", payment)
                    .addFormDataPart("mobile", mobile)
                    .addFormDataPart("address", address)
                    .addFormDataPart("state", state)
                    .addFormDataPart("description", description)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "registershopAPI.php")
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
            ivUploadshopimg.setEnabled(true);
            etRShopname.setEnabled(true);
            spnType.setEnabled(true);
            etRShopMobile.setEnabled(true);
            etRShopAddress.setEnabled(true);
            spnState.setEnabled(true);
            btnTrack.setEnabled(true);
            etShopDescription.setEnabled(true);
            btnSignup.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(getContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(getContext(), "Fail to register shop.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("OK")) {
                Toast.makeText(getContext(), "Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), StartActivity.class));
            }
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

}