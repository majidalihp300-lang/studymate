package com.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;

    private EditText editSearch;
    private Spinner spinnerFilter;
    private View emptyStateView;
    private FloatingActionButton fabAddTask;

    private String currentSearchKeyword = "";
    private String currentStatusFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Bind Views
        editSearch = view.findViewById(R.id.edit_search);
        spinnerFilter = view.findViewById(R.id.spinner_filter);
        emptyStateView = view.findViewById(R.id.empty_state_view);
        recyclerView = view.findViewById(R.id.recycler_tasks);
        fabAddTask = view.findViewById(R.id.fab_add_task);

        dbHelper = new DatabaseHelper(requireContext());
        taskList = new ArrayList<>();

        setupRecyclerView();
        setupSearchAndFilter();

        // FAB Click -> Add Task Activity
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), AddEditTaskActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Dynamic re-query when returning to list (e.g. after adding/editing)
        refreshTasks();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(requireContext(), taskList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        // Search text watcher
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchKeyword = s.toString();
                refreshTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Spinner filter item selections
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] filterOptions = getResources().getStringArray(R.array.task_filter_options);
                currentStatusFilter = filterOptions[position];
                refreshTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void refreshTasks() {
        if (dbHelper == null) return;
        List<Task> filtered = dbHelper.getFilteredTasks(currentSearchKeyword, currentStatusFilter);
        taskList = filtered;
        adapter.updateList(filtered);

        // Toggle Empty state visual container
        if (filtered.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // --- OnTaskClickListener Callbacks ---

    @Override
    public void onTaskClick(Task task) {
        // Open Detail Screen
        Intent intent = new Intent(requireActivity(), TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskStatusChanged(Task task, boolean isCompleted) {
        // Direct status toggle on list item Checkbox click
        task.setStatus(isCompleted ? "completed" : "pending");
        dbHelper.updateTask(task);
        refreshTasks();
        Toast.makeText(requireContext(), "Task marked as " + task.getStatus(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskDeleteClick(final Task task) {
        // Requirement: Confirmation Dialogs on Delete Operations
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Study Task");
        builder.setMessage("Are you sure you want to delete '" + task.getTitle() + "'? This operation cannot be undone.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteTask(task.getId());
                refreshTasks();
                Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
