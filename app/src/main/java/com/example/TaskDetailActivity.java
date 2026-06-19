package com.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;

public class TaskDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int taskId = -1;
    private Task currentTask;

    private Toolbar toolbar;
    private MaterialCardView cvStatusBadge;
    private TextView tvStatusText;
    private TextView tvTaskId;
    private TextView tvTaskTitle;
    private TextView tvTaskDate;
    private TextView tvTaskDesc;

    private com.google.android.material.button.MaterialButton btnToggleCompleteness;
    private Button btnEditTask;
    private Button btnDeleteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Bind Views
        toolbar = findViewById(R.id.detail_toolbar);
        cvStatusBadge = findViewById(R.id.cv_detail_status_badge);
        tvStatusText = findViewById(R.id.tv_detail_status_text);
        tvTaskId = findViewById(R.id.tv_detail_id);
        tvTaskTitle = findViewById(R.id.tv_detail_title);
        tvTaskDate = findViewById(R.id.tv_detail_date);
        tvTaskDesc = findViewById(R.id.tv_detail_desc);

        btnToggleCompleteness = findViewById(R.id.btn_toggle_completeness);
        btnEditTask = findViewById(R.id.btn_edit_task);
        btnDeleteTask = findViewById(R.id.btn_delete_task);

        dbHelper = new DatabaseHelper(this);

        // Configure toolbar
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

        // Parse Task ID
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TASK_ID")) {
            taskId = intent.getIntExtra("TASK_ID", -1);
        }

        if (taskId == -1) {
            Toast.makeText(this, "Error parsing task details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupButtonActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Dynamically load/reload task details so that updates are drawn immediately upon return
        loadTaskAndRender();
    }

    private void loadTaskAndRender() {
        currentTask = dbHelper.getTask(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "Task was deleted or does not exist.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Output fields
        tvTaskId.setText("ID: #" + currentTask.getId());
        tvTaskTitle.setText(currentTask.getTitle());
        tvTaskDate.setText(currentTask.getDate());
        
        String desc = currentTask.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            tvTaskDesc.setText("(No description provided for this academic task.)");
            tvTaskDesc.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            tvTaskDesc.setText(desc);
            tvTaskDesc.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }

        // Render status badges and dynamic button actions
        boolean isCompleted = "completed".equalsIgnoreCase(currentTask.getStatus());
        if (isCompleted) {
            cvStatusBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_completed));
            tvStatusText.setText("DONE");
            btnToggleCompleteness.setText("MARK AS PENDING");
            btnToggleCompleteness.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
            btnToggleCompleteness.setStrokeColor(ContextCompat.getColorStateList(this, R.color.status_pending));
        } else {
            cvStatusBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_pending));
            tvStatusText.setText("PENDING");
            btnToggleCompleteness.setText("MARK AS COMPLETED");
            btnToggleCompleteness.setTextColor(ContextCompat.getColor(this, R.color.status_completed));
            btnToggleCompleteness.setStrokeColor(ContextCompat.getColorStateList(this, R.color.status_completed));
        }
    }

    private void setupButtonActions() {
        // Toggle Task Status (Pending <-> Completed)
        btnToggleCompleteness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTask == null) return;
                
                String newStatus = "completed".equalsIgnoreCase(currentTask.getStatus()) ? "pending" : "completed";
                currentTask.setStatus(newStatus);
                dbHelper.updateTask(currentTask);
                
                loadTaskAndRender();
                Toast.makeText(TaskDetailActivity.this, "Status updated to " + newStatus.toUpperCase(), Toast.LENGTH_SHORT).show();
            }
        });

        // Open AddEditTaskActivity for modifications
        btnEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, AddEditTaskActivity.class);
                intent.putExtra("TASK_ID", taskId);
                startActivity(intent);
            }
        });

        // Delete Task Action (including verification Dialog prompts)
        btnDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTask == null) return;

                AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetailActivity.this);
                builder.setTitle("Confirm Deletion");
                builder.setMessage("Are you sure you want to delete '" + currentTask.getTitle() + "'? This action will permanently remove it from database tables.");
                builder.setIcon(android.R.drawable.ic_dialog_alert);

                builder.setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteTask(taskId);
                        Toast.makeText(TaskDetailActivity.this, "Task permanently removed.", Toast.LENGTH_SHORT).show();
                        finish();
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
