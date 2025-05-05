package com.nimeshkadecha.todo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AppDatabaseHelper extends SQLiteOpenHelper {
	// Database Info
	private static final String DATABASE_NAME = "todo_app.db";
	private static final int DATABASE_VERSION = 1;

	// Table Names
	private static final String TABLE_USERS = "users";
	private static final String TABLE_TASKS = "tasks";

	// User Table Columns
	private static final String KEY_USER_ID = "id";
	private static final String KEY_USER_NAME = "name";
	private static final String KEY_USER_EMAIL = "email";
	private static final String KEY_USER_MOBILE = "mobile";
	private static final String KEY_USER_PASSWORD = "password";
	private static final String KEY_USER_REMEMBER = "remember_me";

	// Task Table Columns
	private static final String KEY_TASK_ID = "id";
	private static final String KEY_TASK_USER_ID = "user_id";
	private static final String KEY_TASK_TITLE = "title";
	private static final String KEY_TASK_DESC = "description";
	private static final String KEY_TASK_DUE = "due_date";
	private static final String KEY_TASK_PRIORITY = "priority";
	private static final String KEY_TASK_STATUS = "status";
	private static final String KEY_TASK_CATEGORY = "category";

	public AppDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
										KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
										KEY_USER_NAME + " TEXT, " +
										KEY_USER_EMAIL + " TEXT UNIQUE, " +
										KEY_USER_MOBILE + " TEXT, " +
										KEY_USER_PASSWORD + " TEXT, " +
										KEY_USER_REMEMBER + " INTEGER DEFAULT 0" +
										");";

		String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + " (" +
										KEY_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
										KEY_TASK_USER_ID + " INTEGER, " +
										KEY_TASK_TITLE + " TEXT, " +
										KEY_TASK_DESC + " TEXT, " +
										KEY_TASK_DUE + " TEXT, " +
										KEY_TASK_PRIORITY + " TEXT, " +
										KEY_TASK_STATUS + " TEXT, " +
										KEY_TASK_CATEGORY + " TEXT, " +
										"FOREIGN KEY(" + KEY_TASK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
										");";

		db.execSQL(CREATE_USERS_TABLE);
		db.execSQL(CREATE_TASKS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		onCreate(db);
	}

	// -- User operations --

	public long createUser(String name, String email, String mobile, String password) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USER_NAME, name);
		values.put(KEY_USER_EMAIL, email);
		values.put(KEY_USER_MOBILE, mobile);
		values.put(KEY_USER_PASSWORD, password);
		values.put(KEY_USER_REMEMBER, 0);
		long id = db.insert(TABLE_USERS, null, values);
		db.close();
		return id;
	}

	public boolean authenticateUser(String email, String password) {
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = KEY_USER_EMAIL + " = ? AND " + KEY_USER_PASSWORD + " = ?";
		String[] args = { email, password };
		Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_USER_ID}, selection, args, null, null, null);
		boolean exists = (cursor != null && cursor.getCount() > 0);
		if (cursor != null) cursor.close();
		db.close();
		return exists;
	}

	public void setRememberMe(String email, boolean remember) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USER_REMEMBER, remember ? 1 : 0);
		db.update(TABLE_USERS, values, KEY_USER_EMAIL + " = ?", new String[]{ email });
		db.close();
	}

	public String getRememberedUser() {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + KEY_USER_EMAIL + " FROM " + TABLE_USERS + " WHERE " + KEY_USER_REMEMBER + " = 1 LIMIT 1";
		Cursor cursor = db.rawQuery(query, null);
		String email = null;
		if (cursor.moveToFirst()) {
			email = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return email;
	}

	public boolean updateUserProfile(String email, String currentPassword, String newName, String newMobile, String newPassword) {
		SQLiteDatabase db = this.getWritableDatabase();

		// Validate current password
		Cursor cursor = db.rawQuery("SELECT password FROM users WHERE email = ?", new String[]{email});
		if (cursor.moveToFirst()) {
			String dbPassword = cursor.getString(0);
			if (!dbPassword.equals(currentPassword)) {
				cursor.close();
				db.close();
				return false;
			}
		} else {
			cursor.close();
			db.close();
			return false;
		}
		cursor.close();

		ContentValues values = new ContentValues();
		values.put("name", newName);
		values.put("mobile", newMobile);
		if (!newPassword.isEmpty()) {
			values.put("password", newPassword);
		}

		db.update("users", values, "email = ?", new String[]{email});
		db.close();
		return true;
	}


	public void clearRememberedUser() {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USER_REMEMBER, 0);
		db.update(TABLE_USERS, values, KEY_USER_REMEMBER + " = ?", new String[] { "1" });
		db.close();
	}



	// -- Task operations --

	public long addTask(long userId, String title, String description, String dueDate, String priority, String status, String category) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TASK_USER_ID, userId);
		values.put(KEY_TASK_TITLE, title);
		values.put(KEY_TASK_DESC, description);
		values.put(KEY_TASK_DUE, dueDate);
		values.put(KEY_TASK_PRIORITY, priority);
		values.put(KEY_TASK_STATUS, status);
		values.put(KEY_TASK_CATEGORY, category);
		long id = db.insert(TABLE_TASKS, null, values);
		db.close();
		return id;
	}

	public List<Task> getAllTasks(long userId) {
		List<Task> tasks = new ArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = KEY_TASK_USER_ID + " = ?";
		String[] args = { String.valueOf(userId) };
		Cursor cursor = db.query(TABLE_TASKS, null, selection, args, null, null, KEY_TASK_DUE + " ASC");
		if (cursor.moveToFirst()) {
			do {
				@SuppressLint("Range") Task task = new Task(
												cursor.getLong(cursor.getColumnIndex(KEY_TASK_ID)),
												cursor.getLong(cursor.getColumnIndex(KEY_TASK_USER_ID)),
												cursor.getString(cursor.getColumnIndex(KEY_TASK_TITLE)),
												cursor.getString(cursor.getColumnIndex(KEY_TASK_DESC)),
												cursor.getString(cursor.getColumnIndex(KEY_TASK_DUE)),
												cursor.getString(cursor.getColumnIndex(KEY_TASK_PRIORITY)),
												cursor.getString(cursor.getColumnIndex(KEY_TASK_STATUS)),
												cursor.getString(cursor.getColumnIndex(KEY_TASK_CATEGORY))
				);
				tasks.add(task);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return tasks;
	}

	@SuppressLint("Range")
	public Task getTaskById(long taskId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = KEY_TASK_ID + " = ?";
		String[] args = { String.valueOf(taskId) };
		Cursor cursor = db.query(TABLE_TASKS, null, selection, args, null, null, null);
		Task task = null;
		if (cursor != null && cursor.moveToFirst()) {
			task = new Task(
											cursor.getLong(cursor.getColumnIndex(KEY_TASK_ID)),
											cursor.getLong(cursor.getColumnIndex(KEY_TASK_USER_ID)),
											cursor.getString(cursor.getColumnIndex(KEY_TASK_TITLE)),
											cursor.getString(cursor.getColumnIndex(KEY_TASK_DESC)),
											cursor.getString(cursor.getColumnIndex(KEY_TASK_DUE)),
											cursor.getString(cursor.getColumnIndex(KEY_TASK_PRIORITY)),
											cursor.getString(cursor.getColumnIndex(KEY_TASK_STATUS)),
											cursor.getString(cursor.getColumnIndex(KEY_TASK_CATEGORY))
			);
			cursor.close();
		}
		db.close();
		return task;
	}

	public boolean updateTask(Task task) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TASK_TITLE, task.getTitle());
		values.put(KEY_TASK_DESC, task.getDescription());
		values.put(KEY_TASK_DUE, task.getDueDate());
		values.put(KEY_TASK_PRIORITY, task.getPriority());
		values.put(KEY_TASK_STATUS, task.getStatus());
		values.put(KEY_TASK_CATEGORY, task.getCategory());
		int rows = db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
		db.close();
		return rows > 0;
	}

	public boolean deleteTask(long taskId) {
		SQLiteDatabase db = this.getWritableDatabase();
		int rows = db.delete(TABLE_TASKS, KEY_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
		db.close();
		return rows > 0;
	}
}
