package com.nimeshkadecha.practical_1;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
	private TextView answerDisplay;
	private String currentExpression = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_main);
		answerDisplay = findViewById(R.id.answerDisplay);
		setNumberAndOperatorListeners();
		setExtraListeners();
		setParenthesesListeners();
		setEqualsListener();
	}
	private void setNumberAndOperatorListeners() {
		int[] buttonIds = {
										R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
										R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot,
										R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
		};
		for (int id : buttonIds) {
			findViewById(id).setOnClickListener(view -> {
				TextView tv = (TextView) view;
				currentExpression += tv.getText().toString();
				answerDisplay.setText(currentExpression);
			});
		}
	}
	private void setEqualsListener() {
		findViewById(R.id.btnEquals).setOnClickListener(view -> {
			try {
				double result = evaluateExpression(currentExpression);
				answerDisplay.setText(String.valueOf(result));
				currentExpression = String.valueOf(result);
			} catch (Exception e) {
				answerDisplay.setText("Error");
				currentExpression = "";
			}
		});
	}
	private void setExtraListeners() {
		findViewById(R.id.btnClear).setOnClickListener(view -> {
			currentExpression = "";
			answerDisplay.setText("0");
		});
		findViewById(R.id.btnClearEntry).setOnClickListener(view -> {
			if (!currentExpression.isEmpty()) {
				currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
				answerDisplay.setText(currentExpression.isEmpty() ? "0" : currentExpression);
			}
		});
	}
	private void setParenthesesListeners() {
		findViewById(R.id.btnLeftParen).setOnClickListener(view -> {
			currentExpression += "(";
			answerDisplay.setText(currentExpression);
		});

		findViewById(R.id.btnRightParen).setOnClickListener(view -> {
			currentExpression += ")";
			answerDisplay.setText(currentExpression);
		});
	}
	private double evaluateExpression(String expression) throws Exception {
		List<String> tokens = tokenize(expression);
		List<String> postfix = infixToPostfix(tokens);
		return evaluatePostfix(postfix);
	}
	private List<String> tokenize(String expression) {
		List<String> tokens = new ArrayList<>();
		StringBuilder numberBuffer = new StringBuilder();
		for (int i = 0; i < expression.length(); i++) {
			char c = expression.charAt(i);
			if (Character.isDigit(c) || c == '.') {
				numberBuffer.append(c);
			} else {
				if (numberBuffer.length() > 0) {
					tokens.add(numberBuffer.toString());
					numberBuffer.setLength(0);
				}
				tokens.add(String.valueOf(c));
			}
		}
		if (numberBuffer.length() > 0) {
			tokens.add(numberBuffer.toString());
		}
		return tokens;
	}
	private List<String> infixToPostfix(List<String> tokens) throws Exception {
		List<String> output = new ArrayList<>();
		Stack<String> operatorStack = new Stack<>();

		for (String token : tokens) {
			if (isNumber(token)) {
				output.add(token);
			}
			else if (isOperator(token)) {
				while (!operatorStack.isEmpty() && isOperator(operatorStack.peek())
												&& getPrecedence(operatorStack.peek()) >= getPrecedence(token)) {
					output.add(operatorStack.pop());
				}
				operatorStack.push(token);
			}
			else if (token.equals("(")) {
				operatorStack.push(token);
			}
			else if (token.equals(")")) {
				while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
					output.add(operatorStack.pop());
				}
				if (operatorStack.isEmpty()) {
					throw new Exception("Mismatched parentheses");
				}
				operatorStack.pop();
			}
		}
		while (!operatorStack.isEmpty()) {
			String op = operatorStack.pop();
			if (op.equals("(") || op.equals(")")) {
				throw new Exception("Mismatched parentheses");
			}
			output.add(op);
		}
		return output;
	}
	private double evaluatePostfix(List<String> tokens) throws Exception {
		Stack<Double> stack = new Stack<>();
		for (String token : tokens) {
			if (isNumber(token)) {
				stack.push(Double.parseDouble(token));
			} else if (isOperator(token)) {
				if (stack.size() < 2) {
					throw new Exception("Invalid Expression");
				}
				double b = stack.pop();
				double a = stack.pop();
				double res = applyOperator(a, b, token);
				stack.push(res);
			}
		}
		if (stack.size() != 1) {
			throw new Exception("Invalid Expression");
		}
		return stack.pop();
	}
	private double applyOperator(double a, double b, String operator) throws Exception {
		switch (operator) {
			case "+":
				return a + b;
			case "-":
				return a - b;
			case "x":
				return a * b;
			case "/":
				if (b == 0) {
					throw new Exception("Division by zero");
				}
				return a / b;
			default:
				throw new Exception("Unknown operator: " + operator);
		}
	}
	private boolean isOperator(String token) {
		return token.equals("+") || token.equals("-") || token.equals("x") || token.equals("/");
	}
	private int getPrecedence(String operator) {
		switch (operator) {
			case "+":
			case "-":
				return 1;
			case "x":
			case "/":
				return 2;
			default:
				return 0;
		}
	}
	private boolean isNumber(String token) {
		try {
			Double.parseDouble(token);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}