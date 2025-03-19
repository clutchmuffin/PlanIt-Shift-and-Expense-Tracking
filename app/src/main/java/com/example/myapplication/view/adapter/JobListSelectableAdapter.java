package com.example.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.JobSelectable;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class JobListSelectableAdapter extends RecyclerView.Adapter<JobListSelectableAdapter.JobListSelectableViewHolder> {
    private List<JobSelectable> jobs;
    @NonNull
    @Override
    public JobListSelectableAdapter.JobListSelectableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_selectable, parent, false);
        return new JobListSelectableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobListSelectableAdapter.JobListSelectableViewHolder holder, int position) {
        JobSelectable job = jobs.get(position);
        holder.jobTitle.setText(job.getTitle());
        holder.jobEmployer.setText(job.getEmployer());

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class JobListSelectableViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, jobEmployer;
        MaterialCardView jobCard;
        View colorAccent;
        public JobListSelectableViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
