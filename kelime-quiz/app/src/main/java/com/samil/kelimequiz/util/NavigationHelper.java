package com.samil.kelimequiz.util;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.samil.kelimequiz.R;
import com.samil.kelimequiz.ui.main.MainActivity;
import com.samil.kelimequiz.ui.profile.ProfileActivity;
import com.samil.kelimequiz.ui.word.AddWordActivity;

public final class NavigationHelper {
    private NavigationHelper() {
    }

    public static void bindTopBar(AppCompatActivity activity, boolean showBackButton) {
        bindTopBar(activity, showBackButton, MainActivity.class);
    }

    public static void bindTopBar(AppCompatActivity activity, boolean showBackButton, Class<?> targetClass) {
        ImageButton btnBack = activity.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setVisibility(showBackButton ? View.VISIBLE : View.GONE);
            if (showBackButton) {
                btnBack.setOnClickListener(v -> redirectTo(activity, targetClass));
            }
        }
    }

    public static void bindTopBar(AppCompatActivity activity) {
        bindTopBar(activity, true);
    }

    public static void redirectToMain(AppCompatActivity activity) {
        redirectTo(activity, MainActivity.class);
    }

    public static void redirectTo(AppCompatActivity activity, Class<?> targetClass) {
        if (activity.getClass().equals(targetClass)) {
            activity.finish();
        } else {
            Intent intent = new Intent(activity, targetClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    public static void bindBottomBar(AppCompatActivity activity) {
        MaterialButton btnHome = activity.findViewById(R.id.btnNavHome);
        MaterialButton btnAdd = activity.findViewById(R.id.btnNavAdd);
        MaterialButton btnProfile = activity.findViewById(R.id.btnNavProfile);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> openIfNeeded(activity, MainActivity.class));
        }
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> openIfNeeded(activity, AddWordActivity.class));
        }
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> openIfNeeded(activity, ProfileActivity.class));
        }
    }

    private static void openIfNeeded(AppCompatActivity activity, Class<?> target) {
        if (activity.getClass().equals(target)) {
            return;
        }
        activity.startActivity(new Intent(activity, target));
    }
}
