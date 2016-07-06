package com.henry.calendarview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * 每个月作为一个ItemView
 */
class SimpleMonthView extends View {

    public static final String VIEW_PARAMS_SELECTED_BEGIN_DATE = "selected_begin_date";
    public static final String VIEW_PARAMS_SELECTED_LAST_DATE = "selected_last_date";
    public static final String VIEW_PARAMS_NEAREST_DATE = "mNearestDay";

//    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    private static final int SELECTED_CIRCLE_ALPHA = 128;
    protected static int DEFAULT_HEIGHT = 32;                           // 默认一行的高度
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static int DAY_SELECTED_RECT_SIZE;                        // 选中圆角矩形半径
    protected static int ROW_SEPARATOR = 12;                            // 每行中间的间距
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;                     // 日期字体的最小尺寸
    private static int TAG_TEXT_SIZE;                                   // 标签字体大小
    protected static int MIN_HEIGHT = 10;                               // 最小高度
//    protected static int MONTH_DAY_LABEL_TEXT_SIZE;                     // 头部的星期几的字体大小
    protected static int MONTH_HEADER_SIZE;                             // 头部的高度（包括年份月份，星期几）
    protected static int YEAR_MONTH_TEXT_SIZE;                         // 头部年份月份的字体大小
    protected static int WEEK_TEXT_SIZE;                                // 头部年份月份的字体大小
    //    private final int mSelectType;                                // 类型
//    private boolean mIsDisplayTag;                                      // 是否显示标签

    private List<SimpleMonthAdapter.CalendarDay> mInvalidDays;          // 禁用的日期
    private List<SimpleMonthAdapter.CalendarDay> mBusyDays;             // 被占用的日期
    private SimpleMonthAdapter.CalendarDay mNearestDay;                 // 比离入住日期大且是最近的已被占用或者无效日期
    private List<SimpleMonthAdapter.CalendarDay> mCalendarTags;         // 日期下面的标签
    private String mDefTag = "标签";

    protected int mPadding = 0;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;

    protected Paint mWeekTextPaint;                     // 头部星期几的字体画笔
    protected Paint mDayTextPaint;
    protected Paint mTagTextPaint;                      // 日期底部的文字画笔
//    protected Paint mTitleBGPaint;
    protected Paint mYearMonthPaint;                    // 头部的画笔
    protected Paint mSelectedDayBgPaint;
    protected Paint mBusyDayBgPaint;
    protected Paint mInValidDayBgPaint;
//    protected Paint mSelectedDayTextPaint;
    protected int mCurrentDayTextColor;                 // 今天的字体颜色
    protected int mYearMonthTextColor;                  // 头部年份和月份字体颜色
    protected int mWeekTextColor;                       // 头部星期几字体颜色
//    protected int mDayTextColor;
    protected int mDayTextColor;                        // 日期字体颜色
    protected int mSelectedDayTextColor;                // 被选中的日期字体颜色
    protected int mPreviousDayTextColor;                // 过去的字体颜色
    protected int mSelectedDaysBgColor;                 // 选中的日期背景颜色
    protected int mBusyDaysBgColor;                     // 被占用的日期背景颜色
    protected int mInValidDaysBgColor;                  // 禁用的日期背景颜色
    protected int mBusyDaysTextColor;                     // 被占用的日期字体颜色
    protected int mInValidDaysTextColor;                  // 禁用的日期字体颜色

    private final StringBuilder mStringBuilder;

    protected boolean mHasToday = false;
    protected int mToday = -1;
    protected int mWeekStart = 1;               // 一周的第一天（不同国家的一星期的第一天不同）
    protected int mNumDays = 7;                 // 一行几列
    protected int mNumCells;                    // 一个月有多少天
    private int mDayOfWeekStart = 0;            // 日期对应星期几
//    protected Boolean mDrawRect;              // 圆角还是圆形
    protected int mRowHeight = DEFAULT_HEIGHT;  // 行高
    protected int mWidth;                       // simpleMonthView的宽度

    protected int mYear;
    protected int mMonth;
    final Time today;

