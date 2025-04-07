package com.nimeshkadecha.practical_4_animatedball;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
	private BallView ballView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ballView = new BallView(this);
		setContentView(ballView);
	}
}