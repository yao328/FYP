package com.example.fyp;

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

public class NewBookingService extends Service {
    private final UserDatabase userDatabase = new UserDatabase(NewBookingService.this);
    private String databaseUsername;
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
            databaseUsername = cursor.getString(cursor.getColumnIndex("username"));
            NotificationChannel notificationChannel = new NotificationChannel("channelid", "channelname",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(NewBookingService.this, "channelid")
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
        GetBooking getBooking = new GetBooking(databaseUsername);
        getBooking.execute();
        refresh();
    }

    private void refresh() {
        handler = new Handler();
        runnable = this::content;
        handler.postDelayed(runnable, 5000);
    }

    private class GetBooking extends AsyncTask {
        private final String boss;
        private String sresponse = "";

        private GetBooking(String boss) {
            this.boss = boss;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "appointmentnotiAPI.php?boss=" + boss)
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                sresponse = Objects.requireNonNull(response.body()).string();
                Log.e("bookingservice", sresponse);
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
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Intent notificationIntent = new Intent(NewBookingService.this, StartActivity.class);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Random random = new Random();
                        int id = random.nextInt(10000);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent pendingIntent = PendingIntent.getActivity(NewBookingService.this, id, notificationIntent, 0);

                        NotificationChannel notificationChannel = new NotificationChannel("channelid", "channelname",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        notificationChannel.setDescription("description");
                        notificationChannel.setShowBadge(true);
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(notificationChannel);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(NewBookingService.this, "channelid")
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("New Booking")
                                .setContentText("You have new booking: " + Objects.requireNonNull(jsonObject).getString("serviceName"))
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);

                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(NewBookingService.this);
                        notificationManagerCompat.notify(id, Objects.requireNonNull(builder).build());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}