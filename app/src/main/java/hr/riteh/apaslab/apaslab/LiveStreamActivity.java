package hr.riteh.apaslab.apaslab;

import hr.riteh.apaslab.apaslab.misc.OnSwipeGestureListener;
import hr.riteh.apaslab.apaslab.model.Waypoint;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraPreviewResolustionType;
import dji.sdk.api.Gimbal.DJIGimbalAttitude;
import dji.sdk.api.Gimbal.DJIGimbalRotation;
import dji.sdk.api.GroundStation.DJIGroundStationFlyingInfo;
import dji.sdk.api.GroundStation.DJIGroundStationTask;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHoverResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResumeResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationTakeOffResult;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJIExecuteBooleanResultCallback;
import dji.sdk.interfaces.DJIGimbalErrorCallBack;
import dji.sdk.interfaces.DJIGimbalUpdateAttitudeCallBack;
import dji.sdk.interfaces.DJIGroundStationExecutCallBack;
import dji.sdk.interfaces.DJIGroundStationFlyingInfoCallBack;
import dji.sdk.interfaces.DJIGroundStationHoverCallBack;
import dji.sdk.interfaces.DJIGroundStationResumeCallBack;
import dji.sdk.interfaces.DJIGroundStationTakeOffCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

public class LiveStreamActivity extends Activity implements OnClickListener {

	private static final String TAG = "LiveStreamActivity";

	private static final int LANDING_STATE_ROTATING_CW_FAST = 1;
	private static final int LANDING_STATE_ROTATING_CW_SLOW = 2;
	private static final int LANDING_STATE_ROTATING_CCW_FAST = 3;
	private static final int LANDING_STATE_ROTATING_CCW_SLOW = 4;
	private static final int LANDING_STATE_MOVING_NORTH_FAST = 5;
	private static final int LANDING_STATE_MOVING_NORTH_SLOW = 6;
	private static final int LANDING_STATE_MOVING_SOUTH_FAST = 7;
	private static final int LANDING_STATE_MOVING_SOUTH_SLOW = 8;
	private static final int LANDING_STATE_MOVING_WEST_FAST = 9;
	private static final int LANDING_STATE_MOVING_WEST_SLOW = 10;
	private static final int LANDING_STATE_MOVING_EAST_FAST = 11;
	private static final int LANDING_STATE_MOVING_EAST_SLOW = 12;
	private static final int LANDING_STATE_DESCENDING = 13;
	private static final int LANDING_STATE_LANDING = 14;

	// --------------- BT--------------------------------

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	// --------------------------------------

	private LatLng previousPoint = new LatLng(45.372690, 14.394425);
	private GoogleMap map;
	private Marker marin;

	private boolean flying = false;

	private TextView txtConnectionState;

	private LinearLayout llButtonContainer;

	private TextView txtHomeLatitude;
	private TextView txtHomeLongitude;
	private TextView txtPhantomLatitude;
	private TextView txtPhantomLongitude;
	private TextView txtPhantomAltitude;
	private TextView txtPhantomCompass;

	private TextView txtLandingText;
	private ImageView imgLandingIcon;

	private boolean landing;

	private int newGimbalPitch;

	private TextView txtSatelliteCount;

	private final int SHOWTOAST = 1;
	private DJIGroundStationTask mTask;

	private DJIMcuUpdateStateCallBack mcuUpdateStateCallBack = null;
	private DJIGroundStationFlyingInfoCallBack mGroundStationFlyingInfoCallBack = null;
	private DJIGimbalErrorCallBack mGimbalErrorCallBack;
	private DJIGimbalUpdateAttitudeCallBack mGimbalUpdateAttitudeCallBack;

	private GestureDetectorCompat mDetector;

	private double altitudeOffset = 0;

	private double homeLatitude;
	private double homeLongitude;
	private double phantomLatitude;
	private double phantomLongitude;
	private double phantomAltitude;
	private double phantomYaw;

	private double satelliteCount;

	private int flyingState;

	private Button mOpenGsButton;
	private Button mAddOneWaypointButton;
	private Button mUploadWaypointButton;
	private Button mTakeOffButton;
	private Button mGohomeButton;
	private Button mCloseGsButton;
	private Button mPauseResumeGsButton;

	private boolean goHome = false;
	private boolean goDown = false;
	private boolean rotating = false;
	private boolean goingStraight = false;
	private boolean goingSideways = false;
	private int yaw = 0;
	private int descending = 0; // 0 - steady, 1 - descending,
	private int throttle = 0;
	private double fullAngle = 0;

	private int descendCounter = 50;

	private int currentQuadrant = 0;

	private int mPitch;
	private int mRoll;

	private ArrayList<Waypoint> waypoints;

	private double offsetX = 0;
	private double offsetY = 0;

	private Timer timer;
	private Timer landingTimer;
	private Timer straightTimer;

	private double mGimbalPitch;

	ImageView iv;

	class Task extends TimerTask {

