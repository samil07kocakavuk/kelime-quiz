package com.samil.kelimequiz.ui.auth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.domain.model.AuthResult;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.NavigationHelper;
import com.samil.kelimequiz.util.PasswordVisibilityToggle;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etPasswordRepeat;
    private TextInputLayout tilPassword;
    private TextInputLayout tilPasswordRepeat;
    private TextView tvStatusMessage;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPasswordRepeat = findViewById(R.id.etPasswordRepeat);
        tilPassword = findViewById(R.id.tilPassword);
        tilPasswordRepeat = findViewById(R.id.tilPasswordRepeat);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        btnRegister = findViewById(R.id.btnRegister);

        NavigationHelper.bindTopBar(this);
        AppExecutors.io().execute(() -> AppContainer.from(this));
        bindPasswordToggles();
        btnRegister.setOnClickListener(v -> register());
    }

    private void bindPasswordToggles() {
        new PasswordVisibilityToggle(tilPassword, etPassword).bind();
        new PasswordVisibilityToggle(tilPasswordRepeat, etPasswordRepeat).bind();
    }

    private void register() {
        String username = getInput(etUsername);
        String password = getInput(etPassword);
        String passwordRepeat = getInput(etPasswordRepeat);
        if (!password.equals(passwordRepeat)) {
            showStatus("Şifreler eşleşmiyor.");
            return;
        }

        showStatus("Kayıt oluşturuluyor...");
        btnRegister.setEnabled(false);
        AppExecutors.io().execute(() -> {
            AuthResult result = AppContainer.from(this).authRepository.register(username, password);
            runOnUiThread(() -> handleRegisterResult(result));
        });
    }

    private void handleRegisterResult(AuthResult result) {
        btnRegister.setEnabled(true);
        if (!result.isSuccess()) {
            showStatus(result.getMessage());
            return;
        }

        showStatus("Kayıt başarılı.");
        finish();
    }

    private void showStatus(String message) {
        tvStatusMessage.setText(message);
    }

    private String getInput(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
