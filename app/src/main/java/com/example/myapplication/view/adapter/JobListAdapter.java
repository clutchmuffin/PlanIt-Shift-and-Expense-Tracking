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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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

        // Set button click listener to delete the job.
        holder.jobDelete.setOnClickListener(v -> {
            String jobId = job.getJobId();
            if (jobId == null) {
                Log.e(TAG, "Cannot delete job: jobId is null");
                return;
            }

            // Reference to the job document and its sub-collections
            DocumentReference jobRef = db.collection("Jobs").document(jobId);

            // Create a new write batch
            WriteBatch batch = db.batch();
            Task<QuerySnapshot> eventsTask = jobRef.collection("Events").get();
            Task<QuerySnapshot> expensesTask = jobRef.collection("EXP").get();

            // Wait for both tasks to complete successfully
            Tasks.whenAllSuccess(eventsTask, expensesTask)
                    .addOnSuccessListener(results -> {

                        // Process events
                        QuerySnapshot eventsSnapshot = (QuerySnapshot) results.get(0);
                        for (QueryDocumentSnapshot doc : eventsSnapshot) {
                            CalendarEvent event = doc.toObject(CalendarEvent.class);
                            cancelNotification(event);
                            batch.delete(doc.getReference());
                        }

                        // Process expenses
                        QuerySnapshot expensesSnapshot = (QuerySnapshot) results.get(1);
                        for (QueryDocumentSnapshot doc : expensesSnapshot) {
                            batch.delete(doc.getReference());
                        }

                        // Delete the job document itself
                        batch.delete(jobRef);

                        // Commit the batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    // Remove the job from the local list and update RecyclerView
                                    jobs.remove(position);
                                    notifyItemRemoved(position);
                                    Log.d(TAG, "Job and all subcollections successfully deleted");
                                })
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error deleting job and subcollections", e)
                                );
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error fetching subcollections", e)
                    );
        });
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


