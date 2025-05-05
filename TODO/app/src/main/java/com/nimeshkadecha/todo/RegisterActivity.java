package com.nimeshkadecha.todo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
	private TextInputEditText etName, etEmail, etMobile, etPassword, etConfirmPassword;
	private AppDatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		dbHelper = new AppDatabaseHelper(this);

		etName = findViewById(R.id.etName);
		etEmail = findViewById(R.id.etEmail);
		etMobile = findViewById(R.id.etMobile);
		etPassword = findViewById(R.id.etPassword);
		etConfirmPassword = findViewById(R.id.etConfirmPassword);
		Button btnRegister = findViewById(R.id.btnRegister);
		TextView tvLoginLink = findViewById(R.id.tvLoginLink);

		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = etName.getText().toString().trim();
				String email = etEmail.getText().toString().trim();
				String mobile = etMobile.getText().toString().trim();
				String password = etPassword.getText().toString().trim();
				String confirmPassword = etConfirmPassword.getText().toString().trim();

				if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(mobile)
												|| TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
					Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
					return;
				}

				if (!password.equals(confirmPassword)) {
					Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
					return;
				}

				long id = dbHelper.createUser(name, email, mobile, password);
				if (id != -1) {
					Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
					startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
					finish();
				} else {
					Toast.makeText(RegisterActivity.this, "Registration failed: Email may already exist", Toast.LENGTH_SHORT).show();
				}
			}
		});

		tvLoginLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
				finish();
			}
		});
	}
}
