package com.example.jumpjudge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.database.Cursor;
import android.widget.TextView;
import android.text.format.DateUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.datatype.Duration;

public class RiderCursorAdapter extends CursorAdapter {

    RiderCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView numberTextView = view.findViewById(R.id.number);
        TextView divisionTextView = view.findViewById(R.id.division);
        TextView otherTextView = view.findViewById(R.id.other);
        TextView editTextView = view.findViewById(R.id.edited);
        TextView refusalsView = view.findViewById(R.id.refusals);
        TextView summaryTextView = view.findViewById(R.id.summary);

        int numberColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_NUM);
        int divisionColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_DIVISION);
        int otherColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_OTHER);
        int timeColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_TIME);
        int holdColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_HOLD);
        int refusalsColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_REFUSALS);
        int editedColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_EDIT);

        String riderNumber = cursor.getString(numberColumnIndex);
        String division = cursor.getString(divisionColumnIndex);
        String other = cursor.getString(otherColumnIndex);
        String editRaw = cursor.getString(editedColumnIndex);
        long jumpTimeRaw = cursor.getLong(timeColumnIndex);
        long holdTimeRaw = cursor.getLong(holdColumnIndex);
        String jumpTime = "Time: " + formatTime(jumpTimeRaw) + " Hold: " + DateUtils.formatElapsedTime(holdTimeRaw/1000);
        String refusals = cursor.getString(refusalsColumnIndex);

        numberTextView.setText(riderNumber);
        divisionTextView.setText(division);
        otherTextView.setText(other);
        refusalsView.setText(refusals);
        if(editRaw != null)
            editTextView.setText("Edited");
        summaryTextView.setText(jumpTime);
    }

    private String formatTime(long timeRaw) {
        Timestamp ts = new Timestamp(timeRaw);
        Date time = new Date(ts.getTime());
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        return format.format(time);
    }
}
