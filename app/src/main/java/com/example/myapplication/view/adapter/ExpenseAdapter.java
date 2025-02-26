package com.example.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.myapplication.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.myapplication.controller.UpdateActivity;
import com.example.myapplication.model.ExpenseModel;
import com.example.myapplication.model.ExpenseSqlite;

import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    ArrayList<ExpenseModel> arrayList = new ArrayList<>();
    Context context;

    public ExpenseAdapter(ArrayList<ExpenseModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {



        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.data_list, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ExpenseModel expense = arrayList.get(position);
        holder.tvReason.setText(arrayList.get(position).getReason());
        holder.tvBuy.setText(arrayList.get(position).getBuy());

        holder.updtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("id", expense.getId());
                intent.putExtra("amount", expense.getBuy());
                intent.putExtra("reason", expense.getReason());

                // If context is not an Activity, add FLAG_ACTIVITY_NEW_TASK
                if (!(context instanceof android.app.Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                context.startActivity(intent);
            }
        });

        holder.delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpenseSqlite db = new ExpenseSqlite(context);
                db.deleteData(String.valueOf(expense.getId()));

                arrayList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, arrayList.size());
            }
        });


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBuy, tvReason;
        Button updtButton, delButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBuy = itemView.findViewById(R.id.tvBuy);
            tvReason = itemView.findViewById(R.id.tvReason);
            updtButton = itemView.findViewById(R.id.updtButton);
            delButton = itemView.findViewById(R.id.delButton);


        }
    }


}
