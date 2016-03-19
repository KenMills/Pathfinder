package com.example.android.pathfinder;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by kenm on 3/3/2016.
 */
public class SetTime implements TimePickerDialog.OnTimeSetListener {

    private Button timeButton;
    private Calendar myCalendar;
    private Context mContext;

    public static String KEY_HOUR   = "Hour";
    public static String KEY_MINUTE = "Minute";

    public interface SetTimeCompleteCallback{
        void onSetTimeComplete(Bundle bundle);
    }

    SetTimeCompleteCallback callback;

    public SetTime(Button timeButton, Context ctx, SetTimeCompleteCallback callback){
        this.timeButton = timeButton;
        this.myCalendar = Calendar.getInstance();
        this.mContext = ctx;
        this.callback = callback;

        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);
        new TimePickerDialog(mContext, this, hour, minute, true).show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // TODO Auto-generated method stub
        this.timeButton.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_HOUR, hourOfDay);
        bundle.putInt(KEY_MINUTE, minute);
        callback.onSetTimeComplete(bundle);
    }
}
