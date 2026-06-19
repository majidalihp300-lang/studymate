package com.example;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private List<Task> taskList;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskStatusChanged(Task task, boolean isCompleted);
        void onTaskDeleteClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateList(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox cbStatus;
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvDate;
        private final ImageButton btnDelete;
        private final MaterialCardView cvStatusBadge;
        private final TextView tvStatusBadgeText;
        private final MaterialCardView cardParent;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbStatus = itemView.findViewById(R.id.cb_task_status);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDesc = itemView.findViewById(R.id.tv_task_desc);
            tvDate = itemView.findViewById(R.id.tv_task_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_quick);
            cvStatusBadge = itemView.findViewById(R.id.cv_status_badge);
            tvStatusBadgeText = itemView.findViewById(R.id.tv_status_badge_text);
            cardParent = (MaterialCardView) itemView;
        }

        public void bind(final Task task, final OnTaskClickListener clickListener) {
            tvTitle.setText(task.getTitle());
            tvDesc.setText(task.getDescription());
            tvDate.setText(task.getDate());

            // Set Checkbox state without triggering listener initially
            cbStatus.setOnCheckedChangeListener(null);
            boolean isCompleted = "completed".equalsIgnoreCase(task.getStatus());
            cbStatus.setChecked(isCompleted);

            // Apply Strikethrough dynamic styling for completed study items
            if (isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                cvStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_completed));
                tvStatusBadgeText.setText("DONE");
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                cvStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_pending));
                tvStatusBadgeText.setText("PENDING");
            }

            // Re-attach status listener
            cbStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (clickListener != null) {
                        clickListener.onTaskStatusChanged(task, isChecked);
                    }
                }
            });

            // Card body clicks open detail screen
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onTaskClick(task);
                    }
                }
            });

            // Quick Delete click
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onTaskDeleteClick(task);
                    }
                }
            });
        }
    }
}
