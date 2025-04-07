package com.nimeshkadecha.practical_6_videointent;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.FileDescriptor;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

	private static final int REQUEST_PERMISSIONS = 100;
	private MediaRecorder mediaRecorder;
	private Uri videoUri;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private VideoView videoView;
	private boolean surfaceReady = false;

	private final ActivityResultLauncher<Intent> createVideoLauncher =
									registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
										if (result.getResultCode() == RESULT_OK && result.getData() != null) {
											videoUri = result.getData().getData();
											if (surfaceReady) {
												setupMediaRecorder();
											} else {
												Toast.makeText(this, "Surface not ready yet.", Toast.LENGTH_SHORT).show();
											}
										}
									});

	private boolean hasAllPermissions() {
		return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
										ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button recordBtn = findViewById(R.id.btnRecord);
		Button stopBtn = findViewById(R.id.btnStop);
		Button playBtn = findViewById(R.id.btnPlay);
		surfaceView = findViewById(R.id.surfaceView);
		videoView = findViewById(R.id.videoView);
		surfaceHolder = surfaceView.getHolder();

		// Ensure Surface is ready before recording
		surfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(@NonNull SurfaceHolder holder) {
				surfaceReady = true;
			}

			@Override
			public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

			@Override
			public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
				surfaceReady = false;
			}
		});

		if (!hasAllPermissions()) {
			ActivityCompat.requestPermissions(this, new String[]{
											Manifest.permission.CAMERA,
											Manifest.permission.RECORD_AUDIO,
											Manifest.permission.READ_EXTERNAL_STORAGE,
											Manifest.permission.WRITE_EXTERNAL_STORAGE
			}, REQUEST_PERMISSIONS);
		}

		recordBtn.setOnClickListener(v -> {
			if (hasAllPermissions()) {
				openSaveLocation();
			} else {
				Toast.makeText(this, "Missing permissions!", Toast.LENGTH_SHORT).show();
			}
		});

		stopBtn.setOnClickListener(v -> stopRecording());

		playBtn.setOnClickListener(v -> {
			if (videoUri != null) {
				videoView.setVideoURI(videoUri);
				videoView.start();
			} else {
				Toast.makeText(this, "No video to play", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void openSaveLocation() {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Video.Media.DISPLAY_NAME, "intent_topic_video_" + System.currentTimeMillis() + ".mp4");
		values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
		values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VideoIntentApp");

		Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
		if (uri != null) {
			videoUri = uri;
			if (surfaceReady) {
				setupMediaRecorder();
			} else {
				Toast.makeText(this, "Surface not ready yet.", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "Failed to create video Uri", Toast.LENGTH_SHORT).show();
		}
	}

	private void setupMediaRecorder() {
		try {
			FileDescriptor fd = getContentResolver().openFileDescriptor(videoUri, "w").getFileDescriptor();

			mediaRecorder = new MediaRecorder();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mediaRecorder.setOutputFile(fd);
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

			mediaRecorder.prepare();
			mediaRecorder.start();
			Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
			Toast.makeText(this, "Failed to start recording.", Toast.LENGTH_SHORT).show();
		}
	}

	private void stopRecording() {
		if (mediaRecorder != null) {
			try {
				mediaRecorder.stop();
				Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
			} catch (RuntimeException e) {
				Toast.makeText(this, "Stop failed. Recording may be too short.", Toast.LENGTH_SHORT).show();
			} finally {
				mediaRecorder.release();
				mediaRecorder = null;
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_PERMISSIONS) {
			if (hasAllPermissions()) {
				Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "All permissions are required!", Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}
}
