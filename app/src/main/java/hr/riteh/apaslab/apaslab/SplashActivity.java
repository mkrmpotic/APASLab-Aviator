package hr.riteh.apaslab.apaslab;

import org.opencv.android.Utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.interfaces.DJIGerneralListener;

public class SplashActivity extends Activity {

	private static final String TAG = "SplashActivity";
	
	// Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    
    private TextView txtProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		txtProgress = (TextView)findViewById(R.id.txt_progress);

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
									if (result == 0) {
										SplashActivity.this.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												txtProgress.setText("App validated :)");
											}
										});
									}
								}
							});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
		onInitSDK();
		
		new Handler().postDelayed(new Runnable() {
			 
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
 
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
	}


	private void onInitSDK() {
		DJIDrone.initWithType(getApplicationContext(), DJIDroneType.DJIDrone_Vision);
		DJIDrone.connectToDrone();
	}

	private void onUnInitSDK() {
		DJIDrone.disconnectToDrone();
	}


}
