package com.nimeshkadecha.practical_3_overduecalculator;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	EditText feePerDayEditText, numBooksEditText, overdueDaysEditText;
	TextView resultTextView;
	Button calculateButton, pickDateButton;
	TextView pickedDateText;
	Switch useOverdueDaysSwitch;

	Calendar selectedReturnDate = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		feePerDayEditText = findViewById(R.id.feePerDayEditText);
		numBooksEditText = findViewById(R.id.numBooksEditText);
		overdueDaysEditText = findViewById(R.id.overdueDaysEditText);
		resultTextView = findViewById(R.id.resultTextView);
		calculateButton = findViewById(R.id.calculateButton);
		pickDateButton = findViewById(R.id.pickDateButton);
		pickedDateText = findViewById(R.id.pickedDateText);
		useOverdueDaysSwitch = findViewById(R.id.useOverdueDaysSwitch);

		pickDateButton.setOnClickListener(v -> showDatePicker());

		useOverdueDaysSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				pickDateButton.setEnabled(false);
				overdueDaysEditText.setEnabled(true);
				pickedDateText.setText("Using overdue days input.");
			} else {
				pickDateButton.setEnabled(true);
				overdueDaysEditText.setEnabled(false);
				pickedDateText.setText("No date picked");
			}
		});

		calculateButton.setOnClickListener(v -> calculateOverdue());
	}

	private void showDatePicker() {
		Calendar calendar = Calendar.getInstance();
		DatePickerDialog dialog = new DatePickerDialog(this,
		                                               (view, year, month, dayOfMonth) -> {
			                                               selectedReturnDate = Calendar.getInstance();
			                                               selectedReturnDate.set(year, month, dayOfMonth);
			                                               SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
			                                               pickedDateText.setText("Return Date: " + sdf.format(selectedReturnDate.getTime()));
		                                               },
		                                               calendar.get(Calendar.YEAR),
		                                               calendar.get(Calendar.MONTH),
		                                               calendar.get(Calendar.DAY_OF_MONTH));
		dialog.show();
	}

	private void calculateOverdue() {
		String feeStr = feePerDayEditText.getText().toString().trim();
		String numBooksStr = numBooksEditText.getText().toString().trim();

		if (feeStr.isEmpty() || numBooksStr.isEmpty()) {
			resultTextView.setText("Please enter all required fields.");
			return;
		}

		double feePerDay = Double.parseDouble(feeStr);
		int numBooks = Integer.parseInt(numBooksStr);
		int overdueDays = 0;

		if (useOverdueDaysSwitch.isChecked()) {
			String daysStr = overdueDaysEditText.getText().toString().trim();
			if (daysStr.isEmpty()) {
				resultTextView.setText("Enter overdue days.");
				return;
			}
			overdueDays = Integer.parseInt(daysStr);
		} else {
			if (selectedReturnDate == null) {
				resultTextView.setText("Please pick a return date.");
				return;
			}
			Calendar today = Calendar.getInstance();
			long diffInMillis = today.getTimeInMillis() - selectedReturnDate.getTimeInMillis();
			overdueDays = (int) (diffInMillis / (1000 * 60 * 60 * 24));

			if (overdueDays < 0) {
				resultTextView.setText("You have " + (-overdueDays) + " day(s) left.");
				return;
			}
		}

		double total = overdueDays * feePerDay * numBooks;
		resultTextView.setText("Total Overdue: ₹" + total + " for " + overdueDays + " day(s).");
	}
}