package com.example.android.pathfinder;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by kenm on 3/3/2016.
 */
public class SetDate implements View.OnFocusChangeListener, DatePickerDialog.OnDateSetListener {

    private Button dateButton;
    private Calendar mCalendar;
    private Context mContext;

    public static String KEY_YEAR   = "Year";
    public static String KEY_MONTH  = "Month";
    public static String KEY_DAY    = "Day";

    public interface SetDateCompleteCallback{
        void onSetDateComplete(Bundle bundle);
    }

    SetDateCompleteCallback callback;

    public SetDate(Button dateBtn, Context ctx, SetDateCompleteCallback callback){
        dateButton = dateBtn;
        dateButton.setOnFocusChangeListener(this);
        mCalendar = Calendar.getInstance();
        mContext = ctx;
        this.callback = callback;

        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(mContext, this, year, month, day).show();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // TODO Auto-generated method stub
        if(hasFocus){
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        dateButton.setText( monthOfYear + "/" + dayOfMonth + "/" + year);

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_YEAR, year);
        bundle.putInt(KEY_MONTH, monthOfYear);
        bundle.putInt(KEY_DAY, dayOfMonth);
        callback.onSetDateComplete(bundle);
    }
}
