package org.openhds.mobile.activity;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.Converter;
import org.openhds.mobile.Queries;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.AdapterContent;
import org.openhds.mobile.adapter.MapAdapter;
import org.openhds.mobile.database.DatabaseAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class ShowMapActivity extends MapActivity implements OnItemClickListener {
	
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // milliseconds
	private static final double METER_TO_MILE = 1609.34;
	private static final int LOCATION_RADIUS = 25;
	
	private DatabaseAdapter databaseAdapter;
	private MapController mapController;
	private MapView mapView;
	private ListView locationsView;
	private LocationManager locationManager;
	private MyLocationOverlay myLocationOverlay;
	private GeoUpdateHandler handler;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.map_view);
		
	    databaseAdapter = new DatabaseAdapter(getBaseContext());
		handler = new GeoUpdateHandler();

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);
		
		locationsView = (ListView) findViewById(R.id.locationsListView);
		locationsView.setOnItemClickListener(this);
		
		mapController = mapView.getController();
		mapController.setZoom(14); 
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			buildAlertMessageNoGPS();
		
		else {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, handler);
			
			myLocationOverlay = new MyLocationOverlay(this, mapView);
			mapView.getOverlays().add(myLocationOverlay);
	
			myLocationOverlay.runOnFirstFix(new Runnable() {
				public void run() {
					mapView.getController().animateTo(myLocationOverlay.getMyLocation());
				}
			});
			
			displayCurrentLocation();
			List<org.openhds.mobile.model.Location> nearestLocations = displayNearestLocations();
			populateListView(nearestLocations);
		}
	}

	private void displayCurrentLocation() {
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		String message = String.format("Current Location \n Longitude: %1$s \n Latitude: %2$s",
				location.getLongitude(), location.getLatitude());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	private List<org.openhds.mobile.model.Location> displayNearestLocations() {
		
		Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		double currentLatitude = currentLocation.getLatitude();
		double currentLongitude = currentLocation.getLongitude();
		
		List<org.openhds.mobile.model.Location> nearestLocations = new ArrayList<org.openhds.mobile.model.Location>();
		
		Cursor cursor = Queries.allLocations(getContentResolver());
		List<org.openhds.mobile.model.Location> locations = Converter.toLocationList(cursor);
		for (org.openhds.mobile.model.Location loc : locations) {
			
			if (loc.getLatitude().equals("Unknown") || loc.getLongitude().equals("Unknown") || 
				loc.getLatitude().equals("") || loc.getLongitude().equals(""))
				continue;
			
			double latitude = Double.parseDouble(loc.getLatitude());
			double longitude = Double.parseDouble(loc.getLongitude());
			
			float[] results = new float[1];
			Location.distanceBetween(currentLatitude, currentLongitude, latitude, longitude, results);
			float result = results[0];
			
			if (result < (METER_TO_MILE * LOCATION_RADIUS))
				nearestLocations.add(loc);
		}
		return nearestLocations;
	}
	
	private void populateListView(List<org.openhds.mobile.model.Location> nearestLocations) {
		AdapterContent[] content = new AdapterContent[nearestLocations.size()];
		
		for (int i = 0; i < nearestLocations.size(); i++) {
			content[i] = new AdapterContent(nearestLocations.get(i).getName(), nearestLocations.get(i).getExtId());
		}
		
		MapAdapter adapter = new MapAdapter(this, R.layout.custom_row, content);     
        locationsView.setAdapter(adapter);
	}
	
	private void buildAlertMessageNoGPS() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Warning");
		alertDialogBuilder.setMessage("Your GPS seems to be disabled. Please make sure that it's enabled in order to use this service.");
		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});	
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (myLocationOverlay != null) {
			myLocationOverlay.enableMyLocation();
			myLocationOverlay.enableCompass();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(handler);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
			myLocationOverlay.disableCompass();
		}
	}
		
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AdapterContent content = (AdapterContent) locationsView.getItemAtPosition(position);
		Intent i = new Intent();
		i.putExtra("name", content.getItem1());
		i.putExtra("extId", content.getItem2());
		setResult(Activity.RESULT_OK, i);
		finish();	
	}
	
	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
	        mapController.animateTo(point); 
	        displayCurrentLocation();
		}

		public void onProviderDisabled(String provider) { 
			Toast.makeText(ShowMapActivity.this, "Provider disabled by the user. GPS turned off", Toast.LENGTH_LONG).show();
		}

		public void onProviderEnabled(String provider) { 
			Toast.makeText(ShowMapActivity.this, "Provider enabled by the user. GPS turned on", Toast.LENGTH_LONG).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) { 
			Toast.makeText(ShowMapActivity.this, "Provider status changed", Toast.LENGTH_LONG).show();
		}
	}
} 