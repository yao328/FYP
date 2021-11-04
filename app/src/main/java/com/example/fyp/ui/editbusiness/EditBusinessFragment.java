package com.example.fyp.ui.editbusiness;

import android.Manifest;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.SetListViewHeight;
import com.example.fyp.StartActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.vanniktech.emoji.EmojiPopup;
import com.yalantis.ucrop.UCrop;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import pub.devrel.easypermissions.EasyPermissions;

public class EditBusinessFragment extends Fragment {

    private ImageView ivELogo;
    private TextInputLayout tipeshopname, tipeshopmobile, tipetype, tipepayment, tipeshopaddress, tipeshopstate, tipedescription;
    private EditText etEShopName, etEShopPayment, etEShopMobile, etEShopAddress, etEDescription;
    private Spinner spnType, spnShopState;
    private final String[] shopTypes = {"Select shop type", "House", "Car", "Beauty", "Medical", "Education", "Pet", "Other"};
    private final String[] states = {"Select your state", "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Perak",
            "Perlis", "Pulau Pinang", "Sabah", "Sarawak", "Selangor", "Terengganu", "Wilayah Persekutuan Kuala Lumpur",
            "Labuan Federal Territory", "Putrajaya"};
    private ImageButton btnEmoji;
    private ListView lvServices;
    private FloatingActionButton btnAddService;
    private Button btnSaveService;
    private ProgressBar pbLoading;
    private ActivityResultLauncher<Intent> openCamera, selectImage, crop;
    private Uri imgUri;
    private int shopID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_business, container, false);

        ivELogo = view.findViewById(R.id.iv_eLogo);
        tipeshopname = view.findViewById(R.id.tipeshopname);
        tipeshopmobile = view.findViewById(R.id.tipeshopmobile);
        tipetype = view.findViewById(R.id.tipetype);
        tipepayment = view.findViewById(R.id.tipeshoppayment);
        tipeshopaddress = view.findViewById(R.id.tipeshopaddress);
        tipeshopstate = view.findViewById(R.id.tipeshopstate);
        tipedescription = view.findViewById(R.id.tipedescription);
        etEShopName = view.findViewById(R.id.et_eShopName);
        etEShopPayment = view.findViewById(R.id.et_eShopPayment);
        etEShopMobile = view.findViewById(R.id.et_eShopMobile);
        etEShopAddress = view.findViewById(R.id.et_eShopAddress);
        etEDescription = view.findViewById(R.id.et_eShopDescription);
        spnType = view.findViewById(R.id.spn_eType);
        spnShopState = view.findViewById(R.id.spn_eShopState);
        btnEmoji = view.findViewById(R.id.btn_emoji);
        lvServices = view.findViewById(R.id.lv_services);
        btnAddService = view.findViewById(R.id.btn_addService);
        btnSaveService = view.findViewById(R.id.btn_saveService);
        pbLoading = view.findViewById(R.id.pb_loading);

        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, shopTypes);
        spnType.setAdapter(stringArrayAdapter);

        ArrayAdapter<String> stringArrayAdapter1 = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, states);
        spnShopState.setAdapter(stringArrayAdapter1);

        EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(view).build(etEDescription);
        btnEmoji.setOnClickListener(view12 -> emojiPopup.toggle());

        GetShopData getShopData = new GetShopData(StartActivity.user.getUsername());
        getShopData.execute();

        etEShopMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                if (!etEShopMobile.getText().toString().startsWith("+60")) {
                    etEShopMobile.setText("+60");
                }
            }
        });

        btnAddService.setOnClickListener(view1 -> addservicepopup());

        ivELogo.setOnClickListener(view1 -> {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            if (!EasyPermissions.hasPermissions(requireContext(), permissions)) {
                EasyPermissions.requestPermissions(requireActivity(), "Allow this application to access your gallery?", 0, permissions);
            } else {
                uploadImgPopup();
            }
        });

        selectImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                Uri oriUri = intent.getData();
                File croppedimg = new File(requireActivity().getCacheDir(), "cropped.jpg");
                try {
                    Files.deleteIfExists(Paths.get(croppedimg.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                crop.launch(UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1)
                        .getIntent(requireContext()));
            }
        });

        openCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if (intent != null) {
                Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                Uri oriUri = getImageUri(requireContext(), bitmap);
                File croppedimg = new File(requireActivity().getCacheDir(), "cropped.jpg");
                try {
                    Files.deleteIfExists(Paths.get(croppedimg.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                crop.launch(UCrop.of(oriUri, Uri.fromFile(croppedimg))
                        .withAspectRatio(1, 1)
                        .getIntent(requireContext()));

            }
        });

        crop = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            assert result.getData() != null;
            imgUri = UCrop.getOutput(result.getData());
            ivELogo.setImageURI(null);
            ivELogo.setImageURI(imgUri);
            ivELogo.setTag("uploaded");
        });

        btnSaveService.setOnClickListener(view1 -> {
            boolean isError = false;

            tipeshopname.setErrorEnabled(false);
            tipeshopmobile.setErrorEnabled(false);
            tipetype.setErrorEnabled(false);
            tipepayment.setErrorEnabled(false);
            tipeshopaddress.setErrorEnabled(false);
            tipeshopstate.setErrorEnabled(false);
            tipedescription.setErrorEnabled(false);

            if (etEShopName.getText().toString().replace(" ", "").isEmpty()) {
                tipeshopname.setError("This field cannot be blank!");
                isError = true;
            }
            if (etEShopMobile.getText().toString().equals("+60")) {
                tipeshopmobile.setError("This field cannot be blank!");
                isError = true;
            } else if (etEShopMobile.getText().toString().length() < 11) {
                tipeshopmobile.setError("Invalid format");
                isError = true;
            }
            if (spnType.getSelectedItem().toString().equals(shopTypes[0])) {
                tipetype.setError("This field cannot be blank!");
                isError = true;
            }
            if (etEShopPayment.getText().toString().replace(" ", "").isEmpty()) {
                tipepayment.setError("This field cannot be blank!");
                isError = true;
            }
            if (etEShopAddress.getText().toString().replace(" ", "").isEmpty()) {
                tipeshopaddress.setError("This field cannot be blank!");
                isError = true;
            }
            if (spnShopState.getSelectedItem().toString().equals(states[0])) {
                tipeshopstate.setError("This field cannot be blank!");
                isError = true;
            }
            if (etEDescription.getText().toString().replace(" ", "").isEmpty()) {
                tipedescription.setError("This field cannot be blank!");
                isError = true;
            }

            if (!isError) {
                UpdateShop updateShop = new UpdateShop(StartActivity.user.getUsername(), imgUri, etEShopName.getText().toString(),
                        spnType.getSelectedItem().toString(), etEShopPayment.getText().toString(), etEShopMobile.getText().toString(),
                        etEShopAddress.getText().toString(), spnShopState.getSelectedItem().toString(),
                        StringEscapeUtils.escapeJava(etEDescription.getText().toString()).replace("\\", "SLASH"));
                updateShop.execute();
            }
        });

        return view;
    }

    private String getPath(Uri imgUri) {
        if (imgUri == null) {
            return null;
        } else {
            String[] projection = {MediaStore.Images.Media.DATA};
            @SuppressLint("Recycle")
            Cursor cursor = requireActivity().getContentResolver().query(imgUri, projection, null, null, null);

            if (cursor != null) {
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                return cursor.getString(col_index);
            }
        }
        return imgUri.getPath();
    }

    @SuppressLint("SimpleDateFormat")
    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SS:aa");
        simpleDateFormat.setTimeZone(timeZone);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, simpleDateFormat.format(calendar.getTime()), null);
        return Uri.parse(path);
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

    private void addservicepopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        AlertDialog alertDialog;
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.popup_addservice, null);
        TextInputLayout tipservicename = view.findViewById(R.id.tipservicename),
                tipserviceprice = view.findViewById(R.id.tipserviceprice);
        EditText etservicename = view.findViewById(R.id.et_serviceName),
                etserviceprice = view.findViewById(R.id.et_servicePrice);
        builder.setView(view).setTitle("Add Service")
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                boolean isError = false;
                tipservicename.setErrorEnabled(false);
                tipserviceprice.setErrorEnabled(false);

                if (etservicename.getText().toString().replace(" ", "").isEmpty()) {
                    isError = true;
                    tipservicename.setError("Don't leave blank!");
                }
                if (etserviceprice.getText().toString().isEmpty()) {
                    isError = true;
                    tipserviceprice.setError("Don't leave blank!");
                }

                if (!isError) {
                    double scale = Math.pow(10, 1);
                    double price = Math.round(Double.parseDouble(etserviceprice.getText().toString()) * scale) / scale;
                    AddNewService addNewService = new AddNewService(StartActivity.user.getUsername(),shopID,
                            etservicename.getText().toString(), price);
                    addNewService.execute();
                    dialogInterface.dismiss();
                }
            });
        });
        alertDialog.show();

        etserviceprice.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().startsWith("0")) {
                    etserviceprice.setText(charSequence.toString().replaceFirst("0", ""));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class GetShopData extends AsyncTask {
        private final String username;
        private JSONArray jsonArray;
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetShopData(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ivELogo.setEnabled(false);
            tipeshopname.setEnabled(false);
            tipeshopmobile.setEnabled(false);
            tipetype.setEnabled(false);
            tipepayment.setEnabled(false);
            tipeshopaddress.setEnabled(false);
            tipeshopstate.setEnabled(false);
            tipedescription.setEnabled(false);
            etEShopName.setEnabled(false);
            etEShopMobile.setEnabled(false);
            etEShopAddress.setEnabled(false);
            etEDescription.setEnabled(false);
            spnType.setEnabled(false);
            etEShopPayment.setEnabled(false);
            spnShopState.setEnabled(false);
            btnEmoji.setEnabled(false);
            lvServices.setEnabled(false);
            btnAddService.setEnabled(false);
            btnSaveService.setEnabled(false);
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getshopAPI.php?username=" + username)
                    .method("GET", null)
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

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(getContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(getContext(), "Error to get your information.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    jsonArray = new JSONArray(sresponse);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    Glide.with(requireActivity())
                            .load(Connection.getUrl() + jsonObject.getString("logo"))
                            .thumbnail(Glide.with(requireActivity()).load(R.drawable.loading))
                            .centerCrop().into(ivELogo);
                    etEShopName.setText(jsonObject.getString("shopName"));
                    etEShopMobile.setText(jsonObject.getString("mobile"));
                    for (int i = 0; i < shopTypes.length; i++) {
                        if (jsonObject.getString("type").equals(shopTypes[i])) {
                            spnType.setSelection(i);
                        }
                    }
                    etEShopPayment.setText(jsonObject.getString("payment"));
                    etEShopAddress.setText(jsonObject.getString("address"));
                    for (int i = 0; i < states.length; i++) {
                        if (jsonObject.getString("state").equals(states[i])) {
                            spnShopState.setSelection(i);
                        }
                    }
                    etEDescription.setText(StringEscapeUtils.unescapeJava(jsonObject.getString("description")
                            .replace("SLASH", "\\")));
                    GetServiceData getServiceData = new GetServiceData(jsonObject.getInt("id"));
                    getServiceData.execute();
                    shopID = jsonObject.getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetServiceData extends AsyncTask {
        private final int shopID;
        private final ArrayList<Service> serviceArrayList = new ArrayList<>();
        private String sresponse = "";
        private boolean isConnectionFail;

        public GetServiceData(int shopID) {
            this.shopID = shopID;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getserviceAPI.php?shopID=" + shopID)
                    .method("GET", null)
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

            ivELogo.setEnabled(true);
            tipeshopname.setEnabled(true);
            tipeshopmobile.setEnabled(true);
            tipetype.setEnabled(true);
            tipepayment.setEnabled(true);
            tipeshopaddress.setEnabled(true);
            tipeshopstate.setEnabled(true);
            tipedescription.setEnabled(true);
            etEShopName.setEnabled(true);
            etEShopMobile.setEnabled(true);
            etEShopAddress.setEnabled(true);
            etEDescription.setEnabled(true);
            spnType.setEnabled(true);
            etEShopPayment.setEnabled(true);
            spnShopState.setEnabled(true);
            btnEmoji.setEnabled(true);
            lvServices.setEnabled(true);
            btnAddService.setEnabled(true);
            btnSaveService.setEnabled(true);
            pbLoading.setVisibility(View.GONE);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(getContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(getContext(), "Error to get service.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Service service = new Service();
                        service.setId(jsonObject.getInt("id"));
                        service.setUsername(jsonObject.getString("username"));
                        service.setShopID(jsonObject.getInt("shopID"));
                        service.setServiceName(jsonObject.getString("serviceName"));
                        service.setServicePrice(jsonObject.getString("servicePrice"));
                        serviceArrayList.add(service);
                    }

                    ServiceListAdapter serviceListAdapter = new ServiceListAdapter(requireActivity(), serviceArrayList, lvServices);
                    lvServices.setAdapter(serviceListAdapter);
                    ServiceListHeight.setListViewHeightBasedOnItems(lvServices);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AddNewService extends AsyncTask {
        private final String username;
        private final int shopID;
        private final String servicename;
        private final double serviceprice;
        private String sresponse = "";
        private boolean isConnectionFail;

        public AddNewService(String username, int shopID, String servicename, double serviceprice) {
            this.username = username;
            this.shopID = shopID;
            this.servicename = servicename;
            this.serviceprice = serviceprice;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("shopID", String.valueOf(shopID))
                    .addFormDataPart("serviceName", servicename)
                    .addFormDataPart("servicePrice", String.valueOf(serviceprice))
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "addserviceAPI.php")
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

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(getContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(getContext(), "Fail to add service. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("OK")) {
                GetServiceData getServiceData = new GetServiceData(shopID);
                getServiceData.execute();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateShop extends AsyncTask {
        private final String username, shopName, type, payment, mobile, address, state, description;
        private final Uri imgUri;
        private String sresponse = "";
        private boolean isConnectionFail;

        public UpdateShop(String username, Uri imgUri, String shopName, String type, String payment, String mobile, String address, String state, String description) {
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

            ivELogo.setEnabled(false);
            tipeshopname.setEnabled(false);
            tipeshopmobile.setEnabled(false);
            tipetype.setEnabled(false);
            tipepayment.setEnabled(false);
            tipeshopaddress.setEnabled(false);
            tipeshopstate.setEnabled(false);
            tipedescription.setEnabled(false);
            etEShopName.setEnabled(false);
            etEShopMobile.setEnabled(false);
            etEShopAddress.setEnabled(false);
            etEDescription.setEnabled(false);
            spnType.setEnabled(false);
            etEShopPayment.setEnabled(false);
            spnShopState.setEnabled(false);
            btnEmoji.setEnabled(false);
            lvServices.setEnabled(false);
            btnAddService.setEnabled(false);
            btnSaveService.setEnabled(false);
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            if (imgUri != null) {
                @SuppressLint("SdCardPath")
                File logo = new File(getPath(imgUri).replace("/storage/emulated/0/", "/sdcard/"));
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("username", username)
                        .addFormDataPart("logo", logo.getName(),
                                RequestBody.create(MediaType.parse("application/octet-stream"),
                                        new File(logo.getPath())))
                        .addFormDataPart("shopName", shopName)
                        .addFormDataPart("type", type)
                        .addFormDataPart("payment", payment)
                        .addFormDataPart("mobile", mobile)
                        .addFormDataPart("address", address)
                        .addFormDataPart("state", state)
                        .addFormDataPart("description", description)
                        .build();
                Request request = new Request.Builder()
                        .url(Connection.getUrl() + "updateshopAPI.php")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    sresponse = Objects.requireNonNull(response.body()).string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
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
                        .url(Connection.getUrl() + "updateshopnopicAPI.php")
                        .method("POST", body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    sresponse = Objects.requireNonNull(response.body()).string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            ivELogo.setEnabled(true);
            tipeshopname.setEnabled(true);
            tipeshopmobile.setEnabled(true);
            tipetype.setEnabled(true);
            tipepayment.setEnabled(true);
            tipeshopaddress.setEnabled(true);
            tipeshopstate.setEnabled(true);
            tipedescription.setEnabled(true);
            etEShopName.setEnabled(true);
            etEShopMobile.setEnabled(true);
            etEShopAddress.setEnabled(true);
            etEDescription.setEnabled(true);
            spnType.setEnabled(true);
            etEShopPayment.setEnabled(true);
            spnShopState.setEnabled(true);
            btnEmoji.setEnabled(true);
            lvServices.setEnabled(true);
            btnAddService.setEnabled(true);
            btnSaveService.setEnabled(true);
            pbLoading.setVisibility(View.GONE);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(requireContext(), "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(requireContext(), "Error update your information.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("OK")) {
                Toast.makeText(requireContext(), "Done!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireContext(), StartActivity.class));
            }
        }
    }
}