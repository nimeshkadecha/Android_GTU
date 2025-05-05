package com.nimeshkadecha.todo;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.nimeshkadecha.todo.data.AppDatabaseHelper;
import com.nimeshkadecha.todo.Task;
import com.nimeshkadecha.todo.TaskAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TodoListActivity extends AppCompatActivity {
	private AppDatabaseHelper dbHelper;
	private RecyclerView rvTasks;
	private TaskAdapter taskAdapter;
	private long userId;
	private List<Task> taskList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_todo_list);

		// Set up the Toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		dbHelper = new AppDatabaseHelper(this);
		rvTasks = findViewById(R.id.rvTasks);
		FloatingActionButton fabAdd = findViewById(R.id.fabAddTask);
		BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

		userId = getIntent().getLongExtra("USER_ID", -1);

		rvTasks.setLayoutManager(new LinearLayoutManager(this));
		loadTasks();

		taskAdapter.setOnItemClickListener(task -> {
			Intent intent = new Intent(TodoListActivity.this, AddEditTaskActivity.class);
			intent.putExtra("USER_ID", userId);
			intent.putExtra("TASK_ID", task.getId());
			startActivity(intent);
		});

		fabAdd.setOnClickListener(v -> {
			Intent intent = new Intent(TodoListActivity.this, AddEditTaskActivity.class);
			intent.putExtra("USER_ID", userId);
			startActivity(intent);
		});

		bottomNav.setSelectedItemId(R.id.nav_todos);
		bottomNav.setOnNavigationItemSelectedListener(item -> {
			if (item.getItemId() == R.id.nav_stats) {
				Intent intent = new Intent(TodoListActivity.this, StatisticsActivity.class);
				intent.putExtra("USER_ID", userId);
				startActivity(intent);
				overridePendingTransition(0,0);
				return true;
			}else if (item.getItemId() == R.id.nav_profile) {
				Intent intent = new Intent(TodoListActivity.this, ProfileActivity.class);
				intent.putExtra("USER_ID", userId);
				startActivity(intent);
				overridePendingTransition(0,0);
				return true;
			}
			return false;
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadTasks();
	}

	private void loadTasks() {
		taskList = dbHelper.getAllTasks(userId);
		taskAdapter = new TaskAdapter(this, new ArrayList<>(taskList));
		rvTasks.setAdapter(taskAdapter);

		ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
		                                                                                  ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
				int pos = viewHolder.getAdapterPosition();
				Task task = taskList.get(pos);

				if (direction == ItemTouchHelper.LEFT) {
					if (dbHelper.deleteTask(task.getId())) {
						taskList.remove(pos);
						taskAdapter.updateTasks(new ArrayList<>(taskList));
						Toast.makeText(TodoListActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
					}
				} else {
					Intent intent = new Intent(TodoListActivity.this, AddEditTaskActivity.class);
					intent.putExtra("USER_ID", userId);
					intent.putExtra("TASK_ID", task.getId());
					startActivity(intent);
				}
			}
		};

		new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvTasks);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.todo_filter_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.filter_priority) {
			sortByPriority();
			return true;
		} else if (id == R.id.filter_due_date) {
			sortByDueDate();
			return true;
		} else if (id == R.id.filter_optimization) {
			sortByOptimization();
			return true;
		} else if (id == R.id.filter_title_asc) {
			sortByTitle(true);
			return true;
		} else if (id == R.id.filter_title_desc) {
			sortByTitle(false);
			return true;
		} else if (id == R.id.filter_category) {
			filterByCategory();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private void sortByPriority() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			Collections.sort(taskList, Comparator.comparingInt(this::priorityValue));
		}
		refreshAdapter();
	}

	private void sortByDueDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Collections.sort(taskList, (t1, t2) -> {
			try {
				return sdf.parse(t1.getDueDate()).compareTo(sdf.parse(t2.getDueDate()));
			} catch (ParseException e) {
				return 0;
			}
		});
		refreshAdapter();
	}

	private void sortByOptimization() {
		// due date ASC then priority DESC
		sortByDueDate();
		Collections.sort(taskList, (t1, t2) -> priorityValue(t2) - priorityValue(t1));
		refreshAdapter();
	}

	private void sortByTitle(boolean asc) {
		Collections.sort(taskList, (t1, t2) -> asc ? t1.getTitle().compareToIgnoreCase(t2.getTitle())
										: t2.getTitle().compareToIgnoreCase(t1.getTitle()));
		refreshAdapter();
	}

	private void filterByCategory() {
		// get distinct categories
		List<String> categories = new ArrayList<>();
		for (Task t : taskList) {
			if (!categories.contains(t.getCategory())) categories.add(t.getCategory());
		}
		if (categories.isEmpty()) {
			Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Category");
		String[] arr = categories.toArray(new String[0]);
		builder.setItems(arr, (dialog, which) -> {
			String selected = arr[which];
			List<Task> filtered = new ArrayList<>();
			for (Task t : taskList) {
				if (t.getCategory().equals(selected)) filtered.add(t);
			}
			taskAdapter.updateTasks(filtered);
		});
		builder.show();
	}

	private int priorityValue(Task task) {
		switch (task.getPriority().toLowerCase()) {
			case "high": return 3;
			case "medium": return 2;
			default: return 1;
		}
	}

	private void refreshAdapter() {
		taskAdapter.updateTasks(new ArrayList<>(taskList));
	}
}
