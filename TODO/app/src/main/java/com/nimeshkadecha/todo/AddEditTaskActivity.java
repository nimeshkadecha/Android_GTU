package com.nimeshkadecha.todo;



import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.nimeshkadecha.todo.R;
//import com.nimeshkadecha.todo.data.AppDatabaseHelper;
import com.nimeshkadecha.todo.Task;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {
	private TextInputEditText etTitle, etDescription, etDueDate, etCategory;
	private Spinner spinnerPriority, spinnerStatus;
	private AppDatabaseHelper dbHelper;
	private Task existingTask;
	private long userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_edit_task);

		dbHelper = new AppDatabaseHelper(this);
		etTitle = findViewById(R.id.etTitle);
		etDescription = findViewById(R.id.etDescription);
		etDueDate = findViewById(R.id.etDueDate);
		etDueDate.setOnClickListener(v -> showDatePicker());
		spinnerPriority = findViewById(R.id.spinnerPriority);
		spinnerStatus = findViewById(R.id.spinnerStatus);
		etCategory = findViewById(R.id.etCategory);
		Button btnSave = findViewById(R.id.btnSaveTask);

		// Retrieve userId passed via Intent
		userId = getIntent().getLongExtra("USER_ID", -1);

		// Check for existing Task to edit
		if (getIntent().hasExtra("TASK_ID")) {
			long taskId = getIntent().getLongExtra("TASK_ID", -1);
			existingTask = dbHelper.getTaskById(taskId);
			if (existingTask != null) {
				etTitle.setText(existingTask.getTitle());
				etDescription.setText(existingTask.getDescription());
				etDueDate.setText(existingTask.getDueDate());
				// Assuming spinner adapters have matching entries
				spinnerPriority.setSelection(getPriorityIndex(existingTask.getPriority()));
				spinnerStatus.setSelection(getStatusIndex(existingTask.getStatus()));
				etCategory.setText(existingTask.getCategory());
			}
		}

		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = etTitle.getText().toString().trim();
				String description = etDescription.getText().toString().trim();
				String dueDate = etDueDate.getText().toString().trim();
				String priority = spinnerPriority.getSelectedItem().toString();
				String status = spinnerStatus.getSelectedItem().toString();
				String category = etCategory.getText().toString().trim();

				if (TextUtils.isEmpty(title) || TextUtils.isEmpty(dueDate)) {
					Toast.makeText(AddEditTaskActivity.this, "Title and Due Date are required", Toast.LENGTH_SHORT).show();
					return;
				}

				if (existingTask == null) {
					long id = dbHelper.addTask(userId, title, description, dueDate, priority, status, category);
					if (id != -1) Toast.makeText(AddEditTaskActivity.this, "Task added", Toast.LENGTH_SHORT).show();
				} else {
					existingTask.setTitle(title);
					existingTask.setDescription(description);
					existingTask.setDueDate(dueDate);
					existingTask.setPriority(priority);
					existingTask.setStatus(status);
					existingTask.setCategory(category);
					boolean updated = dbHelper.updateTask(existingTask);
					if (updated) Toast.makeText(AddEditTaskActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		});
	}

	// Helper methods to map spinner selection, implement as needed
	private int getPriorityIndex(String priority) {
		// e.g., LOW=0, MEDIUM=1, HIGH=2
		switch (priority) {
			case "Low": return 0;
			case "Medium": return 1;
			default: return 2;
		}
	}
	private void showDatePicker() {
		// Use current date as default
		final Calendar cal = Calendar.getInstance();
		int year  = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day   = cal.get(Calendar.DAY_OF_MONTH);

		// If editing an existing task, parse its date and override year/month/day here:
		if (existingTask != null && existingTask.getDueDate() != null) {
			try {
				String[] parts = existingTask.getDueDate().split("-");
				year  = Integer.parseInt(parts[0]);
				month = Integer.parseInt(parts[1]) - 1; // zero-based
				day   = Integer.parseInt(parts[2]);
			} catch (Exception ignored) {}
		}

		DatePickerDialog dpd = new DatePickerDialog(
										this,
										(view, pickedYear, pickedMonth, pickedDay) -> {
											// Format as yyyy-MM-dd
											String formatted = String.format(
																			Locale.getDefault(),
																			"%04d-%02d-%02d",
																			pickedYear,
																			pickedMonth + 1,
																			pickedDay
											                                );
											etDueDate.setText(formatted);
										},
										year, month, day
		);
		dpd.show();
	}

	private int getStatusIndex(String status) {
		// e.g., TODO=0, DONE=1
		return status.equalsIgnoreCase("Done") ? 1 : 0;
	}
}
