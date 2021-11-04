package com.example.fyp.ui.stafflist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp.Appointment;
import com.example.fyp.Chat;
import com.example.fyp.ChatAdapter;
import com.example.fyp.Connection;
import com.example.fyp.R;
import com.example.fyp.StartActivity;
import com.example.fyp.TrackActivity;
import com.vanniktech.emoji.EmojiPopup;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class ChatActivity extends AppCompatActivity {
    private String myusername;
    private String targetusername;
    private RecyclerView rvChatList;
    private ImageButton btnSelectImage, btnSend;
    private EditText etMessage;
    private ProgressBar pbLaoding;
    private final ArrayList<Chat> chatArrayList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private Handler refreshhandler;
    private Runnable refreshrunnable;
    private Staff staff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.chatactivitytoolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        staff = (Staff) getIntent().getSerializableExtra("staff");
        myusername = bundle.getString("myusername");
        targetusername = bundle.getString("targetusername");

        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvTargetName = findViewById(R.id.tv_targetName);
        rvChatList = findViewById(R.id.rv_chatList);
        btnSelectImage = findViewById(R.id.btn_selectImage);
        btnSend = findViewById(R.id.btn_send);
        etMessage = findViewById(R.id.et_message);
        pbLaoding = findViewById(R.id.pb_loading);

        GetMessage getMessage = new GetMessage();
        getMessage.execute();

        btnBack.setOnClickListener(view -> {
            finish();
            refreshhandler.removeCallbacks(refreshrunnable);
        });

        tvTargetName.setText(targetusername);

        btnSend.setOnClickListener(view -> {
            SendMessage sendMessage = new SendMessage(etMessage.getText().toString());
            sendMessage.execute();
            etMessage.setText("");
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(btnSend.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        });

        ActivityResultLauncher<Intent> selectImg = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Uri imgUri = intent.getData();
                        SendImage sendImage = new SendImage(imgUri);
                        sendImage.execute();
                    }
                });

        btnSelectImage.setOnClickListener(view -> {
            String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!EasyPermissions.hasPermissions(ChatActivity.this, perms)) {
                EasyPermissions.requestPermissions(ChatActivity.this, "Allow this apps to read your storage?",
                        0, perms);
            } else {
                selectImg.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void content() {
        CheckLastMessage checkLastMessage = new CheckLastMessage();
        checkLastMessage.execute();
        refreshGetMessage();
    }

    private void refreshGetMessage() {
        refreshhandler = new Handler();
        refreshrunnable = this::content;
        refreshhandler.postDelayed(refreshrunnable, 500);
    }

    @SuppressLint("StaticFieldLeak")
    private class SendMessage extends AsyncTask {
        private final String message;
        private String sresponse="";

        private SendMessage(String message) {
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btnSelectImage.setEnabled(false);
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("sender", myusername)
                    .addFormDataPart("receiver", targetusername)
                    .addFormDataPart("message", StringEscapeUtils.escapeJava(message).replace("\\", "SLASH"))
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "addchatAPI.php")
                    .method("POST", body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            btnSelectImage.setEnabled(true);
            etMessage.setEnabled(true);
            btnSend.setEnabled(true);

            if (!sresponse.equals("OK")) {
                Toast.makeText(ChatActivity.this, "Message fail to send", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetMessage extends AsyncTask {
        private String sresponse="";
        private boolean isConnectionFail;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbLaoding.setVisibility(View.VISIBLE);
            btnSelectImage.setEnabled(false);
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getallchatAPI.php?myusername=" + myusername + "&targetusername=" + targetusername)
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

            pbLaoding.setVisibility(View.GONE);
            btnSelectImage.setEnabled(true);
            etMessage.setEnabled(true);
            btnSend.setEnabled(true);

            if (isConnectionFail || sresponse.equals("Fail")) {
                Toast.makeText(ChatActivity.this, "Connection failed. Please try again later.", Toast.LENGTH_SHORT).show();
            } else if (sresponse.equals("Error")) {
                Toast.makeText(ChatActivity.this, "No message", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Chat chat = new Chat();
                        chat.setId(jsonObject.getString("id"));
                        chat.setSender(jsonObject.getString("sender"));
                        chat.setReceiver(jsonObject.getString("receiver"));
                        chat.setMessage(jsonObject.getString("message"));
                        chat.setImage(jsonObject.getString("image"));
                        chat.setTime(jsonObject.getString("time"));
                        chatArrayList.add(chat);
                    }

                    chatAdapter = new ChatAdapter(ChatActivity.this, chatArrayList, myusername, targetusername);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    layoutManager.setStackFromEnd(true);
                    rvChatList.setItemAnimator(new DefaultItemAnimator());
                    rvChatList.setLayoutManager(layoutManager);
                    rvChatList.setAdapter(chatAdapter);

                    Handler handler = new Handler();
                    Runnable runnable = () -> rvChatList.scrollToPosition(chatArrayList.size() - 1);
                    handler.postDelayed(runnable, 500);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            content();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckLastMessage extends AsyncTask {
        private String sresponse = "";

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "getallchatAPI.php?myusername=" + myusername + "&targetusername=" + targetusername)
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (!sresponse.equals("Fail") && !sresponse.equals("Error")) {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    if (chatArrayList.size() == 0 && jsonArray.length() != 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Chat chat = new Chat();
                            chat.setId(jsonObject.getString("id"));
                            chat.setSender(jsonObject.getString("sender"));
                            chat.setReceiver(jsonObject.getString("receiver"));
                            chat.setMessage(jsonObject.getString("message"));
                            chat.setImage(jsonObject.getString("image"));
                            chat.setTime(jsonObject.getString("time"));
                            chatArrayList.add(chat);
                        }

                        chatAdapter = new ChatAdapter(ChatActivity.this, chatArrayList, myusername, targetusername);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        layoutManager.setStackFromEnd(true);
                        rvChatList.setItemAnimator(new DefaultItemAnimator());
                        rvChatList.setLayoutManager(layoutManager);
                        rvChatList.setAdapter(chatAdapter);

                        Handler handler = new Handler();
                        Runnable runnable = () -> rvChatList.scrollToPosition(chatArrayList.size() - 1);
                        handler.postDelayed(runnable, 500);
                    } else {
                        JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);
                        if (!chatArrayList.get(chatArrayList.size() - 1).getId().equals(jsonObject.getString("id"))) {
                            Log.e("here", "here");
                            Chat chat = new Chat();
                            chat.setId(jsonObject.getString("id"));
                            chat.setSender(jsonObject.getString("sender"));
                            chat.setReceiver(jsonObject.getString("receiver"));
                            chat.setMessage(jsonObject.getString("message"));
                            chat.setImage(jsonObject.getString("image"));
                            chat.setTime(jsonObject.getString("time"));
                            chatArrayList.add(chat);
                            chatAdapter.notifyItemInserted(chatArrayList.size() - 1);
                            rvChatList.scrollToPosition(chatArrayList.size() - 1);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getPath(Uri imgUri) {
        Cursor cursor = getContentResolver().query(imgUri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @SuppressLint("StaticFieldLeak")
    private class SendImage extends AsyncTask {
        private final Uri imgUri;
        private String sresponse="";

        public SendImage(Uri imgUri) {
            this.imgUri = imgUri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btnSelectImage.setEnabled(false);
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            @SuppressLint("SdCardPath")
            File file = new File(getPath(imgUri).replace("/storage/emulated/0/", "/sdcard/"));
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("sender", myusername)
                    .addFormDataPart("receiver", targetusername)
                    .addFormDataPart("image", file.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(file.getPath())))
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "insertchatimgAPI.php")
                    .method("POST", body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            btnSelectImage.setEnabled(true);
            etMessage.setEnabled(true);
            btnSend.setEnabled(true);

            if (!sresponse.equals("OK")) {
                Toast.makeText(ChatActivity.this, "Image failed to send.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}