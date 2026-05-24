package com.samil.kelimequiz.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.domain.model.AuthResult;
import com.samil.kelimequiz.ui.main.MainActivity;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.PasswordVisibilityToggle;
import com.samil.kelimequiz.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputLayout tilPassword;
    private TextView tvStatusMessage;
    private MaterialButton btnLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppContainer.from(this);
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openMainAndClose();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tilPassword = findViewById(R.id.tilPassword);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoRegister = findViewById(R.id.btnGoRegister);
        MaterialButton btnForgotPassword = findViewById(R.id.btnForgotPassword);

        bindPasswordToggle();
        btnLogin.setOnClickListener(v -> login());
        btnGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void bindPasswordToggle() {
        new PasswordVisibilityToggle(tilPassword, etPassword).bind();
    }

    private void login() {
        String username = getInput(etUsername);
        String password = getInput(etPassword);
        showStatus("Kontrol ediliyor...");
        btnLogin.setEnabled(false);
        AppExecutors.io().execute(() -> {
            try {
                AuthResult result = AppContainer.from(this).authRepository.login(username, password);
                if (result.isSuccess()) {
                    try {
                        AppContainer.from(this).wordSeedBootstrapper.ensureSeedWords(result.getUserId());
                    } catch (RuntimeException seedException) {
                        Log.e(TAG, "Seed words could not be imported.", seedException);
                    }
                }
                runOnUiThread(() -> handleAuthResult(result));
            } catch (RuntimeException exception) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    showStatus(exception.getMessage());
                });
            }
        });
    }

    private void handleAuthResult(AuthResult result) {
        btnLogin.setEnabled(true);
        if (!result.isSuccess()) {
            showStatus(result.getMessage());
            return;
        }

        sessionManager.saveUserId(result.getUserId());
        showStatus("Giriş başarılı.");
        openMainAndClose();
    }

    private void showStatus(String message) {
        tvStatusMessage.setText(message);
    }

    private String getInput(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private void openMainAndClose() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