		@Override
		public void run() {
			DJIDrone.getDjiMC().startUpdateTimer(1000); //this is an ugly fix
			DJIDrone.getDjiGroundStation().startUpdateTimer(1000); //this one also

			checkConnectionState();
			if (goHome) {
				if (checkIfCurrentPositionIsHome()) {
					goHome = false;
					beginLanding();
				}
			}
		}

	};

	class LandingTask extends TimerTask {

		@Override
		public void run() {
			if (goDown) {
				descendCounter--;
				if (descendCounter >= 0) {
					descend();
				} else {
					//sendMessage("InitialPosition?");
				}
			}
			if (rotating) {
				controlRotation();
			}
		//	if (goingStraight) {
		//		sendMessage("CurrentPosition?");
		//	}
		}

	};

	class StraightTask extends TimerTask {

		@Override
		public void run() {
			if (goingStraight) {
				sendMessage("CurrentPosition?");
			}
			if (goingSideways) {
				sendMessage("CurrentPosition??");
			}
		}

	};

	private void checkConnectionState() {

		LiveStreamActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				marin.setPosition(new LatLng(phantomLatitude, phantomLongitude));
				marin.setRotation((float) phantomYaw);
				if (map != null) {
					//map.moveCamera(CameraUpdateFactory.newLatLngZoom(
					//		new LatLng(phantomLatitude, phantomLongitude), 19));
				}
				boolean bConnectState = DJIDrone.getDjiCamera()
						.getCameraConnectIsOk();
				if (bConnectState) {
					txtConnectionState.setText(R.string.camera_connected);
				} else {
					txtConnectionState.setText(R.string.camera_disconnected);
				}
			}
		});

	}

	private Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SHOWTOAST:
				setResultToToast((String) msg.obj);
				break;

			default:
				break;
			}
			return false;
		}
	});

	private void setResultToToast(String result) {
		Toast.makeText(LiveStreamActivity.this, result, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_live_stream);

		mDetector = new GestureDetectorCompat(this,
				new OnSwipeGestureListener() {
					@Override
					public void onSwipe(final float distanceY) {

						if ((int) (distanceY * 30 + mGimbalPitch) > 1000) {
							newGimbalPitch = 1000;
						} else if ((int) (distanceY * 30 + mGimbalPitch) < 0) {
							newGimbalPitch = 0;
						} else {
							newGimbalPitch = (int) (distanceY * 30 + mGimbalPitch);
						}
						new Thread() {
							public void run() {

								DJIGimbalRotation mPitch = new DJIGimbalRotation(
										true, false, true, newGimbalPitch);

								DJIDrone.getDjiGimbal().updateGimbalAttitude(
										mPitch, null, null);

							}
						}.start();
					}
				});

		// ---------------------------- BT ------------------

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.txt_bluetooth_connection);
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// ----------------------------------------

		waypoints = getIntent().getParcelableArrayListExtra("waypoints");
		previousPoint = new LatLng(getIntent().getDoubleExtra("startLat", 45.372690), getIntent().getDoubleExtra("startLon", 14.394425));


		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		if (map != null) {
			marin = map.addMarker(new MarkerOptions()
					.position(previousPoint)
					.title("Home")
					.flat(true)
					.rotation((float) phantomYaw)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_drone)));
			marin.setAnchor(0.5f, 0.5f);
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(previousPoint, 19));

			for (Waypoint waypoint : waypoints) {
				map.addMarker(
						new MarkerOptions().position(
								new LatLng(waypoint.getLatitude(), waypoint
										.getLongitude())).icon(
								BitmapDescriptorFactory
										.fromResource(R.drawable.ic_flag)))
						.setAnchor(0.5f, 1.0f);

				Polyline line = map.addPolyline(new PolylineOptions()
						.add(previousPoint,
								new LatLng(waypoint.getLatitude(), waypoint
										.getLongitude())).width(6)
						.color(Color.parseColor("#4Df1c40f")));

				previousPoint = new LatLng(waypoint.getLatitude(),
						waypoint.getLongitude());
			}

		}

		txtLandingText = (TextView) findViewById(R.id.landing_text);
		imgLandingIcon = (ImageView) findViewById(R.id.landing_icon);

		txtConnectionState = (TextView) findViewById(R.id.txt_connection_state);

		llButtonContainer = (LinearLayout) findViewById(R.id.ll_buttons_container);

		txtHomeLatitude = (TextView) findViewById(R.id.txt_home_latitude);
		txtHomeLongitude = (TextView) findViewById(R.id.txt_home_longitude);
		txtPhantomLatitude = (TextView) findViewById(R.id.txt_phantom_latitude);
		txtPhantomLongitude = (TextView) findViewById(R.id.txt_phantom_longitude);
		txtPhantomAltitude = (TextView) findViewById(R.id.txt_phantom_altitude);
		txtPhantomCompass = (TextView) findViewById(R.id.txt_phantom_compass);

		txtSatelliteCount = (TextView) findViewById(R.id.txt_satellite_count);

		mOpenGsButton = (Button) findViewById(R.id.OpenGsButton);
		mAddOneWaypointButton = (Button) findViewById(R.id.AddWaypointButton);
		mUploadWaypointButton = (Button) findViewById(R.id.UploadWaypointButton);
		mTakeOffButton = (Button) findViewById(R.id.TakeOffButton);
		mGohomeButton = (Button) findViewById(R.id.GohomeButton);
		mCloseGsButton = (Button) findViewById(R.id.CloseGsButton);
		mPauseResumeGsButton = (Button) findViewById(R.id.PauseResumeButton);


		mOpenGsButton.setOnClickListener(this);
		mAddOneWaypointButton.setOnClickListener(this);
		mUploadWaypointButton.setOnClickListener(this);
		mTakeOffButton.setOnClickListener(this);
		mGohomeButton.setOnClickListener(this);
		mCloseGsButton.setOnClickListener(this);
		mPauseResumeGsButton.setOnClickListener(this);

        mcuUpdateStateCallBack = new DJIMcuUpdateStateCallBack() {

            @Override
            public void onResult(DJIMainControllerSystemState state) {
                // TODO Auto-generated method stub

                homeLatitude = state.homeLocationLatitude;
                homeLongitude = state.homeLocationLongitude;

                phantomLatitude = state.phantomLocationLatitude;
                phantomLongitude = state.phantomLocationLongitude;
                phantomAltitude = state.altitude;

                phantomYaw = state.yaw;

                satelliteCount = state.satelliteCount;

                Log.e("UPDATING TIMER...", "1");

                LiveStreamActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        txtHomeLatitude.setText("Home Latitude: "
                                + Double.toString(homeLatitude));
                        txtHomeLongitude.setText("Home Longitude: "
                                + Double.toString(homeLongitude));
                        txtPhantomLatitude.setText("Phantom Latitude: "
                                + Double.toString(phantomLatitude));
                        txtPhantomLongitude.setText("Phantom Longitude: "
                                + Double.toString(phantomLongitude));
						txtPhantomCompass.setText("Phantom Compass: "
								+ Double.toString(phantomYaw));
                        txtPhantomAltitude.setText("Phantom Altitude: "
                                + Double.toString(phantomAltitude));
                        txtSatelliteCount.setText("Satellite Count: "
                                + Double.toString(satelliteCount));
                    }
                });
            }

        };

        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(mcuUpdateStateCallBack);

		/*
		djiSurfaceView = (DjiGLSurfaceView) findViewById(R.id.dji_surface_view);

		djiSurfaceView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mDetector.onTouchEvent(event);
			}
		});

		djiSurfaceView
				.setStreamType(CameraPreviewResolustionType.Resolution_Type_640x480_30fps);
		djiSurfaceView.start();

		receivedVideoDataCallBack = new DJIReceivedVideoDataCallBack() {
			// byte[] blabla = new byte[300];

			@Override
			public void onResult(byte[] videoBuffer, int size) {


				// Log.e("size: ", Integer.toString(size));
				// Log.e("buffer length: ",
				// Integer.toString(videoBuffer.length));

				// TODO Auto-generated method stub
				djiSurfaceView.setDataToDecoder(videoBuffer, size);

			}

		};

		DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(
				receivedVideoDataCallBack);


		*/
		mGroundStationFlyingInfoCallBack = new DJIGroundStationFlyingInfoCallBack() {

			@Override
			public void onResult(DJIGroundStationFlyingInfo flyingInfo) {
				// TODO Auto-generated method stub
				// Log.e(TAG, "DJIGroundStationFlyingInfo homeLocationLatitude "
				// +flyingInfo.homeLocationLatitude);
				// Log.e(TAG,
				// "DJIGroundStationFlyingInfo homeLocationLongitude "
				// +flyingInfo.homeLocationLongitude);
				flyingState = flyingInfo.controlMode.value();

			}

		};

		mGimbalErrorCallBack = new DJIGimbalErrorCallBack() {

			@Override
			public void onError(int error) {
				// TODO Auto-generated method stub
				// Log.d(TAG, "Gimbal error = "+error);
			}

		};

		mGimbalUpdateAttitudeCallBack = new DJIGimbalUpdateAttitudeCallBack() {

			@Override
			public void onResult(DJIGimbalAttitude altitude) {
				// TODO Auto-generated method stub
				// Log.d(TAG, attitude.toString());

				mGimbalPitch = altitude.pitch;

			}

		};

		DJIDrone.getDjiGimbal().setGimbalErrorCallBack(mGimbalErrorCallBack);
		DJIDrone.getDjiGimbal().setGimbalUpdateAttitudeCallBack(
				mGimbalUpdateAttitudeCallBack);

		DJIDrone.getDjiGroundStation().setGroundStationFlyingInfoCallBack(
				mGroundStationFlyingInfoCallBack);
		mTask = new DJIGroundStationTask();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.OpenGsButton:

			// if(!checkGetHomePoint()) return;
			DJIDrone.getDjiGroundStation().openGroundStation(
					new DJIGroundStationExecutCallBack() {

						@Override
						public void onResult(GroundStationResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.toString();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}

					});
			break;

		case R.id.AddWaypointButton:
			// if(!checkGetHomePoint()) return;

			// north
			DJIGroundStationWaypoint mWayPoint1 = new DJIGroundStationWaypoint(
					homeLatitude + 0.0000899322, homeLongitude);
			mWayPoint1.altitude = 7;
			mWayPoint1.speed = 2; // slow 2
			mWayPoint1.heading = 360;
			mWayPoint1.maxReachTime = 0;
			mWayPoint1.stayTime = 3;
			mTask.addWaypoint(mWayPoint1);

			break;

		case R.id.GohomeButton:
			// if(!checkGetHomePoint()) return;
			mTask.RemoveAllWaypoint();
			// north
			DJIGroundStationWaypoint mWayPoint2 = new DJIGroundStationWaypoint(
					homeLatitude, homeLongitude);
			mWayPoint2.altitude = 15 + (float) altitudeOffset;
			mWayPoint2.speed = 1; // slow 2
			mWayPoint2.heading = 360;
			mWayPoint2.maxReachTime = 0;
			mWayPoint2.stayTime = 3;
			mTask.addWaypoint(mWayPoint2);

			DJIDrone.getDjiGroundStation().uploadGroundStationTask(mTask,
					new DJIGroundStationExecutCallBack() {

						@Override
						public void onResult(GroundStationResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.toString();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}

					});

			DJIDrone.getDjiGroundStation().startGroundStationTask(
					new DJIGroundStationTakeOffCallBack() {

						@Override
						public void onResult(GroundStationTakeOffResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.toString();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
							goHome = true;
						}
					});

			break;

		case R.id.UploadWaypointButton:
			// if(!checkGetHomePoint()) return;

			DJIDrone.getDjiGroundStation().uploadGroundStationTask(mTask,
					new DJIGroundStationExecutCallBack() {

						@Override
						public void onResult(GroundStationResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.toString();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}

					});
			break;

		case R.id.TakeOffButton:
			// if(!checkGetHomePoint()) return;
			/*
			 * DJIDrone.getDjiMC().setAircraftHomeGpsLocation(phantomLatitude,
			 * phantomLongitude, new DJIExecuteResultCallback() {
			 *
			 * @Override public void onResult(DJIError result) { String
			 * ResultsString = "return code =" + result;
			 * handler.sendMessage(handler.obtainMessage( SHOWTOAST,
			 * ResultsString)); }
			 *
			 * });
			 */

			altitudeOffset = phantomAltitude;

			mTakeOffButton.setVisibility(View.INVISIBLE);
			llButtonContainer.setVisibility(View.VISIBLE);

			DJIDrone.getDjiGroundStation().closeGroundStation(
					new DJIGroundStationExecutCallBack() {

						@Override
						public void onResult(GroundStationResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.value();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}

					});

			flying = true;

			DJIDrone.getDjiGroundStation().openGroundStation(
					new DJIGroundStationExecutCallBack() {

						@Override
						public void onResult(GroundStationResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.toString();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}

					});

			for (Waypoint waypoint : waypoints) {
				DJIGroundStationWaypoint mWayPoint = new DJIGroundStationWaypoint(
						waypoint.getLatitude(), waypoint.getLongitude());
				mWayPoint.altitude = (float) waypoint.getAltitude();
				mWayPoint.speed = 2; // slow 2
				mWayPoint.heading = 360;
				mWayPoint.maxReachTime = 0;
				mWayPoint.stayTime = 3;
				mTask.addWaypoint(mWayPoint);
			}

			DJIDrone.getDjiGroundStation().uploadGroundStationTask(mTask,
					new DJIGroundStationExecutCallBack() {

						@Override
						public void onResult(GroundStationResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.toString();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}

					});

			DJIDrone.getDjiGroundStation().startGroundStationTask(
					new DJIGroundStationTakeOffCallBack() {

						@Override
						public void onResult(GroundStationTakeOffResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.toString();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}
					});
			break;

		case R.id.CloseGsButton:
			// if(!checkGetHomePoint()) return;
			DJIDrone.getDjiGroundStation().closeGroundStation(
					new DJIGroundStationExecutCallBack() {

						@Override
						public void onResult(GroundStationResult result) {
							// TODO Auto-generated method stub
							String ResultsString = "return code ="
									+ result.value();
							handler.sendMessage(handler.obtainMessage(
									SHOWTOAST, ResultsString));
						}

					});
			break;

		case R.id.PauseResumeButton:
			// if(!checkGetHomePoint()) return;

			if (flying) {
				flying = false;
				mPauseResumeGsButton.setText("Resume");
				DJIDrone.getDjiGroundStation().pauseGroundStationTask(
						new DJIGroundStationHoverCallBack() {

							@Override
							public void onResult(GroundStationHoverResult result) {
								// TODO Auto-generated method stub
								String ResultsString = "return code ="
										+ result.value();
								handler.sendMessage(handler.obtainMessage(
										SHOWTOAST, ResultsString));
							}
						});
			} else {
				flying = true;
				mPauseResumeGsButton.setText("Pause");
				DJIDrone.getDjiGroundStation().continueGroundStationTask(
						new DJIGroundStationResumeCallBack() {

							@Override
							public void onResult(
									GroundStationResumeResult result) {
								// TODO Auto-generated method stub
								String ResultsString = "return code ="
										+ result.value();
								handler.sendMessage(handler.obtainMessage(
										SHOWTOAST, ResultsString));
							}

						});
			}

			break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		timer = new Timer();
		Task task = new Task();
		timer.schedule(task, 0, 500);

		landingTimer = new Timer();
		LandingTask landingTask = new LandingTask();
		landingTimer.schedule(landingTask, 0, 100);

		straightTimer = new Timer();
		StraightTask straightTask = new StraightTask();
		straightTimer.schedule(straightTask, 0, 1000);

		//DJIDrone.getDjiMC().startUpdateTimer(1000);
		//DJIDrone.getDjiGroundStation().startUpdateTimer(1000);
		//DJIDrone.getDjiGimbal().startUpdateTimer(1000);



		// ----------------- BT ----------------------

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}

		// ----------------------------

        super.onResume();

	}

	// -------------------------- BT ---------------------

	private void setupChat() {
		Log.d(TAG, "setupChat()");
		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);
		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);
		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);
		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	// ------------------------

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}

		if (landingTimer != null) {
			landingTimer.cancel();
			landingTimer.purge();
			landingTimer = null;
		}

		if (straightTimer != null) {
			straightTimer.cancel();
			straightTimer.purge();
			straightTimer = null;
		}

		DJIDrone.getDjiMC().stopUpdateTimer();
		DJIDrone.getDjiGroundStation().stopUpdateTimer();
		//DJIDrone.getDjiGimbal().stopUpdateTimer();

        Log.e("PAUSING ACTIVITY", "1");

		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub


		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//djiSurfaceView.destroy();
		//DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);


		super.onDestroy();

		// ------------------ BT --------------------

		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();

		// ----------------------------

	}

	// ---------------------- BT ----------------------

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	// -----------------------------

	private boolean checkIfCurrentPositionIsHome() {
        Log.e("MAROOOO", Integer.toString(flyingState));
		if (cooDistance(homeLatitude, homeLongitude, phantomLatitude,
				phantomLongitude) < 2 && flyingState == 8) {
            Log.e("DOMA SAM!", Integer.toString(flyingState));
	/*	if (flyingState == 8) { */
			return true;
		} else {
			return false;
		}
	}

	// returning meters
	private double cooDistance(double lat1, double lon1, double lat2,
			double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344 * 1000;
		return (dist);
	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	private void beginLanding() {
		DJIDrone.getDjiGroundStation().pauseGroundStationTask(
				new DJIGroundStationHoverCallBack() {

					@Override
					public void onResult(GroundStationHoverResult result) {
						// TODO Auto-generated method stub
						String ResultsString = "PAUSING return code ="
								+ result.toString();
						handler.sendMessage(handler.obtainMessage(SHOWTOAST,
								ResultsString));
						// sendMessage("InitialPosition?");
						rotateToSouth();
					}
				});

		mTask.RemoveAllWaypoint();

		// goDown = true;

		// Attempt to connect to the device
		// Intent data = new Intent();
		// data.putExtra(EXTRA_DEVICE_ADDRESS, "18:E2:C2:93:32:73");
		// connectDevice(data, true);

		// if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
		// {
		// sendMessage("Give me offset.");
		// }

	}

	private void rotateToSouth() {
		if ((phantomYaw > 4) || (phantomYaw < -4)) {
			if (phantomYaw < -4) {
				if (phantomYaw < -40) {
					setLandingIcon(LANDING_STATE_ROTATING_CW_FAST);
					yaw = 200;
				} else {
					setLandingIcon(LANDING_STATE_ROTATING_CW_SLOW);
					yaw = 100;
				}
			} else {
				if (phantomYaw > 40) {
					setLandingIcon(LANDING_STATE_ROTATING_CCW_FAST);
					yaw = -200;
				} else {
					setLandingIcon(LANDING_STATE_ROTATING_CCW_SLOW);
					yaw = -100;
				}
			}
			rotating = true;
			new Thread() {
				public void run() {

					DJIDrone.getDjiGroundStation().setAircraftJoystick(yaw, 0, 0,
							0, new DJIGroundStationExecutCallBack() {

								@Override
								public void onResult(GroundStationResult result) {
									//rotating = true;
									// TODO Auto-generated method stub

								}

							});
				}
			}.start();
		} else {
			yaw = 0;
			rotating = false;
			new Thread() {
				public void run() {
					// now stop rotating
					DJIDrone.getDjiGroundStation().setAircraftJoystick(0, 0,
							0, 0, new DJIGroundStationExecutCallBack() {

								@Override
								public void onResult(GroundStationResult result) {
									// TODO Auto-generated method stub
									sendMessage("InitialPosition?");
									//goingStraight = true;
								}

							});
				}
			}.start();
		}

	}

	// ------------------- BT ------------------------

	/**
	 * Sends a message.
	 *
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			handler.sendMessage(handler.obtainMessage(SHOWTOAST,
					R.string.not_connected));

			return;
		}
		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);
			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}

			return true;
		}
	};

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setTextColor(Color.parseColor("#27ae60"));
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(" " + mConnectedDeviceName);
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setTextColor(Color.parseColor("#c0392b"));
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				mConversationArrayAdapter.add("Me: " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName + ": "
						+ readMessage);
				parsePosition(readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	// -----------------------------------

	@Override
	public void onStart() {
		super.onStart();

		// ------------------------ BT ----------------

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}

		// ------------------------
	}

	private void parsePosition(String message) {
		String[] offsets = message.split(" ");
		String type = offsets[0];
		offsetX = Double.parseDouble(offsets[1]);
		offsetY = Double.parseDouble(offsets[2]);

		if (type.equals("I")) {
			//correctPosition();
			moveToCenter();
		} else if (type.equals("C")) {
			controlStraight();
		} else if (type.equals("S")) {
			controlSideways();
		}
	}

	private void moveToCenter() {
		double ratio = offsetY / offsetX;
	//	mPitch = (int) Math.round((1 / (ratio + 1)) * ratio);
	//	mRoll = (int) Math.round(1 / (ratio + 1));
		int speed;
		if (phantomAltitude > 9) {
			speed = 150;
		} else {
			speed = 120;
		}

		if (offsetX > 0 && offsetY > 0) {
			// 1st quadrant
			currentQuadrant = 1;
			if (speed == 150) {
				setLandingIcon(LANDING_STATE_MOVING_NORTH_FAST);
			} else {
				setLandingIcon(LANDING_STATE_MOVING_NORTH_SLOW);
			}

			mPitch = speed;
			mRoll = -speed;
		//	mPitch = mPitch;
		//	mRoll = -mRoll;
		} else if (offsetX > 0 && offsetY < 0) {
			// 2nd quadrant
			currentQuadrant = 2;
			if (speed == 150) {
				setLandingIcon(LANDING_STATE_MOVING_SOUTH_FAST);
			} else {
				setLandingIcon(LANDING_STATE_MOVING_SOUTH_SLOW);
			}

			mPitch = -speed;
			mRoll = -speed;
			//mPitch = -mPitch;
			//mRoll = -mRoll;
		} else if (offsetX < 0 && offsetY < 0) {
			// 3rd quadrant
			currentQuadrant = 3;
			if (speed == 150) {
				setLandingIcon(LANDING_STATE_MOVING_SOUTH_FAST);
			} else {
				setLandingIcon(LANDING_STATE_MOVING_SOUTH_SLOW);
			}

			mPitch = -speed;
			mRoll = speed;
			//mPitch = -mPitch;
		//	mRoll = mRoll;
		} else if (offsetX < 0 && offsetY > 0) {
			// 4th quadrant
			currentQuadrant = 4;
			if (speed == 150) {
				setLandingIcon(LANDING_STATE_MOVING_NORTH_FAST);
			} else {
				setLandingIcon(LANDING_STATE_MOVING_NORTH_SLOW);
			}

			mPitch = speed;
			mRoll = speed;
		//	mPitch = mPitch;
		//	mRoll = mRoll;
		}

		new Thread() {
			public void run() {

				DJIDrone.getDjiGroundStation().setAircraftJoystick(0, mPitch,
						0, 0, new DJIGroundStationExecutCallBack() {

							@Override
							public void onResult(GroundStationResult result) {
								goingStraight = true;
								//sendMessage("CurrentPosition?");
								// TODO Auto-generated method stub

							}

						});
			}
		}.start();
	}

	private void correctPosition() {

		double targetAngle = Math.toDegrees(Math.atan(Math.abs(offsetY
				/ offsetX)));

		if (offsetX > 0 && offsetY > 0) {
			// 1st quadrant
			currentQuadrant = 1;
			fullAngle = targetAngle + 180;
		} else if (offsetX > 0 && offsetY < 0) {
			// 2nd quadrant
			currentQuadrant = 2;
			fullAngle = 360 - targetAngle;
		} else if (offsetX < 0 && offsetY < 0) {
			// 3rd quadrant
			currentQuadrant = 3;
			fullAngle = targetAngle;
		} else if (offsetX < 0 && offsetY > 0) {
			// 4th quadrant
			currentQuadrant = 4;
			fullAngle = 180 - targetAngle;
		}

		if ((phantomYaw > fullAngle + 6) || (phantomYaw < fullAngle - 6)) {
			if ((phantomYaw + 180) >= 360) {
				if ((fullAngle - (phantomYaw - 180)) >= 0) {
					yaw = -200;
				} else {
					yaw = 200;
				}
			} else {
				if ((fullAngle - (phantomYaw + 180)) >= 0) {
					yaw = -200;
				} else {
					yaw = 200;
				}
			}

			new Thread() {
				public void run() {

					DJIDrone.getDjiGroundStation().setAircraftJoystick(yaw, 0,
							0, 0, new DJIGroundStationExecutCallBack() {

								@Override
								public void onResult(GroundStationResult result) {
									rotating = true;
									// TODO Auto-generated method stub

								}

							});
				}
			}.start();
		} else {
			new Thread() {
				public void run() {
					// now stop rotating and start going straight
					DJIDrone.getDjiGroundStation().setAircraftJoystick(0, 150,
							0, 0, new DJIGroundStationExecutCallBack() {

								@Override
								public void onResult(GroundStationResult result) {
									// TODO Auto-generated method stub
									rotating = false;
									goingStraight = true;
									sendMessage("CurrentPosition?");
								}

							});
				}
			}.start();
		}

	}

	private void controlRotation() {
	//	if ((phantomYaw > 4) || (phantomYaw < -4)) {
			// continue rotating
		//} else {
			rotateToSouth();
	//	}
	}

	private void controlSideways() {
		if (currentQuadrant == 1) {
			if (offsetX <= 10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {

									@Override
									public void onResult(
											GroundStationResult result) {
										goingSideways = false;
										goDown = true;
										//controlRotation();
										// TODO Auto-generated method stub

									}

								});
					}
				}.start();
			}
		} else if (currentQuadrant == 2) {
			if (offsetX <= 10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {

									@Override
									public void onResult(
											GroundStationResult result) {
										goingSideways = false;
										goDown = true;

									}

								});
					}
				}.start();
			}
		} else if (currentQuadrant == 3) {
			if (offsetX >= -10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {

									@Override
									public void onResult(
											GroundStationResult result) {
										goingSideways = false;
										goDown = true;

									}

								});
					}
				}.start();
			}
		} else if (currentQuadrant == 4) {
			if (offsetX >= -10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {

									@Override
									public void onResult(
											GroundStationResult result) {
										goingSideways = false;
										goDown = true;

									}

								});
					}
				}.start();
			}
		}

	}

	private void controlStraight() {
		if (currentQuadrant == 1) {
			if (offsetY <= 10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {

									@Override
									public void onResult(
											GroundStationResult result) {
										goingStraight = false;
										goDown = false;
										descendCounter = 50;
										//controlRotation();
										// TODO Auto-generated method stub

										new Thread() {
											public void run() {
												// stop
												DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
														0, mRoll, 0, new DJIGroundStationExecutCallBack() {

															@Override
															public void onResult(
																	GroundStationResult result) {
																if (mRoll == -150) {
																	setLandingIcon(LANDING_STATE_MOVING_WEST_FAST);
																} else {
																	setLandingIcon(LANDING_STATE_MOVING_WEST_SLOW);
																}
																goingSideways = true;
																//controlRotation();
																// TODO Auto-generated method stub

															}

														});
											}
										}.start();

									}

								});
					}
				}.start();
			}
		} else if (currentQuadrant == 2) {
			if (offsetY >= -10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {

									@Override
									public void onResult(
											GroundStationResult result) {
										goingStraight = false;
										goDown = false;
										descendCounter = 50;
										//controlRotation();
										// TODO Auto-generated method stub

										new Thread() {
											public void run() {
												// stop
												DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
														0, mRoll, 0, new DJIGroundStationExecutCallBack() {

															@Override
															public void onResult(
																	GroundStationResult result) {
																if (mRoll == -150) {
																	setLandingIcon(LANDING_STATE_MOVING_WEST_FAST);
																} else {
																	setLandingIcon(LANDING_STATE_MOVING_WEST_SLOW);
																}
																goingSideways = true;
																//controlRotation();
																// TODO Auto-generated method stub

															}

														});
											}
										}.start();

									}

								});
					}
				}.start();
			}
		} else if (currentQuadrant == 3) {
			if (offsetY >= -10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {
									@Override
									public void onResult(
											GroundStationResult result) {
										goingStraight = false;
										goDown = false;
										descendCounter = 50;
										//controlRotation();
										// TODO Auto-generated method stub

										new Thread() {
											public void run() {
												// stop
												DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
														0, mRoll, 0, new DJIGroundStationExecutCallBack() {

															@Override
															public void onResult(
																	GroundStationResult result) {
																if (mRoll == 150) {
																	setLandingIcon(LANDING_STATE_MOVING_EAST_FAST);
																} else {
																	setLandingIcon(LANDING_STATE_MOVING_EAST_SLOW);
																}
																goingSideways = true;
																//controlRotation();
																// TODO Auto-generated method stub

															}

														});
											}
										}.start();

									}

								});
					}
				}.start();
			}
		} else if (currentQuadrant == 4) {
			if (offsetY <= 10) {
				new Thread() {
					public void run() {
						// stop
						DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
								0, 0, 0, new DJIGroundStationExecutCallBack() {

									@Override
									public void onResult(
											GroundStationResult result) {
										goingStraight = false;
										goDown = false;
										descendCounter = 50;
										//controlRotation();
										// TODO Auto-generated method stub
										new Thread() {
											public void run() {
												// stop
												DJIDrone.getDjiGroundStation().setAircraftJoystick(0,
														0, mRoll, 0, new DJIGroundStationExecutCallBack() {

															@Override
															public void onResult(
																	GroundStationResult result) {
																if (mRoll == 150) {
																	setLandingIcon(LANDING_STATE_MOVING_EAST_FAST);
																} else {
																	setLandingIcon(LANDING_STATE_MOVING_EAST_SLOW);
																}
																goingSideways = true;
																//controlRotation();
																// TODO Auto-generated method stub

															}

														});
											}
										}.start();

									}

								});
					}
				}.start();
			}
		}

	}

	private void descend() {
		if (landing == false) {
			setLandingIcon(LANDING_STATE_DESCENDING);
		} else {
			setLandingIcon(LANDING_STATE_LANDING);
		}
		if (descending == 0) {
			throttle = 2; // go down
			descending = 1;
		} else {
			throttle = 0; // stay
			descending = 0;
		}
		new Thread() {
			public void run() {

				DJIDrone.getDjiGroundStation().setAircraftJoystick(0, 0, 0,
						throttle, new DJIGroundStationExecutCallBack() {

							@Override
							public void onResult(GroundStationResult result) {
								// TODO Auto-generated method stub

							}

						});
			}
		}.start();

		if (descendCounter == 0 && phantomAltitude >= 4.5 + altitudeOffset) {
			goDown = false;
			rotateToSouth();
		}

		if (phantomAltitude < 4.5 + altitudeOffset) {
			landing = true;
			goDown = true;
			descendCounter = 90;
		}
	}

	private void setLandingIcon(final int state) {
		LiveStreamActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				imgLandingIcon.setVisibility(View.VISIBLE);
				txtLandingText.setVisibility(View.VISIBLE);
				switch (state) {
					case 1:
						imgLandingIcon.setImageResource(R.drawable.ic_rotate_clockwise);
						txtLandingText.setText("Rotating fast clockwise");
						break;
					case 2:
						imgLandingIcon.setImageResource(R.drawable.ic_rotate_clockwise);
						txtLandingText.setText("Rotating slowly clockwise");
						break;
					case 3:
						imgLandingIcon.setImageResource(R.drawable.ic_rotate_counterclockwise);
						txtLandingText.setText("Rotating fast counterclockwise");
						break;
					case 4:
						imgLandingIcon.setImageResource(R.drawable.ic_rotate_counterclockwise);
						txtLandingText.setText("Rotating slowly counterclockwise");
						break;
					case 5:
						imgLandingIcon.setImageResource(R.drawable.ic_move_north);
						txtLandingText.setText("Moving fast to North");
						break;
					case 6:
						imgLandingIcon.setImageResource(R.drawable.ic_move_north);
						txtLandingText.setText("Moving slowly to North");
						break;
					case 7:
						imgLandingIcon.setImageResource(R.drawable.ic_move_south);
						txtLandingText.setText("Moving fast to South");
						break;
					case 8:
						imgLandingIcon.setImageResource(R.drawable.ic_move_south);
						txtLandingText.setText("Moving slowly to South");
						break;
					case 9:
						imgLandingIcon.setImageResource(R.drawable.ic_move_west);
						txtLandingText.setText("Moving fast to West");
						break;
					case 10:
						imgLandingIcon.setImageResource(R.drawable.ic_move_west);
						txtLandingText.setText("Moving slowly to West");
						break;
					case 11:
						imgLandingIcon.setImageResource(R.drawable.ic_move_east);
						txtLandingText.setText("Moving fast to East");
						break;
					case 12:
						imgLandingIcon.setImageResource(R.drawable.ic_move_east);
						txtLandingText.setText("Moving slow to East");
						break;
					case 13:
						imgLandingIcon.setImageResource(R.drawable.ic_descend);
						txtLandingText.setText("Descending");
						break;
					case 14:
						imgLandingIcon.setImageResource(R.drawable.ic_landing);
						txtLandingText.setText("Landing");
						break;
					default:
						imgLandingIcon.setVisibility(View.GONE);
						txtLandingText.setVisibility(View.GONE);
						break;
				}
			}
		});

	}
}
