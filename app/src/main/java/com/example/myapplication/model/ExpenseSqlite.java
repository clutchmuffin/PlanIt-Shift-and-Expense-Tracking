package com.example.myapplication.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ExpenseSqlite extends SQLiteOpenHelper {
    public ExpenseSqlite(@Nullable Context context) {
        super(context, "lastStraw", null, 1);


    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        database.execSQL("create table expense(id INTEGER primary key autoincrement, amount DOUBLE, reason TEXT, time DOUBLE)");
        database.execSQL("create table income(id INTEGER primary key autoincrement, amount DOUBLE, reason TEXT, time DOUBLE)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        database.execSQL("drop table if exists expense");
        database.execSQL("drop table if exists income");
    }

    public void addExpense(double amount, String reason) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("reason", reason);
        values.put("time", System.currentTimeMillis());

        database.insert("expense", null, values);

    }

    public double showExpense(String Reason) {


        double totalExpense = 0;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from expense WHERE reason=?", new String[]{Reason});

        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                double expense = cursor.getDouble(1);

                totalExpense = totalExpense + expense;
            }

        }

        return totalExpense;
    }


    public void addIncome(double amount, String reason) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("reason", reason);
        values.put("time", System.currentTimeMillis());

        database.insert("income", null, values);

    }


    public double FoodExpense(String Category){

        return showExpense(Category);

    }

    public double ShoppingExpense(String Category){

        return showExpense(Category);

    }

    public double TravelingExpense(String Category){

        return showExpense(Category);

    }

    public double EntertainmentExpense(String Category){

        return showExpense(Category);

    }


    public double showIncome() {


        double totalIncome = 0;

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from income", null);

        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                double income = cursor.getDouble(1);

                totalIncome = totalIncome + income;
            }

        }

        return totalIncome;
    }

    public Cursor showExpenseRecyclerView(String reason){

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("select * from expense WHERE reason=?",new String[]{reason} );

        return cursor;
    }

    public Cursor showIncomeRecyclerView(){

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("select * from income", null);

        return cursor;
    }

    public void updateData(String reason,String amount,String id){

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("reason",reason);
        values.put("amount",amount);

        sqLiteDatabase.update("expense",values,"id=?",new String[]{id});

    }

    public void deleteData(String id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete("expense", "id=?", new String[]{id});
        sqLiteDatabase.close();
    }

}


