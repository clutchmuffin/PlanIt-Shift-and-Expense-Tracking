package com.example.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.LoginActivity;
import com.example.myapplication.controller.SharedCalendarActivity;
import com.example.myapplication.model.CalendarEvent;
import com.example.myapplication.model.Job;
import com.example.myapplication.model.SharedCal;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class SharedEventAdapter extends RecyclerView.Adapter<SharedEventAdapter.SharedEventViewHolder> {
    private static final String TAG = "SharedEventAdapter";

    private List<CalendarEvent> events;
    private List<CalendarEvent> selected;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SharedEventAdapter(ArrayList<CalendarEvent> events) {
        this.events = events != null ? events : new ArrayList<>();
    }

    @NonNull
    @Override
    public SharedEventAdapter.SharedEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false);
        return new SharedEventAdapter.SharedEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedEventAdapter.SharedEventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);
        holder.tvDateRange.setText(event.getBegin_date().substring(5) + " to " + event.getEnd_date().substring(5));
        holder.tvName.setText(event.getName());
        holder.tvTimeRange.setText(event.getBegin_time().substring(0,5) + " - " + event.getEnd_time().substring(0,5));
        holder.tvRepeatType.setText(event.getRepeated().toString());
        holder.tvNetPay.setText("$" + event.calculatePay());

        holder.itemView.setOnClickListener(v -> {
            if (selected.contains(event)) {
                selected.remove(event);
                holder.checkbox.setSelected(false);
            }
            else {
                selected.add(event);
                holder.checkbox.setSelected(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public List<CalendarEvent> getSelected() {
        return selected;
    }

    static class SharedEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange, tvName, tvTimeRange, tvRepeatType, tvNetPay, checkbox;

        public SharedEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.eventDateRange);
            tvName = itemView.findViewById(R.id.eventName);
            tvTimeRange = itemView.findViewById(R.id.eventTimeRange);
            tvRepeatType = itemView.findViewById(R.id.eventRepeatInfo);
            tvNetPay = itemView.findViewById(R.id.netPay);
            checkbox = itemView.findViewById(R.id.checkBox);
        }
    }
}

