package com.nimeshkadecha.todo;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
	private TextInputEditText etEmail, etPassword;
	private CheckBox cbRemember;
	private AppDatabaseHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		dbHelper = new AppDatabaseHelper(this);

		etEmail = findViewById(R.id.etEmail);
		etPassword = findViewById(R.id.etPassword);
		cbRemember = findViewById(R.id.cbRemember);
		Button btnLogin = findViewById(R.id.btnLogin);
		TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);

		// Check remembered user
		String rememberedEmail = dbHelper.getRememberedUser();
		if (rememberedEmail != null) {
			navigateToTodoList();
			finish();
			return;
		}

		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = etEmail.getText().toString().trim();
				String password = etPassword.getText().toString().trim();

				if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
					Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
					return;
				}

				boolean valid = dbHelper.authenticateUser(email, password);
				if (valid) {
					dbHelper.setRememberMe(email, cbRemember.isChecked());
					navigateToTodoList();
					finish();
				} else {
					Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
				}
			}
		});

		tvRegisterLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
			}
		});
	}

	private void navigateToTodoList() {
		startActivity(new Intent(LoginActivity.this, TodoListActivity.class));
	}
}
