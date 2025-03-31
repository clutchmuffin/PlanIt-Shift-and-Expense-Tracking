package com.example.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Job;
import com.example.myapplication.controller.JobDetailActivity;
import com.example.myapplication.model.NotificationSender;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.N;

import java.util.List;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> {

    private static final String TAG = "MainActivity";
    private List<Job> jobs;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public JobListAdapter(List<Job> jobs, Context context) {
        this.jobs = jobs;
        this.context = context;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.jobTitle.setText(job.getTitle());
        holder.jobEmployer.setText(job.getEmployer());

        int color = job.getColor();
        holder.jobCard.setCardBackgroundColor(Color.argb(128,
                Color.red(color),
                Color.green(color),
                Color.blue(color)));

        // Set click listener to open JobDetailActivity.
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
            intent.putExtra(JobDetailActivity.EXTRA_JOB, job);
            v.getContext().startActivity(intent);
        });

        holder.jobDelete.setOnClickListener(
                v -> {
                    if (job.getJobId() != null) {
                        db.collection("Jobs").document(job.getJobId())
                                .collection("Events").get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            CalendarEvent event = document.toObject(CalendarEvent.class);
                                            cancelNotification(event);
                                        }
                                    }
                                });
                        db.collection("Jobs").document(job.getJobId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Delete from local list and update RecyclerView
                                    jobs.remove(position);
                                    notifyItemRemoved(position);
                                    Log.d(TAG, "Job successfully deleted from Firestore");
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error deleting job from Firestore", e));
                    } else {
                        Log.e(TAG, "Cannot delete job: jobId is null");
                    }
                }
        );
    }


    @Override
    public int getItemCount() {
        return jobs.size();
    }

    private void cancelNotification(CalendarEvent event){
        NotificationSender notifSender = new NotificationSender(context);
        notifSender.cancelNotification(event);
        notifSender.updateWeeklyNotif();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobEmployer;
        MaterialButton jobDelete;
        MaterialCardView jobCard;
        View colorAccent;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobEmployer = itemView.findViewById(R.id.jobEmployer);
            jobDelete = itemView.findViewById(R.id.jobDeleteBtn);
            jobCard = itemView.findViewById(R.id.jobCard);
            colorAccent = itemView.findViewById(R.id.colorAccent);
        }
    }
}


