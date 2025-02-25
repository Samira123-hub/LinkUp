package com.example.homepage2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LinearLayout personal = findViewById(R.id.personalInfo);
        personal.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, Information.class);
            startActivity(intent);
        });

        LinearLayout security = findViewById(R.id.mdp);
        security.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, motdepasse.class);
            startActivity(intent);
        });

        ImageView fleche = findViewById(R.id.sfleche);
        fleche.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageView logo = findViewById(R.id.scdLogoApp);
        logo.setOnClickListener(v -> {
            Intent intent1 = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent1);
        });

        LinearLayout logoutButton = findViewById(R.id.logOut);
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log out?")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Effacer les données de l'utilisateur dans SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("UserPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();

                        // Informer l'utilisateur et rediriger vers la page de connexion
                        Toast.makeText(this, "Log out", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, com.example.loginregister.Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });


//        LinearLayout darkModeLayout = findViewById(R.id.darkMode);
//        darkModeLayout.setOnClickListener(v -> {
//            SharedPreferences preferences = getSharedPreferences("ThemePref", MODE_PRIVATE);
//            boolean isDarkMode = preferences.getBoolean("isDarkMode", false);
//            SharedPreferences.Editor editor = preferences.edit();
//
//            if (isDarkMode) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                editor.putBoolean("isDarkMode", false);
//                Toast.makeText(SettingsActivity.this, "Switched to Light Mode", Toast.LENGTH_SHORT).show();
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                editor.putBoolean("isDarkMode", true);
//                Toast.makeText(SettingsActivity.this, "Switched to Dark Mode", Toast.LENGTH_SHORT).show();
//            }
//            editor.apply();
//            recreate();  // Redémarrer l'activité pour appliquer le nouveau thème
//        });
    }
}
