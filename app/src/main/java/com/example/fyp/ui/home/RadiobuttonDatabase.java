package com.example.fyp.ui.home;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class RadiobuttonDatabase extends SQLiteOpenHelper {
    public RadiobuttonDatabase(@Nullable Context context) {
        super(context, "rb.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE `radiobutton` ( `option` int )";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertData(int option) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete("radiobutton", null, null);
        ContentValues contentValues = new ContentValues();
        contentValues.put("option", option);
        sqLiteDatabase.insert("radiobutton", null, contentValues);
        sqLiteDatabase.close();
    }

    public Cursor getData() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM `radiobutton`", null);
    }

    public void deleteDatabase() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete("radiobutton", null, null);
        sqLiteDatabase.close();
    }
}
