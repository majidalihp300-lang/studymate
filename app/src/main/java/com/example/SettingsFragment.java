package com.example;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SwitchMaterial switchTheme;
    private SwitchMaterial switchNotifications;
    private Button btnResetDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchTheme = view.findViewById(R.id.switch_theme);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        btnResetDb = view.findViewById(R.id.btn_reset_db);

        dbHelper = new DatabaseHelper(requireContext());

        // Initialize state from real SharedPreferences preference
        SharedPreferences prefs = requireContext().getSharedPreferences("study_mate_prefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchTheme.setChecked(isDarkMode);

        setupListeners();
        return view;
    }

    private void setupListeners() {
        // Real theme toggle
        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = requireContext().getSharedPreferences("study_mate_prefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("dark_mode", isChecked).apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(requireContext(), "Dark Mode enabled! Enjoy eye-comfortable late-night sessions. 🌙", Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(requireContext(), "Light Mode enabled! Bright study environment ready.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Simulating notification toggle
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(requireContext(), "Reminders enabled! You will be prompted before major study milestones are due.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Reminders turned off.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Reset database handler (CRUD Helper / Maintenance)
        btnResetDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Confirm Hard Reset");
                builder.setMessage("This will wipe all records from StudyMate's SQLite tables. Are you absolutely sure you want to perform a hard reset?");
                builder.setIcon(android.R.drawable.ic_dialog_alert);

                builder.setPositiveButton("Yes, Clear Everything", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.resetDatabase();
                        Toast.makeText(requireContext(), "All study task records successfully erased from SQLite.", Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}
