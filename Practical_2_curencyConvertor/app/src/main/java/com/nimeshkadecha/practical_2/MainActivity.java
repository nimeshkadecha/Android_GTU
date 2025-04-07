package com.nimeshkadecha.practical_2;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
	EditText inputAmount;
	Spinner fromCurrencySpinner, toCurrencySpinner;
	Button convertButton;
	TextView resultText;
	String[] currencies = {"INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "RUB"};
	HashMap<String, Double> ratesToUSD = new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		inputAmount = findViewById(R.id.inputAmount);
		fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner);
		toCurrencySpinner = findViewById(R.id.toCurrencySpinner);
		convertButton = findViewById(R.id.convertButton);
		resultText = findViewById(R.id.resultText);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
		fromCurrencySpinner.setAdapter(adapter);
		toCurrencySpinner.setAdapter(adapter);
		ratesToUSD.put("USD", 1.0);
		ratesToUSD.put("INR", 0.012);
		ratesToUSD.put("EUR", 1.09);
		ratesToUSD.put("GBP", 1.28);
		ratesToUSD.put("JPY", 0.0066);
		ratesToUSD.put("AUD", 0.66);
		ratesToUSD.put("CAD", 0.74);
		ratesToUSD.put("CHF", 1.10);
		ratesToUSD.put("CNY", 0.14);
		ratesToUSD.put("RUB", 0.011);
		convertButton.setOnClickListener(v -> convertCurrency());
	}
	void convertCurrency() {
		String fromCurrency = fromCurrencySpinner.getSelectedItem().toString();
		String toCurrency = toCurrencySpinner.getSelectedItem().toString();
		String input = inputAmount.getText().toString();
		if (input.isEmpty()) {
			Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
			return;
		}
		double amount = Double.parseDouble(input);
		double amountInUSD = amount * ratesToUSD.get(fromCurrency);
		double converted = amountInUSD / ratesToUSD.get(toCurrency);
		String result = String.format("%.2f %s = %.2f %s", amount, fromCurrency, converted, toCurrency);
		resultText.setText(result);
	}
}