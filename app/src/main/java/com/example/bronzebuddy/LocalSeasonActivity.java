package com.example.bronzebuddy;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;

public class LocalSeasonActivity extends AppCompatActivity {
    ArrayList<Integer> localClimateList = new ArrayList<>(12);
    ArrayList<BarEntry> values = new ArrayList<>();
    private BarChart chart;
    //ArrayList<String> labels = new ArrayList<>(Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"));
    ArrayList<String> labels = new ArrayList<>(Arrays.asList("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkExtras();
        setContentView(R.layout.activity_local_season);
        initLayout();
        setData();
    }

    private void initLayout() {
        chart = findViewById(R.id.chart1);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.setDescription(null);
//        XAxis xAxis = chart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

    }

    private void setData(){
        for(int i=0; i<localClimateList.size(); i++){
            values.add(new BarEntry(i,localClimateList.get(i)));
        }
        BarDataSet set1 = new BarDataSet(values, "DataSet1");

        BarData data = new BarData(set1);
        data.setValueTextSize(9f);
        data.setDrawValues(false);
        chart.setData(data);

    }


    private void checkExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                switch (key) {
                    case "localClimateList":
                        localClimateList = extras.getIntegerArrayList(key);
                        break;
                }
            }

        }
    }
}
