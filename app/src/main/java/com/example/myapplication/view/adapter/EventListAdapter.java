package com.example.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.CalendarEvent;

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
    private List<CalendarEvent> events;

    public EventListAdapter(List<CalendarEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);
        holder.tvDateRange.setText(event.getBegin_date() + " - " + event.getEnd_date());
        holder.tvName.setText(event.getName());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange, tvName;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.eventDateRange);
            tvName = itemView.findViewById(R.id.eventName);
        }
    }
}
