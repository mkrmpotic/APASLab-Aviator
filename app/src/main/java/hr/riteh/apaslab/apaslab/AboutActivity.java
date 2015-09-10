package hr.riteh.apaslab.apaslab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {

	private static final String TAG = "AboutActivity";
    
    private TextView txtContentMain, txtContentUsage;
    private Button btnBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		txtContentMain = (TextView) findViewById(R.id.txt_content_main);
		txtContentUsage = (TextView) findViewById(R.id.txt_content_usage);
		btnBack = (Button) findViewById(R.id.btn_back);
		
		txtContentMain.setText("APASLab Aviator combined with APASLab Base allows precise landing of DJI Phantom 2 Vision+ quadcopter.\n\n" +
				"After passing all of the assigned waypoints, user has an option to select the autonomous landing mode.\n\n" +
				"Another device paired via Bluetooth running the APASLab Base app then sends position corrections to the primary device.");
		
		txtContentUsage.setText("1. In APASLab Base app open the menu by clicking the menu button and select \"Discoverable\".\n\n" +
				"2. Set the waypoints in the APASLab Aviator app and select connect in the menu. Choose a device you want to pair with.\n\n" +
				"3. Place the secondary device on the landing point camera facing up.\n\n" +
				"4. You are ready to go! While in flight you can choose the \"Go Home\" option for your Phantom to land.");
		
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(AboutActivity.this, MainActivity.class);
				startActivity(i);

			}

		});

	}



}
