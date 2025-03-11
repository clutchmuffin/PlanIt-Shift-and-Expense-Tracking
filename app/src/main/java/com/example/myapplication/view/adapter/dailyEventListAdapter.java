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

public class dailyEventListAdapter extends RecyclerView.Adapter<dailyEventListAdapter.dailyEventViewHolder> {

    private List<CalendarEvent> dailyEvents;


    public dailyEventListAdapter(List<CalendarEvent> events) {
        this.dailyEvents = events != null ? events : new ArrayList<>();
    }

    @NonNull
    @Override
    public dailyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new dailyEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull dailyEventViewHolder holder, int position) {
        CalendarEvent event = dailyEvents.get(position);
        holder.tvDateRange.setText(event.getBegin_date().substring(5) + " to " + event.getEnd_date().substring(5));
        holder.tvName.setText(event.getName());
        holder.tvTimeRange.setText(event.getBegin_time().substring(0,5) + " - " + event.getEnd_time().substring(0,5));
        holder.tvRepeatType.setText(event.getRepeated().toString());
        holder.tvNetPay.setText("$" + event.calculatePay());
    }

    @Override
    public int getItemCount() {
        return dailyEvents.size();
    }

    public static class dailyEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateRange, tvName, tvTimeRange, tvRepeatType, tvNetPay;

        public dailyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateRange = itemView.findViewById(R.id.eventDateRange);
            tvName = itemView.findViewById(R.id.eventName);
            tvTimeRange = itemView.findViewById(R.id.eventTimeRange);
            tvRepeatType = itemView.findViewById(R.id.eventRepeatInfo);
            tvNetPay = itemView.findViewById(R.id.netPay);
        }
    }
}
