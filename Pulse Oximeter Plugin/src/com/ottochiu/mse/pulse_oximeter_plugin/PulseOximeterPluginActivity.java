package com.ottochiu.mse.pulse_oximeter_plugin;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PulseOximeterPluginActivity extends Activity {
	private static final String TAG = "PulseOximeterPlugin";
	private static final byte INVALID_LEVEL = -1;
	
	private MovingAverage movingAverageLevel;
	private TextView spO2Pct;
	private ProgressBar spO2Level;
	private Button gotoManager;

	private BroadcastReceiver connectionServiceReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ConnectionService.STATUS_UPDATE)) {
				double level = intent.getByteExtra(ConnectionService.STATUS_LEVEL, INVALID_LEVEL);
				double displayVal = Double.MAX_VALUE;
				String displayStr = "??";
				int progress = 0;
				int color = Color.RED;
				
				if (level != INVALID_LEVEL) {
					Log.v(TAG, "Received new level: " + level);
					displayVal = movingAverageLevel.add(level);
					displayStr = String.valueOf(displayVal) + "%";
					progress = (int) (displayVal * spO2Level.getMax() / 100.0);
	
					
					if (progress >= Integer.parseInt(getString(R.string.optimal_cutoff))) {
						color = Color.GREEN;
					} else if (progress >= Integer.parseInt(getString(R.string.acute_cutoff))) {
						color = Color.YELLOW;
					}
				} else {
					Log.w(TAG, "Bad broadcast message from ConnectionService");
				}
				
				// Display value
				spO2Pct.setText(displayStr);
				spO2Pct.setTextColor(color);
				spO2Level.setProgress(progress);
				
			} else if (intent.getAction().equals(ConnectionService.STATUS_MANAGER_CONNECTED)) {
				gotoManager.setEnabled(true);
				gotoManager.setVisibility(View.VISIBLE);
			}
		}
	};
	
	private ConnectionService connectionService;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			connectionService = ((ConnectionService.ConnectionServiceBinder) service).getService();
			
	        boolean isManagerConnected = connectionService.isManagerConnected();
	        gotoManager.setVisibility(isManagerConnected ? View.VISIBLE : View.INVISIBLE);
	        gotoManager.setEnabled(isManagerConnected);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
	        unregisterReceiver(connectionServiceReceiver);
			connectionService = null;
		}
	};
	
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ((TextView) findViewById(R.id.spO2Label)).setText(Html.fromHtml("SpO<sub><small>2</small></sub>"));
        spO2Pct = (TextView) findViewById(R.id.spO2Pct);
        spO2Pct.setText("--");
        gotoManager = (Button) findViewById(R.id.gotoManager);
        
        spO2Level = (ProgressBar) findViewById(R.id.spO2Level);
        
        // Every time the view changes, restart moving average
        movingAverageLevel = new MovingAverage(
    			Integer.parseInt(getString(R.string.moving_avg_size)),
    			Double.parseDouble(getString(R.string.initial_value)));
        
        new StartConnectionService().execute();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (connectionService != null) {
    		unregisterReceiver(connectionServiceReceiver);
    		unbindService(connection);
    	}
    }
    
    // starts the Bluetooth Device Manager activity
    public void gotoManager(View v) {
    	try {
	    	Intent intent = new Intent("android.intent.action.MAIN");
	    	intent.setComponent(connectionService.getManagerComponentName());
	    	startActivity(intent);
    	} catch (ActivityNotFoundException e) {
    		// Manager not installed?
    		Log.e(TAG, "Manager not found");
    		gotoManager.setText("Manager not found");
    		gotoManager.setEnabled(false);
    	}
    }    
    
    private class StartConnectionService extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

	        IntentFilter filter = new IntentFilter(ConnectionService.STATUS_UPDATE);
	        registerReceiver(connectionServiceReceiver, filter);
	        
	        filter = new IntentFilter(ConnectionService.STATUS_MANAGER_CONNECTED);
	        registerReceiver(connectionServiceReceiver, filter);
	        
			
			Intent intent = new Intent(PulseOximeterPluginActivity.this, ConnectionService.class); 
			startService(intent);
			
			boolean isBounded = bindService(intent, 
	        		connection, Context.BIND_AUTO_CREATE);
		
			Log.i(TAG, "Connection service started: " + isBounded);
			
			return null;
		}
    }
}
