package com.example;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Random;

public class DashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView txtQuote;
    private TextView countPending;
    private TextView countCompleted;
    private TextView txtProgressPercent;
    private ProgressBar progressStudy;
    private TextView txtProgressAnalysis;
    private TextView txtStudyTip;

    // Timer widgets
    private TextView txtTimerStatus;
    private TextView txtTimerCountdown;
    private com.google.android.material.button.MaterialButton btnTimerStart;
    private com.google.android.material.button.MaterialButton btnTimerReset;
    private com.google.android.material.button.MaterialButton btnTimerMode;

    private Handler timerHandler = new Handler();
    private boolean isTimerRunning = false;
    private boolean isWorkMode = true; // true = 25m focus, false = 5m break
    private int timeLeftSeconds = 25 * 60; // 25 minutes default

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (timeLeftSeconds > 0) {
                timeLeftSeconds--;
                updateTimerText();
                timerHandler.postDelayed(this, 1000);
            } else {
                isTimerRunning = false;
                btnTimerStart.setText("START");
                if (isWorkMode) {
                    Toast.makeText(getContext(), "🎉 Great work! Time for a short break.", Toast.LENGTH_LONG).show();
                    setBreakMode();
                } else {
                    Toast.makeText(getContext(), "💪 Let's do this! Back to study mode.", Toast.LENGTH_LONG).show();
                    setWorkMode();
                }
            }
        }
    };

    private final String[] motivationalQuotes = {
            "\"The secret of getting ahead is getting started.\" — Mark Twain",
            "\"It always seems impossible until it is done.\" — Nelson Mandela",
            "\"Quality is not an act, it is a habit.\" — Aristotle",
            "\"Procrastination is the thief of time.\" — Edward Young",
            "\"Believe you can and you're halfway there.\" — Theodore Roosevelt",
            "\"Success is the sum of small efforts, repeated day in and day out.\" — Robert Collier"
    };

    private final String[] studyTips = {
            "Try the Pomodoro Technique: study intensely for 25 minutes, then take a short 5-minute break. This keeps your brain sharp!",
            "Explain difficult concepts to someone else (or an imaginary audience). Teaching acts as a cognitive reinforcement.",
            "Eliminate multitasking. When studying math, only have math books and tools open. Silence notifications on your phone.",
            "Take handwritten notes. Studies show writing by hand improves memory recall and structured concept synthesis.",
            "Ensure you maintain a solid sleep routine before major exams. Memory consolidation happens during deep REM sleep cycles.",
            "Spaced Repetition: Review materials after 1 day, then 3 days, then 7 days, then 14 days to transition knowledge to long-term memory.",
            "Explain Like I'm Five (Feynman Technique): Write an explanation of your subject in simple terms to spot critical gaps in your understanding.",
            "Optimize Study Lighting: Proper lighting increases focus and visual endurance. Pair comfortable room lights with cool desk lamp glow.",
            "Active Recall Practice: Close your textbook after reading a chapter and write a quick summary of core concepts strictly from memory first.",
            "Visual Logic Mapping: Create mind maps using icons and branch connections. Human brains store spatial visuals much faster than plain text blocks.",
            "Interleaved Study Routines: Alternate between completely different topics (e.g. maths vs languages) in a single session to build cognitive versatility.",
            "Pre-Test Challenge: Attempt practice questions BEFORE reading your study content. This primes your mind to actively search for the answers!"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Bind Views
        txtQuote = view.findViewById(R.id.txt_quote);
        countPending = view.findViewById(R.id.count_pending);
        countCompleted = view.findViewById(R.id.count_completed);
        txtProgressPercent = view.findViewById(R.id.txt_progress_percent);
        progressStudy = view.findViewById(R.id.progress_study);
        txtProgressAnalysis = view.findViewById(R.id.txt_progress_analysis);
        txtStudyTip = view.findViewById(R.id.txt_study_tip);

        // Bind Quick Assistant Buttons
        view.findViewById(R.id.btn_quick_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), AddEditTaskActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.btn_view_tasks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.navigation_tasks);
                    }
                }
            }
        });

        view.findViewById(R.id.btn_share_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCurrentTip();
            }
        });

        // Bind Study Tip Interactive Buttons
        view.findViewById(R.id.btn_copy_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTipToClipboard();
            }
        });

        view.findViewById(R.id.btn_next_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextTip();
            }
        });

        // Bind Timer views
        txtTimerStatus = view.findViewById(R.id.txt_timer_status);
        txtTimerCountdown = view.findViewById(R.id.txt_timer_countdown);
        btnTimerStart = view.findViewById(R.id.btn_timer_start);
        btnTimerReset = view.findViewById(R.id.btn_timer_reset);
        btnTimerMode = view.findViewById(R.id.btn_timer_mode);

        btnTimerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTimer();
            }
        });

        btnTimerReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        btnTimerMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTimerMode();
            }
        });

        dbHelper = new DatabaseHelper(requireContext());

        setupDynamicContent();
        updateTimerText();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatistics();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void setupDynamicContent() {
        Random random = new Random();
        String selectedQuote = motivationalQuotes[random.nextInt(motivationalQuotes.length)];
        String selectedTip = studyTips[random.nextInt(studyTips.length)];

        txtQuote.setText(selectedQuote);
        txtStudyTip.setText(selectedTip);
    }

    private void updateStatistics() {
        int pendingNum = dbHelper.getTasksCount("pending");
        int completedNum = dbHelper.getTasksCount("completed");
        int totalNum = pendingNum + completedNum;

        countPending.setText(String.valueOf(pendingNum));
        countCompleted.setText(String.valueOf(completedNum));

        int progressPercent = 0;
        if (totalNum > 0) {
            progressPercent = (int) (((double) completedNum / totalNum) * 100);
        }

        txtProgressPercent.setText(progressPercent + "%");
        progressStudy.setProgress(progressPercent);

        if (totalNum == 0) {
            txtProgressAnalysis.setText("Your task board is completely empty. Click 'Task List' then the '+' button to add your first academic task!");
        } else if (progressPercent == 100) {
            txtProgressAnalysis.setText("Amazing job! You have fully completed all academic tasks for this period! Enjoy your free time.");
        } else if (progressPercent >= 75) {
            txtProgressAnalysis.setText("You are almost done! Only a final sprint to finish your pending list.");
        } else if (progressPercent >= 50) {
            txtProgressAnalysis.setText("Halfway there! Keep up the incredible momentum, scholar.");
        } else {
            txtProgressAnalysis.setText("A journey of a thousand miles begins with a single step. Tackle a pending task today!");
        }
    }

    private void shareCurrentTip() {
        String tipText = txtStudyTip.getText().toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "💡 Scholar Study Tip of the Day:\n\n" + tipText + "\n\nShared via StudyMate 🎓");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Study Tip");
        startActivity(shareIntent);
    }

    private void copyTipToClipboard() {
        if (getContext() != null) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Study Tip", txtStudyTip.getText().toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "📋 Tip copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showNextTip() {
        Random random = new Random();
        String currentTip = txtStudyTip.getText().toString();
        String newTip = currentTip;
        while (newTip.equals(currentTip)) {
            newTip = studyTips[random.nextInt(studyTips.length)];
        }
        txtStudyTip.setText(newTip);
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        isTimerRunning = true;
        btnTimerStart.setText("PAUSE");
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        isTimerRunning = false;
        btnTimerStart.setText("START");
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void resetTimer() {
        stopTimer();
        if (isWorkMode) {
            timeLeftSeconds = 25 * 60;
        } else {
            timeLeftSeconds = 5 * 60;
        }
        updateTimerText();
    }

    private void toggleTimerMode() {
        if (isWorkMode) {
            setBreakMode();
        } else {
            setWorkMode();
        }
    }

    private void setWorkMode() {
        isWorkMode = true;
        timeLeftSeconds = 25 * 60;
        txtTimerStatus.setText("FOCUS SESSION");
        txtTimerStatus.setTextColor(getResources().getColor(R.color.primary));
        btnTimerMode.setText("BREAK MODE");
        updateTimerText();
        if (isTimerRunning) {
            stopTimer();
        }
    }

    private void setBreakMode() {
        isWorkMode = false;
        timeLeftSeconds = 5 * 60;
        txtTimerStatus.setText("RECHARGE BREAK");
        txtTimerStatus.setTextColor(getResources().getColor(R.color.status_pending));
        btnTimerMode.setText("FOCUS MODE");
        updateTimerText();
        if (isTimerRunning) {
            stopTimer();
        }
    }

    private void updateTimerText() {
        int minutes = timeLeftSeconds / 60;
        int seconds = timeLeftSeconds % 60;
        String timeString = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);
        txtTimerCountdown.setText(timeString);
    }
}
