package com.nimeshkadecha.todo;


import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class StatisticsActivity extends AppCompatActivity {
	private AppDatabaseHelper dbHelper;
	private long userId;
	private TextView tvTotal, tvCompleted, tvPending;
	private BarChart barChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);

		// Initialize views
		tvTotal = findViewById(R.id.tvTotalTasks);
		tvCompleted = findViewById(R.id.tvCompletedTasks);
		tvPending = findViewById(R.id.tvPendingTasks);
		barChart = findViewById(R.id.barChart);

		dbHelper = new AppDatabaseHelper(this);
		userId = getIntent().getLongExtra("USER_ID", -1);

		loadStatistics();
	}

	private void loadStatistics() {
		// Get all tasks for the user
		List<Task> tasks = dbHelper.getAllTasks(userId);
		Map<String, Integer> completedTasksPerDay = new LinkedHashMap<>();

		// Format for date parsing and comparison
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -30); // 30 days ago
		Date thirtyDaysAgo = calendar.getTime();
		Date today = new Date();

		// Count completed tasks for each day using the Due Date
		for (Task task : tasks) {
			String taskDueDate = task.getDueDate();
			if (taskDueDate != null && "Done".equalsIgnoreCase(task.getStatus())) {
				try {
					Date taskDate = sdf.parse(taskDueDate);
					if (taskDate != null && !taskDate.before(thirtyDaysAgo) && !taskDate.after(today)) {
						// increment count for that date
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							completedTasksPerDay.put(
															taskDueDate,
															completedTasksPerDay.getOrDefault(taskDueDate, 0) + 1
							                        );
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		// Prepare the BarChart entries and labels
		List<BarEntry> entries = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		int index = 0;
		for (Map.Entry<String, Integer> entry : completedTasksPerDay.entrySet()) {
			entries.add(new BarEntry(index, entry.getValue()));
			labels.add(entry.getKey());
			index++;
		}

		if (entries.isEmpty()) {
			Toast.makeText(this, "No completed tasks found for the last 30 days", Toast.LENGTH_SHORT).show();
			return;
		}

		// 1) set up the BarData / chart as before
		BarDataSet dataSet = new BarDataSet(entries, "Completed Tasks per Day");
		dataSet.setColor(getResources().getColor(R.color.teld));
		BarData data = new BarData(dataSet);
		data.setBarWidth(0.9f);

		barChart.setData(data);
		barChart.setFitBars(true);
		barChart.getDescription().setEnabled(false);

		// 2) configure X-axis for completeness, even if we won’t show labels
		XAxis xAxis = barChart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setGranularity(1f);
		xAxis.setGranularityEnabled(true);
		xAxis.setAxisMinimum(-0.5f);
		xAxis.setAxisMaximum(labels.size() - 0.5f);
		xAxis.setDrawGridLines(false);
		xAxis.setDrawLabels(false);     // hide the X-axis labels

		// 3) hide right axis, keep left for counts
		barChart.getAxisRight().setEnabled(false);
		YAxis left = barChart.getAxisLeft();
		left.setAxisMinimum(0f);
		left.setDrawGridLines(true);

		// 4) add tap listener
		barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
			@Override
			public void onValueSelected(Entry e, Highlight h) {
				// get the integer index of the bar
				int i = Math.round(e.getX());
				if (i >= 0 && i < labels.size()) {
					String date = labels.get(i);
					Toast.makeText(StatisticsActivity.this,
					               "Date: " + date, Toast.LENGTH_SHORT).show();
				}
			}
			@Override
			public void onNothingSelected() { }
		});


		// --- Y-Axis setup ---
		YAxis leftAxis = barChart.getAxisLeft();
		leftAxis.setAxisMinimum(0f);
		leftAxis.setDrawGridLines(true);
		barChart.getAxisRight().setEnabled(false); // hide right axis

		barChart.invalidate();  // Refresh the chart
	}



}
