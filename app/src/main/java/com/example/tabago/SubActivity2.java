package com.example.tabago;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SubActivity2 extends AppCompatActivity {
    private BarChart weeklyChart;
    private List<BarEntry> weeklyEntries;
    private List<String> weeklyLabels;
    private List<Float> weeklyData; // 주간 그래프의 데이터 값 목록
    private PieChart dailyPieChart;
    private List<Float> dailyData;
    private List<String> dailyLabels;

    private int usedCount; // 사용한 횟수

    public int userSetCountEditText = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub2);

        weeklyChart = findViewById(R.id.weekly_chart);
        dailyPieChart = findViewById(R.id.daily_pie_chart);

        // MainActivity로부터 데이터 받아오기
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // MainActivity에서 전달한 주간 흡연 횟수 데이터 받기
            usedCount = extras.getInt("usedCount", 0);
            userSetCountEditText = extras.getInt("userSetCountEditText", 0);
            String[] weeklyDataArray = extras.getStringArray("weeklyData");
            String[] weeklyLabelsArray = extras.getStringArray("weeklyLabels");
            String[] dailyDataArray = extras.getStringArray("dailyEntries");
            String[] dailyLabelsArray = extras.getStringArray("dailyLabels");
            if (weeklyDataArray != null && weeklyLabelsArray != null) {
                // 주간 흡연 횟수 데이터를 주간 그래프에 표시하기 위해 Float 리스트로 변환
                weeklyData = stringArrayToFloatList(weeklyDataArray);
                weeklyLabels = Arrays.asList(weeklyLabelsArray);
                dailyData = stringArrayToFloatList(dailyDataArray);
                dailyLabels = Arrays.asList(dailyLabelsArray);
                setupWeeklyChart();
                setupDailyPieChart();
            } else {
                // MainActivity에서 주간 데이터가 전달되지 않은 경우, 빈 주간 그래프를 위해 빈 리스트로 초기화
                weeklyData = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    weeklyData.add(0f);
                }
                weeklyLabels = new ArrayList<>();
                weeklyLabels.add("일");
                weeklyLabels.add("월");
                weeklyLabels.add("화");
                weeklyLabels.add("수");
                weeklyLabels.add("목");
                weeklyLabels.add("금");
                weeklyLabels.add("토");
                setupWeeklyChart();
            }
            // userSetCount 값을 받아옴
        }
    }

    private List<Float> stringArrayToFloatList(String[] data) {
        List<Float> floatList = new ArrayList<>();
        for (String floatStr : data) {
            // 쉼표로 구분된 형식을 처리
            String[] values = floatStr.split(",");
            for (String value : values) {
                try {
                    float floatValue = Float.parseFloat(value.trim());
                    floatList.add(floatValue);
                } catch (NumberFormatException e) {
                    // 파싱 오류 처리가 필요하다면 여기서 처리
                }
            }
        }
        return floatList;
    }

    // 주간 그래프 설정
    private void setupWeeklyChart() {
        // X축 설정
        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(weeklyLabels));

        // Y축 설정
        YAxis leftAxis = weeklyChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        // 주간 그래프의 최대값을 주간 데이터의 최댓값으로 설정
        leftAxis.setAxisMaximum(Collections.max(weeklyData) + 10f);

        weeklyChart.getAxisRight().setEnabled(false);
        weeklyChart.getDescription().setEnabled(false);

        weeklyEntries = new ArrayList<>();
        for (int i = 0; i < weeklyData.size(); i++) {
            weeklyEntries.add(new BarEntry(i, weeklyData.get(i)));
        }

        BarDataSet barDataSet = new BarDataSet(weeklyEntries, "주간 담배 횟수");
        barDataSet.setColor(Color.rgb(255, 153, 0));
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);

        weeklyChart.setData(barData);
        weeklyChart.invalidate();
    }

    private void setupDailyPieChart() {
        int weeklyTotal = 0;
        for (float value : weeklyData) {
            weeklyTotal += value;
        }

        int remainingCount = 50 - weeklyTotal;
        int usedCount = weeklyTotal;

        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(remainingCount, "남은 횟수"));
        pieEntries.add(new PieEntry(usedCount, "사용한 횟수"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "일주일 담배 횟수");
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        pieDataSet.setValueTextSize(12f);

        PieData pieData = new PieData(pieDataSet);

        dailyPieChart.setData(pieData);
        dailyPieChart.setDescription(null);
        dailyPieChart.invalidate();
    }
}

