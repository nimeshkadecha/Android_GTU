package com.nimeshkadecha.practical_1;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

	private TextView answerDisplay;
	// Holds the full expression string (e.g., "1+1")
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

	// Append digits, dot, and operators (except equals, clear, and parentheses) to the expression.
	private void setNumberAndOperatorListeners() {
		int[] buttonIds = {
										R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
										R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot,
										R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
		};

		for (int id : buttonIds) {
			findViewById(id).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					TextView tv = (TextView) view;
					currentExpression += tv.getText().toString();
					answerDisplay.setText(currentExpression);
				}
			});
		}
	}

	// Equals button: evaluate the full expression and display the result.
	private void setEqualsListener() {
		findViewById(R.id.btnEquals).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					double result = evaluateExpression(currentExpression);
					answerDisplay.setText(String.valueOf(result));
					// Set the result as the new expression, allowing further calculations.
					currentExpression = String.valueOf(result);
				} catch (Exception e) {
					answerDisplay.setText("Error");
					currentExpression = "";
				}
			}
		});
	}

	// Extra functions: Clear (C) and Clear Entry (CE)
	private void setExtraListeners() {
		// Clear resets the entire expression.
		findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				currentExpression = "";
				answerDisplay.setText("0");
			}
		});

		// Clear Entry removes the last character.
		findViewById(R.id.btnClearEntry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!currentExpression.isEmpty()) {
					currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
					answerDisplay.setText(currentExpression.isEmpty() ? "0" : currentExpression);
				}
			}
		});
	}

	// Parentheses listeners: simply append them to the expression.
	private void setParenthesesListeners() {
		findViewById(R.id.btnLeftParen).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				currentExpression += "(";
				answerDisplay.setText(currentExpression);
			}
		});

		findViewById(R.id.btnRightParen).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				currentExpression += ")";
				answerDisplay.setText(currentExpression);
			}
		});
	}

	/**
		* Evaluates the arithmetic expression passed as a string.
		* This method tokenizes the expression, converts it to postfix notation,
		* and then evaluates the postfix expression.
		*
		* @param expression The arithmetic expression as a string.
		* @return The evaluated result as a double.
		*/
	private double evaluateExpression(String expression) throws Exception {
		List<String> tokens = tokenize(expression);
		List<String> postfix = infixToPostfix(tokens);
		return evaluatePostfix(postfix);
	}

	/**
		* Tokenizes the expression into numbers, operators, and parentheses.
		*/
	private List<String> tokenize(String expression) {
		List<String> tokens = new ArrayList<>();
		StringBuilder numberBuffer = new StringBuilder();
		for (int i = 0; i < expression.length(); i++) {
			char c = expression.charAt(i);
			// If character is a digit or a decimal point, add to number buffer.
			if (Character.isDigit(c) || c == '.') {
				numberBuffer.append(c);
			} else {
				// If there's a number in the buffer, add it to tokens.
				if (numberBuffer.length() > 0) {
					tokens.add(numberBuffer.toString());
					numberBuffer.setLength(0);
				}
				// Add operator or parenthesis.
				tokens.add(String.valueOf(c));
			}
		}
		// Add the last number if exists.
		if (numberBuffer.length() > 0) {
			tokens.add(numberBuffer.toString());
		}
		return tokens;
	}

	/**
		* Converts an infix token list to postfix using the Shunting-yard algorithm.
		*/
	private List<String> infixToPostfix(List<String> tokens) throws Exception {
		List<String> output = new ArrayList<>();
		Stack<String> operatorStack = new Stack<>();

		for (String token : tokens) {
			// If the token is a number, add it to the output.
			if (isNumber(token)) {
				output.add(token);
			}
			// If token is an operator.
			else if (isOperator(token)) {
				while (!operatorStack.isEmpty() && isOperator(operatorStack.peek())
												&& getPrecedence(operatorStack.peek()) >= getPrecedence(token)) {
					output.add(operatorStack.pop());
				}
				operatorStack.push(token);
			}
			// If token is left parenthesis.
			else if (token.equals("(")) {
				operatorStack.push(token);
			}
			// If token is right parenthesis.
			else if (token.equals(")")) {
				while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
					output.add(operatorStack.pop());
				}
				if (operatorStack.isEmpty()) {
					throw new Exception("Mismatched parentheses");
				}
				// Pop the left parenthesis.
				operatorStack.pop();
			}
		}
		// Pop any remaining operators.
		while (!operatorStack.isEmpty()) {
			String op = operatorStack.pop();
			if (op.equals("(") || op.equals(")")) {
				throw new Exception("Mismatched parentheses");
			}
			output.add(op);
		}
		return output;
	}

	/**
		* Evaluates a postfix expression.
		*/
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

	/**
		* Applies the operator on two operands.
		*/
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

	/**
		* Checks if a token is an operator.
		*/
	private boolean isOperator(String token) {
		return token.equals("+") || token.equals("-") || token.equals("x") || token.equals("/");
	}

	/**
		* Returns the precedence of an operator.
		* Higher number means higher precedence.
		*/
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

	/**
		* Checks if a token is a number.
		*/
	private boolean isNumber(String token) {
		try {
			Double.parseDouble(token);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}