package com.example.myapplication.view.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Job;
import com.example.myapplication.controller.JobDetailActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> {
    private List<Job> jobs;

    public JobListAdapter(List<Job> jobs) {
        this.jobs = jobs;
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
        holder.jobCard.setBackgroundColor(Color.argb(128,
                Color.red(color),
                Color.green(color),
                Color.blue(color)));

        // Set click listener to open JobDetailActivity.
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
            intent.putExtra(JobDetailActivity.EXTRA_JOB, job);
            v.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return jobs.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobEmployer;
        MaterialCardView jobCard;
        View colorAccent;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.jobTitle);
            jobEmployer = itemView.findViewById(R.id.jobEmployer);
            jobCard = itemView.findViewById(R.id.jobCard);
            colorAccent = itemView.findViewById(R.id.colorAccent);
        }
    }
}


