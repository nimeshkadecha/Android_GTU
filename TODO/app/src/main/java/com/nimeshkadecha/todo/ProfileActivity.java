package com.nimeshkadecha.todo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
	private EditText etName, etMobile, etCurrentPassword, etNewPassword;
	private Button btnSaveChanges, btnLogout;
	private AppDatabaseHelper dbHelper;
	private String email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		etName = findViewById(R.id.etName);
		etMobile = findViewById(R.id.etMobile);
		etCurrentPassword = findViewById(R.id.etCurrentPassword);
		etNewPassword = findViewById(R.id.etNewPassword);
		btnSaveChanges = findViewById(R.id.btnSaveChanges);
		btnLogout = findViewById(R.id.btnLogout);

		dbHelper = new AppDatabaseHelper(this);
		email = dbHelper.getRememberedUser();

		if (email != null) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT name, mobile FROM users WHERE email = ?", new String[]{email});
			if (cursor.moveToFirst()) {
				etName.setText(cursor.getString(0));
				etMobile.setText(cursor.getString(1));
			}
			cursor.close();
			db.close();
		}

		btnSaveChanges.setOnClickListener(v -> {
			String name = etName.getText().toString().trim();
			String mobile = etMobile.getText().toString().trim();
			String currentPassword = etCurrentPassword.getText().toString();
			String newPassword = etNewPassword.getText().toString();

			if (email == null) {
				Toast.makeText(this, "No remembered user found.", Toast.LENGTH_SHORT).show();
				return;
			}

			boolean success = dbHelper.updateUserProfile(email, currentPassword, name, mobile, newPassword);

			if (success) {
				Toast.makeText(this, "Changes saved.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_SHORT).show();
			}
		});

		btnLogout.setOnClickListener(v -> {
			dbHelper.clearRememberedUser();
			Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		});
	}
}
