package hr.riteh.apaslab.apaslab;

import hr.riteh.apaslab.apaslab.LiveStreamActivity.Task;
import hr.riteh.apaslab.apaslab.model.Waypoint;

import java.util.ArrayList;
import java.util.Timer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;

public class SetRouteActivity extends Activity {

	private static final String TAG = "SetRouteActivity";

	private LatLng previousPoint = new LatLng(45.372690, 14.394425);
	private LatLng currentPoint = new LatLng(45.372690, 14.394425);
	private GoogleMap map;
	private Marker marin;
	
	private DJIMcuUpdateStateCallBack mMcuUpdateStateCallBack = null;
	
	private ArrayList<Waypoint> waypoints;

	private Button btnSetRoute;
	private TextView txtAltitude;
	
	private double homeLocationLatitude = -1;
    private double homeLocationLongitude = -1;
    private boolean getHomePiontFlag = false;
    private boolean homePointShown = false;
	
	private LatLng desiredPoint;
	private int desiredAltitude;
	
	private void addMarker() {

		SetRouteActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				previousPoint = new LatLng(homeLocationLatitude, homeLocationLongitude);
            	currentPoint = new LatLng(homeLocationLatitude, homeLocationLongitude);
            	marin = map.addMarker(new MarkerOptions()
				.position(previousPoint)
				.title("Base")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_drone_angleless)));
		marin.setAnchor(0.5f, 0.5f);
		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(previousPoint, 20));

		map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				// TODO Auto-generated method stub
				// lstLatLngs.add(point);
				
				desiredPoint = point;
				showAltitudeDialog();
				

			}
		});
		
		homePointShown = true;
			}
		});

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_route);

		waypoints = new ArrayList<Waypoint>();
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		if (map != null) {
			
			
			mMcuUpdateStateCallBack = new DJIMcuUpdateStateCallBack(){

	            @Override
	            public void onResult(DJIMainControllerSystemState state) {
	                // TODO Auto-generated method stub
	            	//Log.e(TAG, "DJIMainControllerSystemState homeLocationLatitude " +state.homeLocationLatitude);
	            	//Log.e(TAG, "DJIMainControllerSystemState homeLocationLongitude " +state.homeLocationLongitude);
	                homeLocationLatitude = state.phantomLocationLatitude;
	                homeLocationLongitude = state.phantomLocationLongitude;
	                
	                if(homeLocationLatitude != -1 && homeLocationLongitude != -1 && homeLocationLatitude != 0 && homeLocationLongitude != 0){
	                    getHomePiontFlag = true;
	                    if (!homePointShown) {
	                    	addMarker();
	                    }
	                }
	                else{
	                    getHomePiontFlag = false;
	                }
	            }
	           
	        };
	        
	        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(mMcuUpdateStateCallBack);
		}

		btnSetRoute = (Button) findViewById(R.id.btn_set_route);

		btnSetRoute.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(SetRouteActivity.this, LiveStreamActivity.class);
				i.putParcelableArrayListExtra("waypoints", waypoints);
				i.putExtra("startLat", homeLocationLatitude);
				i.putExtra("startLon", homeLocationLongitude);
				startActivity(i);
			}
		});

	}

	private void showAltitudeDialog() {
		// custom dialog
		desiredAltitude = 15;
		final Dialog altitudeDialog = new Dialog(this);
		altitudeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		altitudeDialog.setContentView(R.layout.dialog_set_altitude);

		// set the custom dialog components - text, image and button
		SeekBar sbAltitude = (SeekBar) altitudeDialog
				.findViewById(R.id.sb_altitude);
		TextView txtDescription = (TextView) altitudeDialog
				.findViewById(R.id.txt_desc);
		txtAltitude = (TextView) altitudeDialog.findViewById(R.id.txt_altitude);
		txtAltitude.setText("15m");
		txtDescription
				.setText("Please enter the desired altitude of the waypoint:");
		// ImageView image = (ImageView)
		// altitudeDialog.findViewById(R.id.image);
		// image.setImageResource(R.drawable.ic_launcher);

		sbAltitude.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int altitudeValue,
					boolean fromUser) {
				desiredAltitude = altitudeValue;
				txtAltitude.setText(desiredAltitude + "m");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

		});

		Button btnSet = (Button) altitudeDialog.findViewById(R.id.btn_set);
		// if button is clicked, close the custom dialog
		btnSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				altitudeDialog.dismiss();

				Marker newMarker = map.addMarker(
						new MarkerOptions().position(desiredPoint).title(desiredAltitude + "m").icon(
								BitmapDescriptorFactory
										.fromResource(R.drawable.ic_flag)));
				newMarker.setAnchor(0.5f, 1.0f);
				newMarker.showInfoWindow();
				previousPoint = currentPoint;
				currentPoint = desiredPoint;

				Polyline line = map.addPolyline(new PolylineOptions()
						.add(previousPoint, currentPoint).width(6)
						.color(Color.parseColor("#4Df1c40f")));
				waypoints.add(new Waypoint(desiredPoint.latitude, desiredPoint.longitude, desiredAltitude));
			}
		});

		altitudeDialog.show();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		DJIDrone.getDjiMC().startUpdateTimer(900);
		DJIDrone.getDjiGroundStation().startUpdateTimer(1000);

		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		
		DJIDrone.getDjiMC().stopUpdateTimer();
        DJIDrone.getDjiGroundStation().stopUpdateTimer();

		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		DJIDrone.getDjiMC().stopUpdateTimer();
        DJIDrone.getDjiGroundStation().stopUpdateTimer();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		DJIDrone.getDjiMC().stopUpdateTimer();
        DJIDrone.getDjiGroundStation().stopUpdateTimer();

		super.onDestroy();
	}

}
