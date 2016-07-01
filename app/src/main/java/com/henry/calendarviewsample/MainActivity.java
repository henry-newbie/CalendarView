package com.henry.calendarviewsample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.henry.calendarview.DatePickerController;
import com.henry.calendarview.DayPickerView;
import com.henry.calendarview.SimpleMonthAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DayPickerView dayPickerView;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        dayPickerView = (DayPickerView) findViewById(R.id.dpv_calendar);


        DayPickerView.DataModel dataModel = new DayPickerView.DataModel();
        dataModel.monthCount = 16;
        dataModel.defTag = "￥100";

        List<SimpleMonthAdapter.CalendarDay> invalidDays = new ArrayList<>();
        SimpleMonthAdapter.CalendarDay calendarDay = new SimpleMonthAdapter.CalendarDay(2016, 8, 10);
        invalidDays.add(calendarDay);
        dataModel.busyDays = invalidDays;

        SimpleMonthAdapter.CalendarDay startDay = new SimpleMonthAdapter.CalendarDay(2016, 7, 5);
        SimpleMonthAdapter.CalendarDay endDay = new SimpleMonthAdapter.CalendarDay(2016, 7, 20);
        SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays = new SimpleMonthAdapter.SelectedDays<>(startDay, endDay);
        dataModel.selectedDays = selectedDays;

        SimpleMonthAdapter.CalendarDay tag = new SimpleMonthAdapter.CalendarDay(2016, 7, 15);
        tag.setTag("标签1");

        SimpleMonthAdapter.CalendarDay tag2 = new SimpleMonthAdapter.CalendarDay(2016, 8, 15);
        tag2.setTag("标签2");
        List<SimpleMonthAdapter.CalendarDay> tags = new ArrayList<>();
        tags.add(tag);
        tags.add(tag2);
        dataModel.tags = tags;

        dayPickerView.setParameter(dataModel, new DatePickerController() {
            @Override
            public void onDayOfMonthSelected(int year, int month, int day) {
                Toast.makeText(context, "onDayOfMonthSelected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDateRangeSelected(List<SimpleMonthAdapter.CalendarDay> selectedDays) {
                Toast.makeText(context, "onDateRangeSelected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void alertSelectedFail(FailEven even) {
                Toast.makeText(context, "alertSelectedFail", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
