package com.example;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        mainToolbar = findViewById(R.id.main_toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Sets up toolbar
        setSupportActionBar(mainToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("StudyMate");
        }

        // Handle navigation clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String title = "StudyMate";

                int itemId = item.getItemId();
                if (itemId == R.id.navigation_dashboard) {
                    selectedFragment = new DashboardFragment();
                    title = "Dashboard";
                } else if (itemId == R.id.navigation_tasks) {
                    selectedFragment = new TaskListFragment();
                    title = "My Study Tasks";
                } else if (itemId == R.id.navigation_settings) {
                    selectedFragment = new SettingsFragment();
                    title = "Settings";
                }

                if (selectedFragment != null) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(title);
                    }
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });

        // Load Default Fragment (Dashboard) on first launch
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
