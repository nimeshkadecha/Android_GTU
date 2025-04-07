package com.nimeshkadecha.practical_5_markermap;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
	private MapView mapView;
	private List<GeoPoint> routePoints = new ArrayList<>();
	private List<Marker> markerList = new ArrayList<>();
	private List<Polyline> polylineList = new ArrayList<>();
	private SharedPreferences sharedPreferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration.getInstance().load(getApplicationContext(),
		                                 PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
		setContentView(R.layout.activity_main);
		mapView = findViewById(R.id.mapView);
		mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setMultiTouchControls(true);
		IMapController mapController = mapView.getController();
		mapController.setZoom(10.0);
		GeoPoint startPoint = new GeoPoint(20.5937, 78.9629);
		mapController.setCenter(startPoint);
		sharedPreferences = getSharedPreferences("route_prefs", Context.MODE_PRIVATE);
		requestPermissionsIfNecessary();
		loadSavedPoints();
		drawRoute();
		MapEventsReceiver mReceive = new MapEventsReceiver() {
			@Override
			public boolean singleTapConfirmedHelper(GeoPoint p) {
				addMarker(p);return true;			}
			@Override
			public boolean longPressHelper(GeoPoint p) {
				return true;			}
		};
		MapEventsOverlay eventsOverlay = new MapEventsOverlay(mReceive);
		mapView.getOverlays().add(eventsOverlay);
	}
	private void addMarker(GeoPoint p) {
		Marker marker = new Marker(mapView);
		marker.setPosition(p);
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		marker.setTitle("Point " + (markerList.size() + 1));
		marker.setOnMarkerClickListener((m, mapView) -> {
			int index = markerList.indexOf(m);
			if (index != -1) {removeMarker(index);}
			return true;
		});
		mapView.getOverlays().add(marker);
		markerList.add(marker);
		if (markerList.size() > 1) {
			GeoPoint prev = markerList.get(markerList.size() - 2).getPosition();
			Polyline line = new Polyline();
			line.setPoints(List.of(prev, p));
			line.setColor(0xAA0000FF);
			line.setWidth(8f);
			polylineList.add(line);
			mapView.getOverlays().add(line);
		}
		saveAllMarkers();
		mapView.invalidate();
	}
	private void removeMarker(int index) {
		Marker marker = markerList.remove(index);
		mapView.getOverlays().remove(marker);
		if (index < polylineList.size()) {
			mapView.getOverlays().remove(polylineList.get(index));
			polylineList.remove(index);
		}
		if (index > 0 && index - 1 < polylineList.size()) {
			mapView.getOverlays().remove(polylineList.get(index - 1));
			polylineList.remove(index - 1);
		}
		saveAllMarkers();
		mapView.invalidate();
	}
	private void drawRoute() {
		mapView.getOverlays().clear();
		markerList.clear();		polylineList.clear();
		for (int i = 0; i < routePoints.size(); i++)
			addMarker(routePoints.get(i));
	}
	private void saveAllMarkers() {
		routePoints.clear();
		for (Marker marker : markerList)
			routePoints.add(marker.getPosition());
		savePoints();
	}
	private void savePoints() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		StringBuilder builder = new StringBuilder();
		for (GeoPoint point : routePoints)
			builder.append(point.getLatitude()).append(",").append(point.getLongitude()).append(";");
		editor.putString("route", builder.toString());
		editor.apply();
	}
	private void loadSavedPoints() {
		String routeString = sharedPreferences.getString("route", "");
		if (!routeString.isEmpty()) {
			String[] pointStrings = routeString.split(";");
			for (String pointStr : pointStrings) {
				String[] latLon = pointStr.split(",");
				if (latLon.length == 2) {
					double lat = Double.parseDouble(latLon[0]);
					double lon = Double.parseDouble(latLon[1]);
					routePoints.add(new GeoPoint(lat, lon));
				}
			}
		}
	}
	private void requestPermissionsIfNecessary() {
		String[] permissions = {
										Manifest.permission.ACCESS_FINE_LOCATION,
										Manifest.permission.ACCESS_COARSE_LOCATION
		};
		List<String> toRequest = new ArrayList<>();
		for (String permission : permissions)
			if (ActivityCompat.checkSelfPermission(this, permission)
											!= PackageManager.PERMISSION_GRANTED)
				toRequest.add(permission);
		if (!toRequest.isEmpty())
			ActivityCompat.requestPermissions(
											this,
											toRequest.toArray(new String[0]),
											REQUEST_PERMISSIONS_REQUEST_CODE			                                 );
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}
}