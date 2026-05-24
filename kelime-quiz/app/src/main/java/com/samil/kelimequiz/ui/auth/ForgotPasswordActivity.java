package com.samil.kelimequiz.ui.auth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.domain.model.AuthResult;
import com.samil.kelimequiz.util.AppContainer;
import com.samil.kelimequiz.util.AppExecutors;
import com.samil.kelimequiz.util.NavigationHelper;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText etUsername;
    private TextInputEditText etNewPassword;
    private TextInputEditText etNewPasswordRepeat;
    private TextView tvStatusMessage;
    private MaterialButton btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etUsername = findViewById(R.id.etUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        etNewPasswordRepeat = findViewById(R.id.etNewPasswordRepeat);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        NavigationHelper.bindTopBar(this);
        AppExecutors.io().execute(() -> AppContainer.from(this));
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String username = getInput(etUsername);
        String newPassword = getInput(etNewPassword);
        String repeatedPassword = getInput(etNewPasswordRepeat);
        if (!newPassword.equals(repeatedPassword)) {
            showStatus("Yeni şifreler eşleşmiyor.");
            return;
        }

        showStatus("Şifre güncelleniyor...");
        btnResetPassword.setEnabled(false);
        AppExecutors.io().execute(() -> {
            AuthResult result = AppContainer.from(this).authRepository.resetPassword(username, newPassword);
            runOnUiThread(() -> handleResetResult(result));
        });
    }

    private void handleResetResult(AuthResult result) {
        btnResetPassword.setEnabled(true);
        showStatus(result.getMessage());
        if (result.isSuccess()) {
            finish();
        }
    }

    private void showStatus(String message) {
        tvStatusMessage.setText(message);
    }

    private String getInput(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
