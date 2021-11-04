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

import org.apache.commons.text.StringEscapeUtils;
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

public class ChatService extends Service {
    private final UserDatabase userDatabase = new UserDatabase(ChatService.this);
    private String databaseUsername;
    private Handler handler;
    private Runnable runnable;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatService.this, "channelid")
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

    private void content() {
        GetMsg getMsg = new GetMsg(databaseUsername);
        getMsg.execute();
        refresh();
    }

    private void refresh() {
        handler = new Handler();
        runnable = () -> content();
        handler.postDelayed(runnable, 5000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopSelf();
        handler.removeCallbacks(runnable);
    }

    private class GetMsg extends AsyncTask {
        private final String username;
        private String sresponse = "";

        private GetMsg(String username) {
            this.username = username;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Connection.getUrl() + "chatnotiAPI.php?username=" + username)
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

            Log.e("chatservice", sresponse);
            if (!sresponse.equals("Fail") && !sresponse.equals("Error")) {
                try {
                    JSONArray jsonArray = new JSONArray(sresponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Intent notificationIntent = new Intent(ChatService.this, StartActivity.class);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Random random = new Random();
                        int id = random.nextInt(10000);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent pendingIntent = PendingIntent.getActivity(ChatService.this, id, notificationIntent, 0);

                        NotificationChannel notificationChannel = new NotificationChannel("channelid", "channelname",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        notificationChannel.setDescription("description");
                        notificationChannel.setShowBadge(true);
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(notificationChannel);

                        if (!jsonObject.getString("message").equals("null")) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                    ChatService.this, "channelid")
                                    .setSmallIcon(R.drawable.ic_chat)
                                    .setContentTitle("New message")
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(jsonObject.getString("sender") + ": " + StringEscapeUtils.unescapeJava(jsonObject.getString("message").replace("SLASH", "\\"))))
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ChatService.this);
                            notificationManagerCompat.notify(id, Objects.requireNonNull(builder).build());
                        } else {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                    ChatService.this, "channelid")
                                    .setSmallIcon(R.drawable.ic_chat)
                                    .setContentTitle("New message")
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(jsonObject.getString("sender") + ": send an image"))
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ChatService.this);
                            notificationManagerCompat.notify(id, Objects.requireNonNull(builder).build());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}