    private final Calendar mCalendar;
    private final Calendar mDayLabelCalendar;           // 用于显示星期几
    private final Boolean isPrevDayEnabled;             // 今天以前的日期是否能被操作

    private int mNumRows;

    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();

    private OnDayClickListener mOnDayClickListener;

    SimpleMonthAdapter.CalendarDay mStartDate;          // 入住日期
    SimpleMonthAdapter.CalendarDay mEndDate;            // 退房日期

    SimpleMonthAdapter.CalendarDay cellCalendar;        // cell的对应的日期

    /**
     * @param context
     * @param typedArray
     * @param dataModel
     */
    public SimpleMonthView(Context context, TypedArray typedArray, DayPickerView.DataModel dataModel) {
        super(context);

        Resources resources = context.getResources();
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
        mMonthTitleTypeface = resources.getString(R.string.sans_serif);
        mCurrentDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorCurrentDay, resources.getColor(R.color.normal_day));
        mYearMonthTextColor = typedArray.getColor(R.styleable.DayPickerView_colorYearMonthText, resources.getColor(R.color.normal_day));
        mWeekTextColor = typedArray.getColor(R.styleable.DayPickerView_colorWeekText, resources.getColor(R.color.normal_day));
//        mDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorDayName, resources.getColor(R.color.normal_day));
        mDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorNormalDayText, resources.getColor(R.color.normal_day));
        mPreviousDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorPreviousDayText, resources.getColor(R.color.normal_day));
        mSelectedDaysBgColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayBackground, resources.getColor(R.color.selected_day_background));
        mSelectedDayTextColor = typedArray.getColor(R.styleable.DayPickerView_colorSelectedDayText, resources.getColor(R.color.selected_day_text));
        mBusyDaysBgColor = typedArray.getColor(R.styleable.DayPickerView_colorBusyDaysBg, Color.GRAY);
        mInValidDaysBgColor = typedArray.getColor(R.styleable.DayPickerView_colorInValidDaysBg, Color.GRAY);
        mBusyDaysTextColor = typedArray.getColor(R.styleable.DayPickerView_colorBusyDaysText, resources.getColor(R.color.normal_day));
        mInValidDaysTextColor = typedArray.getColor(R.styleable.DayPickerView_colorInValidDaysText, resources.getColor(R.color.normal_day));
