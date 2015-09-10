package hr.riteh.apaslab.apaslab;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.interfaces.DJIGerneralListener;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addListenerOnButton();

		new Thread() {
			public void run() {
				try {
					DJIDrone.checkPermission(getApplicationContext(),
							new DJIGerneralListener() {

								@Override
								public void onGetPermissionResult(int result) {
									// TODO Auto-generated method stub
									Log.e(TAG, "onGetPermissionResult = "
											+ result);
								}
							});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
		onInitSDK();
	}

	private void onInitSDK() {
		DJIDrone.initWithType(getApplicationContext(), DJIDroneType.DJIDrone_Vision);
		DJIDrone.connectToDrone();
	}

	private void onUnInitSDK() {
		DJIDrone.disconnectToDrone();
	}

	public void addListenerOnButton() {

		RelativeLayout btnFlightPrep = (RelativeLayout) findViewById(R.id.btn_flight_prep);
		RelativeLayout btnAbout = (RelativeLayout) findViewById(R.id.btn_about);

		btnFlightPrep.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent menuIntent = new Intent(MainActivity.this, SetRouteActivity.class);
				startActivity(menuIntent);

			}

		});
		
		btnAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent menuIntent = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(menuIntent);

			}

		});

	}
}
