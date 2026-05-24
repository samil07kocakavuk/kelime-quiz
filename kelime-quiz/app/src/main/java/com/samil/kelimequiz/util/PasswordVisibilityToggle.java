package com.samil.kelimequiz.util;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.samil.kelimequiz.R;

public class PasswordVisibilityToggle {
    private final TextInputLayout layout;
    private final TextInputEditText input;
    private boolean visible;

    public PasswordVisibilityToggle(TextInputLayout layout, TextInputEditText input) {
        this.layout = layout;
        this.input = input;
    }

    public void bind() {
        setVisible(false);
        layout.setEndIconOnClickListener(v -> setVisible(!visible));
    }

    private void setVisible(boolean visible) {
        this.visible = visible;
        input.setTransformationMethod(visible
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        layout.setEndIconDrawable(visible ? R.drawable.ic_eye : R.drawable.ic_eye_off);
        input.setSelection(input.length());
    }
}
