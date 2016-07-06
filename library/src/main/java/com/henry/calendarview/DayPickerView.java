package com.henry.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

public class DayPickerView extends RecyclerView {
    protected Context mContext;
    protected SimpleMonthAdapter mAdapter;
    private DatePickerController mController;
    protected int mCurrentScrollState = 0;
    protected long mPreviousScrollPosition;
    protected int mPreviousScrollState = 0;
    private TypedArray typedArray;
    private OnScrollListener onScrollListener;

    private DataModel dataModel;

    public DayPickerView(Context context) {
        this(context, null);
    }

    public DayPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DayPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.DayPickerView);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        init(context);
    }

    public void init(Context paramContext) {
        setLayoutManager(new LinearLayoutManager(paramContext));
        mContext = paramContext;
        setUpListView();

        onScrollListener = new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final SimpleMonthView child = (SimpleMonthView) recyclerView.getChildAt(0);
                if (child == null) {
                    return;
                }

                mPreviousScrollPosition = dy;
                mPreviousScrollState = mCurrentScrollState;
            }
        };
    }

    protected void setUpAdapter() {
        if (mAdapter == null) {
            mAdapter = new SimpleMonthAdapter(getContext(), typedArray, mController, dataModel);
            setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void setUpListView() {
        setVerticalScrollBarEnabled(false);
        setOnScrollListener(onScrollListener);
        setFadingEdgeLength(0);
    }

    /**
     * 设置参数
     *
     * @param dataModel   数据
     * @param mController 回调监听
     */
    public void setParameter(DataModel dataModel, DatePickerController mController) {
        if(dataModel == null) {
            Log.e("crash", "请设置参数");
            return;
        }
        this.dataModel = dataModel;
        this.mController = mController;
        setUpAdapter();
        // 跳转到入住日期所在的月份
        scrollToSelectedPosition(dataModel.selectedDays, dataModel.monthStart);
    }

    private void scrollToSelectedPosition(SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays, int monthStart) {
        if(selectedDays != null && selectedDays.getFirst() != null && selectedDays.getFirst().month > monthStart) {
            int position = selectedDays.getFirst().month - monthStart;
            scrollToPosition(position);
        }
    }

    public static class DataModel implements Serializable {
        // TYPE_MULTI：多选，TYPE_RANGE：范围选，TYPE_ONLY_READ：只读
//        public enum TYPE {TYPE_MULTI, TYPE_RANGE, TYPE_ONLY_READ}

        //        TYPE type;                                       // 类型
        public int yearStart;                                      // 日历开始的年份
        public int monthStart;                                     // 日历开始的月份
        public int monthCount;                                     // 要显示几个月
        public List<SimpleMonthAdapter.CalendarDay> invalidDays;   // 无效的日期
        public List<SimpleMonthAdapter.CalendarDay> busyDays;      // 被占用的日期
        public SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays;  // 默认选择的日期
        public int leastDaysNum;                                   // 至少选择几天
        public int mostDaysNum;                                    // 最多选择几天
        public List<SimpleMonthAdapter.CalendarDay> tags;          // 日期下面对应的标签
        public String defTag;                                      // 默认显示的标签
//        public boolean displayTag;                               // 是否显示标签
    }
}