# CalendarView
================

CalendarView是一个高度定制的日期选择器，可以满足多选日期的需求。

![CalendarView GIF](https://github.com/henry-newbie/CalendarView/blob/master/screenshot/calendar.gif)
 
### 集成
该库已上传到Jcenter中, 可以再build.gradle中直接添加

	dependencies {
	    compile 'com.henry:calendarview:1.1.1'
	}
 
### 使用
 
在xml文件中声明DayPickerView


    <com.henry.calendarview.DayPickerView
        android:id="@+id/dpv_calendar"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:colorCurrentDay="@color/colorAccent"
        app:colorSelectedDayBackground="@color/colorAccent"
        app:colorSelectedDayText="@color/selected_day_text"
        app:colorPreviousDayText="#727272"
        app:colorNormalDayText="#727272"
        app:colorYearMonthText="#727272"
        app:colorWeekText="#727272"
        app:colorBusyDaysBg="#727272"
        app:colorInValidDaysBg="#727272"
        app:colorBusyDaysText="#FFFFFF"
        app:colorInValidDaysText="#FFFFFF"

        app:textSizeDay="14sp"
        app:textSizeTag="12sp"
        app:textSizeYearMonth="16sp"
        app:textSizeWeek="14sp"
        app:headerMonthHeight="50dp"
        app:selectedDayRadius="20dp"
        app:calendarHeight="320dp"
        app:enablePreviousDay="false"/>
         


然后在你的代码中设置DataModel（参数集），DatePickerController（回调接口）

        DayPickerView.DataModel dataModel = new DayPickerView.DataModel();
        dataModel.yearStart = 2016;
        dataModel.monthStart = 6;
        dataModel.monthCount = 16;
        dataModel.defTag = "￥100";
        dataModel.leastDaysNum = 2;
        dataModel.mostDaysNum = 100;

		dayPickerView.setParameter(dataModel, new DatePickerController() {
            @Override
            public void onDayOfMonthSelected(SimpleMonthAdapter.CalendarDay calendarDay) {
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

onDayOfMonthSelected(SimpleMonthAdapter.CalendarDay calendarDay);          点击日期回调函数

onDateRangeSelected(List<SimpleMonthAdapter.CalendarDay> selectedDays);    选择日期范围回调函数

alertSelectedFail(FailEven even);										   异常回调函数

---

### 定制

    <declare-styleable name="DayPickerView">
        <attr name="colorCurrentDay" format="color"/>               <!-- 今天字体颜色 -->
        <attr name="colorSelectedDayBackground" format="color"/>    <!-- 被选中的日期背景颜色 -->
        <attr name="colorSelectedDayText" format="color"/>          <!-- 被选中的日期字体颜色 -->
        <attr name="colorPreviousDayText" format="color"/>          <!-- 已过去的日期字体颜色 -->
        <attr name="colorNormalDayText" format="color" />           <!-- 正常日期颜色 -->
        <attr name="colorYearMonthText" format="color" />           <!-- 头部年份月份字体颜色 -->
        <attr name="colorWeekText" format="color" />                <!-- 头部星期几字体颜色 -->
        <attr name="colorBusyDaysBg" format="color" />              <!-- 被占用的日期背景颜色 -->
        <attr name="colorInValidDaysBg" format="color" />           <!-- 禁用的日期背景颜色 -->
        <attr name="colorBusyDaysText" format="color" />            <!-- 被占用的日期字体颜色 -->
        <attr name="colorInValidDaysText" format="color" />         <!-- 禁用的日期字体颜色 -->

        <attr name="textSizeDay" format="dimension"/>               <!-- 正常日期字体大小 -->
        <attr name="textSizeTag" format="dimension"/>               <!-- 标签字体大小 -->
        <attr name="textSizeYearMonth" format="dimension" />        <!-- 头部年份月份字体大小 -->
        <attr name="textSizeWeek" format="dimension" />             <!-- 头部星期几字体大小 -->
        <attr name="headerMonthHeight" format="dimension" />        <!-- 头部高度 -->
        <attr name="selectedDayRadius" format="dimension" />        <!-- 日期半径 -->
        <attr name="calendarHeight" format="dimension" />           <!-- 行高 -->
        <attr name="enablePreviousDay" format="boolean" />          <!-- 已过去的日期是否能被操作 -->
	</declare-styleable>

### 联系我

有问题可以直接在issues中反馈，我会及时fix，也可以加我秋秋：643995508，欢迎start。

### 感谢

Thanks to [CalendarListview](https://github.com/traex/CalendarListview)。
