package com.example.myapplication.view.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class JobListSelectableAdapter extends RecyclerView.Adapter<JobListSelectableAdapter.JobListSelectableViewHolder> {

    @NonNull
    @Override
    public JobListSelectableAdapter.JobListSelectableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull JobListSelectableAdapter.JobListSelectableViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class JobListSelectableViewHolder extends RecyclerView.ViewHolder {

        public JobListSelectableViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
