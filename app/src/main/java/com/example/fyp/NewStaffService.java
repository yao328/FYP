package com.example.fyp;

import static com.example.fyp.StartActivity.user;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewStaffService extends Service {
    private final UserDatabase userDatabase = new UserDatabase(NewStaffService.this);
    private String databaseusername, databasepassword;
    private int databaseidentity;
    private Handler handler;
    private Runnable runnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("Range")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Cursor cursor = userDatabase.getData();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            databaseusername = cursor.getString(cursor.getColumnIndex("username"));
            databasepassword = cursor.getString(cursor.getColumnIndex("password"));
            databaseidentity = cursor.getInt(cursor.getColumnIndex("identity"));

            NotificationChannel notificationChannel = new NotificationChannel("channelid", "channelname",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(NewStaffService.this, "channelid")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Service enabled")
                    .setContentText("Running service")
                    .setAutoCancel(true)
                    .setSilent(true);

            startForeground(1, builder.build());

            content();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopSelf();
        handler.removeCallbacks(runnable);
    }

    private void content() {
        SignIn signIn = new SignIn(databaseusername, databasepassword);
        signIn.execute();
        refresh();
    }

    private void refresh() {
        handler = new Handler();
        runnable = this::content;
        handler.postDelayed(runnable, 5000);
    }

    private class SignIn extends AsyncTask {
        private final String username, password;
        private String sresponse = "";

        public SignIn(String username, String password) {
            this.username = username;
            this.password = password;
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

                    userDatabase.deleteDatabase();
                    userDatabase.insertUserData(username, password, user.getIdentity());
                    if (databaseidentity == 1 && user.getIdentity() == 2) {
                        GetBossShop getBossShop = new GetBossShop(username);
                        getBossShop.execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetBossShop extends AsyncTask {
        private final String staff;
        private String sresponse = "";

        public GetBossShop(String staff) {
            this.staff = staff;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "staffgetshopAPI.php?staff=" + staff)
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
                Log.e("staffservice", sresponse);
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    Intent notificationIntent = new Intent(NewStaffService.this, StartActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Random random = new Random();
                    int id = random.nextInt(10000);
                    @SuppressLint("UnspecifiedImmutableFlag")
                    PendingIntent pendingIntent = PendingIntent.getActivity(NewStaffService.this, id, notificationIntent, 0);

                    NotificationChannel notificationChannel = new NotificationChannel("channelid", "channelname",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationChannel.setDescription("description");
                    notificationChannel.setShowBadge(true);
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(notificationChannel);

                    try {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                NewStaffService.this, "channelid")
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(getResources().getString(R.string.app_name))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText("You have been invited as a staff under company: "
                                                + jsonObject.getString("shopName")))
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(NewStaffService.this);
                        notificationManagerCompat.notify(id, Objects.requireNonNull(builder).build());

                        userDatabase.deleteDatabase();
                        userDatabase.insertUserData(databaseusername, databasepassword, 2);
                        databaseidentity = 2;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}