package be.kuleuven.gt.nodenest.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import be.kuleuven.gt.nodenest.R;

public class SensorTestActivity extends AppCompatActivity {
    BarChart barChart;
    LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_test);

        barChart = findViewById(R.id.bar_chart_test);
        lineChart = findViewById(R.id.line_chart_test);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> entries= new ArrayList<>();

        for (int i = 1;i<10;i++){
            float value = (float) (i*10.0);
            BarEntry barEntry = new BarEntry(i,value);
            barEntries.add(barEntry);

            Entry entry = new Entry(i,value);
            entries.add(entry);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries,"Label");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.setData(new BarData(barDataSet));
        barChart.animateY(5000);
        barChart.getDescription().setText("Text");
        barChart.getDescription().setTextColor(Color.BLUE);


        LineDataSet lineDataSet = new LineDataSet(entries, "Label");
        lineDataSet.setCircleRadius(1f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setValueTextSize(20F);
        lineDataSet.setFillColor(Color.GREEN);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.setData(new LineData(lineDataSet));
        lineChart.animateXY(3000,3000);
    }
}
