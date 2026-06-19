package com.example;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;

public class AddEditTaskActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int taskId = -1; // -1 means we are creating a new task, otherwise we are editing

    private Toolbar toolbar;
    private TextInputLayout layoutTitle;
    private TextInputLayout layoutDate;
    private EditText etTitle;
    private EditText etDescription;
    private EditText etDate;
    private Spinner spinnerStatus;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // Bind Views
        toolbar = findViewById(R.id.add_edit_toolbar);
        layoutTitle = findViewById(R.id.layout_title);
        layoutDate = findViewById(R.id.layout_date);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etDate = findViewById(R.id.et_date);
        spinnerStatus = findViewById(R.id.spinner_status);
        btnSave = findViewById(R.id.btn_save);

        dbHelper = new DatabaseHelper(this);

        // Set up toolbar back navigation
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Determine if adding or editing
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TASK_ID")) {
            taskId = intent.getIntExtra("TASK_ID", -1);
        }

        if (taskId != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Study Task");
            }
            loadTaskDetails(taskId);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Create New Task");
            }
        }

        setupFormListeners();
    }

    private void loadTaskDetails(int id) {
        Task task = dbHelper.getTask(id);
        if (task != null) {
            etTitle.setText(task.getTitle());
            etDescription.setText(task.getDescription());
            etDate.setText(task.getDate());

            // Set spinner selection
            if ("completed".equalsIgnoreCase(task.getStatus())) {
                spinnerStatus.setSelection(1);
            } else {
                spinnerStatus.setSelection(0);
            }
        } else {
            Toast.makeText(this, "Failed to load task details.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupFormListeners() {
        // Date input click handler to display native Date Picker Dialog
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Save Button Action (Crud Creation & Updation with custom input validations)
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTaskAndExit();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Month starts at 0, must pad month and day values for standard YYYY-MM-DD
                        String formattedMonth = String.format("%02d", selectedMonth + 1);
                        String formattedDay = String.format("%02d", selectedDay);
                        String dateString = selectedYear + "-" + formattedMonth + "-" + formattedDay;
                        etDate.setText(dateString);
                        layoutDate.setError(null); // Clear previous errors
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveTaskAndExit() {
        String titleStr = etTitle.getText().toString().trim();
        String descStr = etDescription.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String statusStr = spinnerStatus.getSelectedItem().toString().toLowerCase(); // "pending" or "completed"

        // Reset errors
        layoutTitle.setError(null);
        layoutDate.setError(null);

        boolean isValid = true;

        // Validation Checks
        if (titleStr.isEmpty()) {
            layoutTitle.setError("Academic task title is required.");
            isValid = false;
        }

        if (dateStr.isEmpty()) {
            layoutDate.setError("Please pick a calendar due date.");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix mandatory missing fields before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Database logic
        if (taskId != -1) {
            // Edit mode
            Task taskToEdit = new Task(taskId, titleStr, descStr, dateStr, statusStr);
            int rowCount = dbHelper.updateTask(taskToEdit);
            if (rowCount > 0) {
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Database error: task updates failed.", Toast.LENGTH_LONG).show();
            }
        } else {
            // New task definition
            Task newTask = new Task(titleStr, descStr, dateStr, statusStr);
            long checkId = dbHelper.addTask(newTask);
            if (checkId != -1) {
                Toast.makeText(this, "New task created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Database error: failed to create task.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