//        mDrawRect = typedArray.getBoolean(R.styleable.DayPickerView_drawRoundRect, true);

        mStringBuilder = new StringBuilder(50);

        MINI_DAY_NUMBER_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeDay, resources.getDimensionPixelSize(R.dimen.text_size_day));
        TAG_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeTag, resources.getDimensionPixelSize(R.dimen.text_size_tag));
        YEAR_MONTH_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeYearMonth, resources.getDimensionPixelSize(R.dimen.text_size_month));
        WEEK_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_textSizeWeek, resources.getDimensionPixelSize(R.dimen.text_size_day_name));
        MONTH_HEADER_SIZE = typedArray.getDimensionPixelOffset(R.styleable.DayPickerView_headerMonthHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        DAY_SELECTED_RECT_SIZE = typedArray.getDimensionPixelSize(R.styleable.DayPickerView_selectedDayRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));

        mRowHeight = ((typedArray.getDimensionPixelSize(R.styleable.DayPickerView_calendarHeight,
                resources.getDimensionPixelOffset(R.dimen.calendar_height)) - MONTH_HEADER_SIZE - ROW_SEPARATOR) / 6);

        isPrevDayEnabled = typedArray.getBoolean(R.styleable.DayPickerView_enablePreviousDay, false);
        mInvalidDays = dataModel.invalidDays;
        mBusyDays = dataModel.busyDays;
        mCalendarTags = dataModel.tags;
        mDefTag = dataModel.defTag;

        cellCalendar = new SimpleMonthAdapter.CalendarDay();

        initView();
    }

    /**
     * 计算每个月的日期占用的行数
     *
     * @return
     */
    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    /**
     * 绘制头部的一行星期几
     *
     * @param canvas
     */
    private void drawMonthDayLabels(Canvas canvas) {
        int y = MONTH_HEADER_SIZE - (WEEK_TEXT_SIZE / 2);
        // 一个cell的二分之宽度
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()),
                    x, y, mWeekTextPaint);
        }
    }

    /**
     * 绘制头部（年份月份，星期几）
     *
     * @param canvas
     */
    private void drawMonthTitle(Canvas canvas) {
        int x = (mWidth + 2 * mPadding) / 2;
        int y = (MONTH_HEADER_SIZE - WEEK_TEXT_SIZE) / 2 + (YEAR_MONTH_TEXT_SIZE / 3);
        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(stringBuilder.toString(), x, y, mYearMonthPaint);
    }

    /**
     * 每个月第一天是星期几
     *
     * @return
     */
    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    /**
     * 获取年份和月份
     *
     * @return
     */
    private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mStringBuilder.setLength(0);
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

    private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
        if (mOnDayClickListener != null && (isPrevDayEnabled || !prevDay(calendarDay.day, today))) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
    }

    private boolean sameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }

    /**
     * 判断是否是已经过去的日期
     *
     * @param monthDay
     * @param time
     * @return
     */
    private boolean prevDay(int monthDay, Time time) {
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) ||
                (mYear == time.year && mMonth == time.month && monthDay < time.monthDay);
    }

    /**
     * 绘制所有的cell
     *
     * @param canvas
     */
    protected void drawMonthCell(Canvas canvas) {
        // ?
        int y = MONTH_HEADER_SIZE + ROW_SEPARATOR + mRowHeight / 2;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        int dayOffset = findDayOffset();
        int day = 1;

        while (day <= mNumCells) {
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;

            mDayTextPaint.setColor(mDayTextColor);
            mDayTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            mTagTextPaint.setColor(mDayTextColor);

            cellCalendar.setDay(mYear, mMonth, day);

            // 当天
            boolean isToady = false;
            if (mHasToday && (mToday == day)) {
                isToady = true;
                canvas.drawText("今天", x, getTextYCenter(mDayTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
            }
            // 已过去的日期
            boolean isPrevDay = false;
            if (!isPrevDayEnabled && prevDay(day, today)) {
                isPrevDay = true;
                mDayTextPaint.setColor(mPreviousDayTextColor);
                mDayTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                canvas.drawText(String.format("%d", day), x, y, mDayTextPaint);
            }

            boolean isBeginDay = false;
            // 绘制起始日期的方格
            if (mStartDate != null && cellCalendar.equals(mStartDate) && !mStartDate.equals(mEndDate)) {
                isBeginDay = true;
                drawDayBg(canvas, x, y, mSelectedDayBgPaint);
                mDayTextPaint.setColor(mSelectedDayTextColor);
                canvas.drawText("入住", x, getTextYCenter(mDayTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                if(isToady) {
                    canvas.drawText("今天", x, getTextYCenter(mDayTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                }
            }

            boolean isLastDay = false;
            // 绘制结束日期的方格
            if (mEndDate != null && cellCalendar.equals(mEndDate) && !mStartDate.equals(mEndDate)) {
                isLastDay = true;
                drawDayBg(canvas, x, y, mSelectedDayBgPaint);
                mDayTextPaint.setColor(mSelectedDayTextColor);
                canvas.drawText("退房", x, getTextYCenter(mDayTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
            }

            // 在入住和退房之间的日期
            if (cellCalendar.after(mStartDate) && cellCalendar.before(mEndDate)) {
                mDayTextPaint.setColor(mSelectedDayTextColor);
                drawDayBg(canvas, x, y, mSelectedDayBgPaint);
                // 标签变为白色
                mTagTextPaint.setColor(Color.WHITE);
//                canvas.drawText(String.format("%d", day), x, y - DAY_SELECTED_RECT_SIZE / 2, mDayTextPaint);
            }

            // 被占用的日期
            boolean isBusyDay = false;
            for (SimpleMonthAdapter.CalendarDay calendarDay : mBusyDays) {
                if (cellCalendar.equals(calendarDay) && !isPrevDay) {
                    isBusyDay = true;
//                    RectF rectF = new RectF(x - DAY_SELECTED_RECT_SIZE,
//                            (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_RECT_SIZE,
//                            x + DAY_SELECTED_RECT_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_RECT_SIZE);

                    // 选择了入住和退房日期，退房日期等于mNearestDay的情况
                    if (mStartDate != null && mEndDate != null && mNearestDay != null &&
                            mEndDate.equals(mNearestDay) && mEndDate.equals(calendarDay)) {

                    } else {
                        // 选择了入住日期，没有选择退房日期，mNearestDay变为可选且不变灰色
                        if (mStartDate != null && mEndDate == null && mNearestDay != null && cellCalendar.equals(mNearestDay)) {
                            mDayTextPaint.setColor(mDayTextColor);
                        } else {
                            drawDayBg(canvas, x, y, mBusyDayBgPaint);
//                            canvas.drawRoundRect(rectF, 10.0f, 10.0f, mBusyDayBgPaint);
                            mDayTextPaint.setColor(mBusyDaysTextColor);
                        }
                        canvas.drawText("已租", x, getTextYCenter(mBusyDayBgPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                    }
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                }
            }

            // 禁用的日期
            boolean isInvalidDays = false;
            for (SimpleMonthAdapter.CalendarDay calendarDay : mInvalidDays) {

                if (cellCalendar.equals(calendarDay) && !isPrevDay) {
                    isBusyDay = true;
//                    RectF rectF = new RectF(x - DAY_SELECTED_RECT_SIZE,
//                            (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_RECT_SIZE,
//                            x + DAY_SELECTED_RECT_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_RECT_SIZE);

                    // 选择了入住和退房日期，退房日期等于mNearestDay的情况
                    if (mStartDate != null && mEndDate != null && mNearestDay != null &&
                            mEndDate.equals(mNearestDay) && mEndDate.equals(calendarDay)) {

                    } else {
                        // 选择了入住日期，没有选择退房日期，mNearestDay变为可选且不变灰色
                        if (mStartDate != null && mEndDate == null && mNearestDay != null && cellCalendar.equals(mNearestDay)) {
                            mDayTextPaint.setColor(mDayTextColor);
                        } else {
                            drawDayBg(canvas, x, y, mInValidDayBgPaint);
//                            canvas.drawRoundRect(rectF, 10.0f, 10.0f, mBusyDayBgPaint);
                            mDayTextPaint.setColor(mInValidDaysTextColor);
                        }
                        canvas.drawText("禁用", x, getTextYCenter(mInValidDayBgPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                    }
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                }
            }

            // 把入住日期之前和不可用日期之后的日期全部灰掉(思路：
            // 1:入住日期和退房日期不能同一天
            // 2：只选择了入住日期且没有选择退房日期
            // 3：比入住日期小全部变灰且不可点击
            // 4：比入住日期大且离入住日期最近的被禁用或者被占用的日期
            if (mStartDate != null && mEndDate == null && !mStartDate.equals(mEndDate) && !isInvalidDays && !isBusyDay) {
                if (cellCalendar.before(mStartDate) || (mNearestDay != null && cellCalendar.after(mNearestDay))) {
//                    RectF rectF = new RectF(x - DAY_SELECTED_RECT_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) - DAY_SELECTED_RECT_SIZE,
//                            x + DAY_SELECTED_RECT_SIZE, (y - MINI_DAY_NUMBER_TEXT_SIZE / 3) + DAY_SELECTED_RECT_SIZE);
//                    canvas.drawRoundRect(rectF, 10.0f, 10.0f, mBusyDayBgPaint);
                    drawDayBg(canvas, x, y, mBusyDayBgPaint);
                }
            }

            // 绘制标签
            if (!isPrevDay && !isInvalidDays && !isBusyDay && !isBeginDay && !isLastDay) {
                boolean isCalendarTag = false;
                for (SimpleMonthAdapter.CalendarDay calendarDay : mCalendarTags) {
                    if (cellCalendar.equals(calendarDay)) {
                        isCalendarTag = true;
                        canvas.drawText(calendarDay.tag, x, getTextYCenter(mTagTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mTagTextPaint);
                    }
                }
                if (!isCalendarTag) {
                    canvas.drawText(mDefTag, x, getTextYCenter(mTagTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mTagTextPaint);
                }
            }

            // 绘制日期
            if (!isToady && !isPrevDay && !isInvalidDays && !isBusyDay) {
                canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
    }

    /**
     * 根据坐标获取对应的日期
     * @param x
     * @param y
     * @return
     */
    public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        SimpleMonthAdapter.CalendarDay calendar = new SimpleMonthAdapter.CalendarDay(mYear, mMonth, day);

        // 获取日期下面的tag
        boolean flag = false;
        for (SimpleMonthAdapter.CalendarDay calendarTag : mCalendarTags) {
            if (calendarTag.compareTo(calendar) == 0) {
                flag = true;
                calendar = calendarTag;
            }
        }
        if (!flag) {
            calendar.tag = mDefTag;
        }
        return calendar;
    }

    /**
     * 初始化一些paint
     */
    protected void initView() {
        // 头部年份和月份的字体paint
        mYearMonthPaint = new Paint();
        mYearMonthPaint.setFakeBoldText(true);
        mYearMonthPaint.setAntiAlias(true);
        mYearMonthPaint.setTextSize(YEAR_MONTH_TEXT_SIZE);
        mYearMonthPaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mYearMonthPaint.setColor(mYearMonthTextColor);
        mYearMonthPaint.setTextAlign(Align.CENTER);
        mYearMonthPaint.setStyle(Style.FILL);

        // 头部星期几字体paint
        mWeekTextPaint = new Paint();
        mWeekTextPaint.setAntiAlias(true);
        mWeekTextPaint.setTextSize(WEEK_TEXT_SIZE);
        mWeekTextPaint.setColor(mWeekTextColor);
        mWeekTextPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mWeekTextPaint.setStyle(Style.FILL);
        mWeekTextPaint.setTextAlign(Align.CENTER);
        mWeekTextPaint.setFakeBoldText(true);

//        // 头部背景paint
//        mTitleBGPaint = new Paint();
//        mTitleBGPaint.setFakeBoldText(true);
//        mTitleBGPaint.setAntiAlias(true);
//        mTitleBGPaint.setColor(mSelectedDayTextColor);
//        mTitleBGPaint.setTextAlign(Align.CENTER);
//        mTitleBGPaint.setStyle(Style.FILL);

        // 被选中的日期背景paint
        mSelectedDayBgPaint = new Paint();
        mSelectedDayBgPaint.setFakeBoldText(true);
        mSelectedDayBgPaint.setAntiAlias(true);
        mSelectedDayBgPaint.setColor(mSelectedDaysBgColor);
        mSelectedDayBgPaint.setTextAlign(Align.CENTER);
        mSelectedDayBgPaint.setStyle(Style.FILL);
        mSelectedDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        // 被占用的日期paint
        mBusyDayBgPaint = new Paint();
        mBusyDayBgPaint.setFakeBoldText(true);
        mBusyDayBgPaint.setAntiAlias(true);
        mBusyDayBgPaint.setColor(mBusyDaysBgColor);
        mBusyDayBgPaint.setTextSize(TAG_TEXT_SIZE);
        mBusyDayBgPaint.setTextAlign(Align.CENTER);
        mBusyDayBgPaint.setStyle(Style.FILL);
        mBusyDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        // 禁用的日期paint
        mInValidDayBgPaint = new Paint();
        mInValidDayBgPaint.setFakeBoldText(true);
        mInValidDayBgPaint.setAntiAlias(true);
        mInValidDayBgPaint.setColor(mInValidDaysBgColor);
        mInValidDayBgPaint.setTextSize(TAG_TEXT_SIZE);
        mInValidDayBgPaint.setTextAlign(Align.CENTER);
        mInValidDayBgPaint.setStyle(Style.FILL);
        mInValidDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);

//        // 被选中的日期字体paint
//        mSelectedDayTextPaint = new Paint();
//        mSelectedDayTextPaint.setAntiAlias(true);
//        mSelectedDayTextPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
//        mSelectedDayTextPaint.setColor(Color.WHITE);
//        mSelectedDayTextPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
//        mSelectedDayTextPaint.setStyle(Style.FILL);
//        mSelectedDayTextPaint.setTextAlign(Align.CENTER);
//        mSelectedDayTextPaint.setFakeBoldText(true);

        // 日期字体paint
        mDayTextPaint = new Paint();
        mDayTextPaint.setAntiAlias(true);
        mDayTextPaint.setColor(mDayTextColor);
        mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mDayTextPaint.setStyle(Style.FILL);
        mDayTextPaint.setTextAlign(Align.CENTER);
        mDayTextPaint.setFakeBoldText(false);

        // 标签字体paint
        mTagTextPaint = new Paint();
        mTagTextPaint.setAntiAlias(true);
        mTagTextPaint.setColor(mDayTextColor);
        mTagTextPaint.setTextSize(TAG_TEXT_SIZE);
        mTagTextPaint.setStyle(Style.FILL);
        mTagTextPaint.setTextAlign(Align.CENTER);
        mTagTextPaint.setFakeBoldText(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthCell(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置simpleMonthView的宽度和高度
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE + ROW_SEPARATOR);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
//            boolean isValidDay = false;
            if (calendarDay == null) {
                return true;
            }

            for (SimpleMonthAdapter.CalendarDay day : mInvalidDays) {
                // 选择了入住日期，这时候比入住日期大且离入住日期最近的不可用日期变为可选
                if (calendarDay.equals(day) && !(mEndDate == null && mNearestDay != null && calendarDay.equals(mNearestDay))) {
                    return true;
                }
            }

            for (SimpleMonthAdapter.CalendarDay day : mBusyDays) {
                // 选择了入住日期，这时候比入住日期大且离入住日期最近的不可用日期变为可选
                if (calendarDay.equals(day) && !(mEndDate == null && mNearestDay != null && calendarDay.equals(mNearestDay))) {
                    return true;
                }
            }
            onDayClick(calendarDay);
        }
        return true;
    }

//    public void reuse() {
//        mNumRows = DEFAULT_NUM_ROWS;
//        requestLayout();
//    }

    /**
     * 设置传递进来的参数
     *
     * @param params
     */
    public void setMonthParams(HashMap<String, Object> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);

//        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
//            mRowHeight = (int) params.get(VIEW_PARAMS_HEIGHT);
//            if (mRowHeight < MIN_HEIGHT) {
//                mRowHeight = MIN_HEIGHT;
//            }
//        }

        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DATE)) {
            mStartDate = (SimpleMonthAdapter.CalendarDay) params.get(VIEW_PARAMS_SELECTED_BEGIN_DATE);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DATE)) {
            mEndDate = (SimpleMonthAdapter.CalendarDay) params.get(VIEW_PARAMS_SELECTED_LAST_DATE);
        }

        if (params.containsKey("mNearestDay")) {
            mNearestDay = (SimpleMonthAdapter.CalendarDay) params.get("mNearestDay");
        }

        mMonth = (int) params.get(VIEW_PARAMS_MONTH);
        mYear = (int) params.get(VIEW_PARAMS_YEAR);

        mHasToday = false;
        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = (int) params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }
        }

        mNumRows = calculateNumRows();
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, SimpleMonthAdapter.CalendarDay calendarDay);
    }

    /**
     * 绘制cell
     *
     * @param canvas
     * @param x
     * @param y
     */
    private void drawDayBg(Canvas canvas, int x, int y, Paint paint) {
        RectF rectF = new RectF(x - DAY_SELECTED_RECT_SIZE, y - DAY_SELECTED_RECT_SIZE,
                x + DAY_SELECTED_RECT_SIZE, y + DAY_SELECTED_RECT_SIZE);
        canvas.drawRoundRect(rectF, 10.0f, 10.0f, paint);
    }

    /**
     * 在使用drawText方法时文字不能根据y坐标居中，所以重新计算y坐标
     * @param paint
     * @param y
     * @return
     */
    private float getTextYCenter(Paint paint, int y) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        return y + offY;
    }
}