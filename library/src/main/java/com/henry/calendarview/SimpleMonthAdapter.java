package com.henry.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SimpleMonthAdapter extends RecyclerView.Adapter<SimpleMonthAdapter.ViewHolder> implements SimpleMonthView.OnDayClickListener {
    protected static final int MONTHS_IN_YEAR = 12;
    private final TypedArray typedArray;
    private final Context mContext;
    private final DatePickerController mController;             // 回调
    private Calendar calendar;
    private SelectedDays<CalendarDay> rangeDays;                // 选择日期范围

    private List<CalendarDay> mBusyDays;                        // 被占用的日期
    private List<CalendarDay> mTags;                            // 日期下面的标签
    private String mDefTag;                                     // 默认标签

    private int mLeastDaysNum;                                  // 至少选择几天
    private int mMostDaysNum;                                   // 至多选择几天

    private List<CalendarDay> mInvalidDays;                     // 无效的日期

    private CalendarDay mNearestDay;                            // 比离入住日期大且是最近的已被占用或者无效日期

    private DayPickerView.DataModel dataModel;

    public SimpleMonthAdapter(Context context, TypedArray typedArray, DatePickerController datePickerController, DayPickerView.DataModel dataModel) {
        mContext = context;
        this.typedArray = typedArray;
        mController = datePickerController;
        this.dataModel = dataModel;

//        // 今天是否默认选中
//        if (typedArray.getBoolean(R.styleable.DayPickerView_currentDaySelected, false))
//            onDayTapped(new CalendarDay(System.currentTimeMillis()));
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        calendar = Calendar.getInstance();

        if (dataModel.invalidDays == null) {
            dataModel.invalidDays = new ArrayList<>();
        }

        if (dataModel.busyDays == null) {
            dataModel.busyDays = new ArrayList<>();
        }

        if (dataModel.tags == null) {
            dataModel.tags = new ArrayList<>();
        }

        if (dataModel.selectedDays == null) {
            dataModel.selectedDays = new SelectedDays<>();
        }

        if (dataModel.yearStart <= 0) {
            dataModel.yearStart = calendar.get(Calendar.YEAR);
        }
        if (dataModel.monthStart <= 0) {
            dataModel.monthStart = calendar.get(Calendar.MONTH);
        }

        if (dataModel.leastDaysNum <= 0) {
            dataModel.leastDaysNum = 0;
        }

        if (dataModel.mostDaysNum <= 0) {
            dataModel.mostDaysNum = 100;
        }

        if (dataModel.leastDaysNum > dataModel.mostDaysNum) {
            Log.e("error", "可选择的最小天数不能小于最大天数");
            throw new IllegalArgumentException("可选择的最小天数不能小于最大天数");
        }

        if(dataModel.monthCount <= 0) {
            dataModel.monthCount = 12;
        }

        if(dataModel.defTag == null) {
            dataModel.defTag = "标签";
        }

        mLeastDaysNum = dataModel.leastDaysNum;
        mMostDaysNum = dataModel.mostDaysNum;

        mBusyDays = dataModel.busyDays;
        mInvalidDays = dataModel.invalidDays;
        rangeDays = dataModel.selectedDays;
        mTags = dataModel.tags;
        mDefTag = dataModel.defTag;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final SimpleMonthView simpleMonthView = new SimpleMonthView(mContext, typedArray, dataModel);
        return new ViewHolder(simpleMonthView, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final SimpleMonthView v = viewHolder.simpleMonthView;
        final HashMap<String, Object> drawingParams = new HashMap<String, Object>();
        int month;          // 月份
        int year;           // 年份

        int monthStart = dataModel.monthStart;
        int yearStart = dataModel.yearStart;

        month = (monthStart + (position % MONTHS_IN_YEAR)) % MONTHS_IN_YEAR;
        year = position / MONTHS_IN_YEAR + yearStart + ((monthStart + (position % MONTHS_IN_YEAR)) / MONTHS_IN_YEAR);

//        v.reuse();

        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_DATE, rangeDays.getFirst());
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_LAST_DATE, rangeDays.getLast());
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_NEAREST_DATE, mNearestDay);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_WEEK_START, calendar.getFirstDayOfWeek());
        v.setMonthParams(drawingParams);
        v.invalidate();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dataModel.monthCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final SimpleMonthView simpleMonthView;

        public ViewHolder(View itemView, SimpleMonthView.OnDayClickListener onDayClickListener) {
            super(itemView);
            simpleMonthView = (SimpleMonthView) itemView;
            simpleMonthView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            simpleMonthView.setClickable(true);
            simpleMonthView.setOnDayClickListener(onDayClickListener);
        }
    }

    @Override
    public void onDayClick(SimpleMonthView simpleMonthView, CalendarDay calendarDay) {
        if (calendarDay != null) {
            onDayTapped(calendarDay);
        }
    }

    /**
     * 点时间
     *
     * @param calendarDay
     */
    protected void onDayTapped(CalendarDay calendarDay) {
        if(mController != null) {
            mController.onDayOfMonthSelected(calendarDay);
        }
        setRangeSelectedDay(calendarDay);
    }

    /**
     * 范围选时对点击的日期的处理
     *
     * @param calendarDay
     */
    public void setRangeSelectedDay(CalendarDay calendarDay) {
        // 选择退房日期
        if (rangeDays.getFirst() != null && rangeDays.getLast() == null) {
            // 把比离入住日期大且是最近的已被占用或者无效日期找出来
            mNearestDay = getNearestDay(rangeDays.getFirst());
            // 所选日期范围内是否有被占用的日期
            if (isContainSpecialDays(rangeDays.getFirst(), calendarDay, mBusyDays)) {
                if(mController != null) {
                    mController.alertSelectedFail(DatePickerController.FailEven.CONTAIN_NO_SELECTED);
                }
                return;
            }
            // 所选日期范围内是否有无效的日期
            if (isContainSpecialDays(rangeDays.getFirst(), calendarDay, mInvalidDays)) {
                if(mController != null) {
                    mController.alertSelectedFail(DatePickerController.FailEven.CONTAIN_INVALID);
                }
                return;
            }
            // 所选退房日期不能再入住日期之前
            if (calendarDay.getDate().before(rangeDays.getFirst().getDate())) {
                if(mController != null) {
                    mController.alertSelectedFail(DatePickerController.FailEven.END_MT_START);
                }
                return;
            }

            int dayDiff = dateDiff(rangeDays.getFirst(), calendarDay);
            // 所选的日期范围不能小于最小限制
            if (dayDiff > 1 && mLeastDaysNum > dayDiff) {
                if(mController != null) {
                    mController.alertSelectedFail(DatePickerController.FailEven.NO_REACH_LEAST_DAYS);
                }
                return;
            }
            // 所选日期范围不能大于最大限制
            if (dayDiff > 1 && mMostDaysNum < dayDiff) {
                if(mController != null) {
                    mController.alertSelectedFail(DatePickerController.FailEven.NO_REACH_MOST_DAYS);
                }
                return;
            }

            rangeDays.setLast(calendarDay);

            // 把开始日期和结束日期中间的所有日期都加到list中
            if(mController != null) {
                mController.onDateRangeSelected(addSelectedDays());
            }
        } else if (rangeDays.getLast() != null) {   // 重新选择入住日期
            rangeDays.setFirst(calendarDay);
            rangeDays.setLast(null);
            // 离该天最近的已定或者禁用日期找出来
            mNearestDay = getNearestDay(calendarDay);
        } else {        // 第一次选择入住日期
            rangeDays.setFirst(calendarDay);
            // 离该天最近的已定或者禁用日期找出来
            mNearestDay = getNearestDay(calendarDay);
        }

        notifyDataSetChanged();
    }

    /**
     * 把比离入住日期大且是最近的已被占用或者无效日期找出来
     *
     * @param calendarDay 入住日期
     * @return
     */
    protected CalendarDay getNearestDay(CalendarDay calendarDay) {
        List<CalendarDay> list = new ArrayList<>();
        list.addAll(mBusyDays);
        list.addAll(mInvalidDays);
        Collections.sort(list);
        for (CalendarDay day : list) {
            if (calendarDay.compareTo(day) < 0) {
                return day;
            }
        }
        return null;
    }

    public SelectedDays<CalendarDay> getRangeDays() {
        return rangeDays;
    }

    /**
     * 判断选择的日期范围是否包含有特殊的日期（无效的或者已被占用的日期）
     *
     * @param first
     * @param last
     * @param specialDays
     * @return
     */
    protected boolean isContainSpecialDays(CalendarDay first, CalendarDay last, List<CalendarDay> specialDays) {
        Date firstDay = first.getDate();
        Date lastDay = last.getDate();
        for (CalendarDay day : specialDays) {
            if (day.getDate().after(firstDay) && day.getDate().before(lastDay)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 两个日期中间隔多少天
     *
     * @param first
     * @param last
     * @return
     */
    protected int dateDiff(CalendarDay first, CalendarDay last) {
        long dayDiff = (last.getDate().getTime() - first.getDate().getTime()) / (1000 * 3600 * 24);
        return Integer.valueOf(String.valueOf(dayDiff)) + 1;
    }

    /**
     * 范围选择时，把选中的所有日期加进list中
     *
     * @return
     */
    protected List<CalendarDay> addSelectedDays() {
        List<CalendarDay> rangeDays = new ArrayList<>();
        CalendarDay firstDay = this.rangeDays.getFirst();
        CalendarDay lastDay = this.rangeDays.getLast();
        rangeDays.add(firstDay);
        int diffDays = dateDiff(firstDay, lastDay);
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.setTime(firstDay.getDate());
        for (int i = 1; i < diffDays; i++) {
            tempCalendar.set(Calendar.DATE, tempCalendar.get(Calendar.DATE) + 1);
            CalendarDay calendarDay = new CalendarDay(tempCalendar);
            boolean isTag = false;
            for (CalendarDay calendarTag : mTags) {
                if (calendarDay.compareTo(calendarTag) == 0) {
                    isTag = true;
                    rangeDays.add(calendarTag);
                    break;
                }
            }
            if (!isTag) {
                calendarDay.tag = mDefTag;
                rangeDays.add(calendarDay);
            }
        }
        return rangeDays;
    }

    /**
     * 设置数据集
     *
     * @param dataModel
     */
    protected void setDataModel(DayPickerView.DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public static class CalendarDay implements Serializable, Comparable<CalendarDay> {
        private static final long serialVersionUID = -5456695978688356202L;
        private Calendar calendar;

        public int day;
        public int month;
        public int year;
        public String tag;

        public CalendarDay(Calendar calendar, String tag) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            this.tag = tag;
        }

        public CalendarDay() {
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }

        public CalendarDay(long timeInMillis) {
            setTime(timeInMillis);
        }

        public CalendarDay(Calendar calendar) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        private void setTime(long timeInMillis) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.setTimeInMillis(timeInMillis);
            month = this.calendar.get(Calendar.MONTH);
            year = this.calendar.get(Calendar.YEAR);
            day = this.calendar.get(Calendar.DAY_OF_MONTH);
        }

        public void set(CalendarDay calendarDay) {
            year = calendarDay.year;
            month = calendarDay.month;
            day = calendarDay.day;
        }

        public void setDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public Date getDate() {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.clear();
            calendar.set(year, month, day);
            return calendar.getTime();
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{ year: ");
            stringBuilder.append(year);
            stringBuilder.append(", month: ");
            stringBuilder.append(month);
            stringBuilder.append(", day: ");
            stringBuilder.append(day);
            stringBuilder.append(" }");

            return stringBuilder.toString();
        }

        /**
         * 只比较年月日
         *
         * @param calendarDay
         * @return
         */
        @Override
        public int compareTo(CalendarDay calendarDay) {
//            return getDate().compareTo(calendarDay.getDate());
            if (calendarDay == null) {
                throw new IllegalArgumentException("被比较的日期不能是null");
            }

            if (year == calendarDay.year && month == calendarDay.month && day == calendarDay.day) {
                return 0;
            }

            if (year < calendarDay.year ||
                    (year == calendarDay.year && month < calendarDay.month) ||
                    (year == calendarDay.year && month == calendarDay.month && day < calendarDay.day)) {
                return -1;
            }
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == 0) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 大于比较的日期（只比较年月日）
         *
         * @param o
         * @return
         */
        public boolean after(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == 1) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 小于比较的日期（只比较年月日）
         *
         * @param o
         * @return
         */
        public boolean before(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == -1) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class SelectedDays<K> implements Serializable {
        private static final long serialVersionUID = 3942549765282708376L;
        private K first;
        private K last;

        public SelectedDays() {
        }

        public SelectedDays(K first, K last) {
            this.first = first;
            this.last = last;
        }

        public K getFirst() {
            return first;
        }

        public void setFirst(K first) {
            this.first = first;
        }

        public K getLast() {
            return last;
        }

        public void setLast(K last) {
            this.last = last;
        }
    }
}