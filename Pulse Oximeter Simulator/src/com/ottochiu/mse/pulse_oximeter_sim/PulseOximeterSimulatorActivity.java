package com.ottochiu.mse.pulse_oximeter_sim;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PulseOximeterSimulatorActivity extends Activity {
	private final static String TAG = "PulseOximeterSimulatorActivity";
	private final static int levelOffset = 80;
	private View oximeterControl;
	private SeekBar seekBar;
	private TextView spO2Level;
	private Button button;
	
	// Update connection button text and status based on current state
	private void updateButtonStatus() {
		if (connectionService != null) {
			Log.i(TAG, "State changed: " + connectionService.getState());
			
			switch (connectionService.getState()) {
			case ConnectionService.STATE_OFFLINE:
				button.setText("Connect");
				button.setEnabled(true);
				oximeterControl.setVisibility(View.INVISIBLE);
				break;
				
			case ConnectionService.STATE_CONNECTING:
				button.setText("Connecting...");
				button.setEnabled(false);
				break;
				
			case ConnectionService.STATE_CONNECTED:
				button.setText("Disconnect");
				button.setEnabled(true);
				oximeterControl.setVisibility(View.VISIBLE);
				writeLevel();
				break;
				
			case ConnectionService.STATE_DISCONNECTING:
				button.setText("Disconnecting");
				button.setEnabled(false);
				oximeterControl.setVisibility(View.INVISIBLE);
				break;
				
			case ConnectionService.STATE_DISCOVERING:
				button.setText("Discovering Manager");
				button.setEnabled(false);
				break;
				
			default:
				Log.wtf(TAG, "Unknown state!");
				throw new IllegalStateException("Unknown ConnectionService state");
			}
		}
	}

	// Receiving state broadcasts from ConnectionService
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ConnectionService.STATE_CHANGED)) {
				updateButtonStatus();
			}
			
		}
	};
	
	///// Connection service
	private ConnectionService connectionService;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			connectionService = ((ConnectionService.ConnectionServiceBinder) service).getService();
			
			updateButtonStatus();
			
			IntentFilter filter = new IntentFilter(ConnectionService.STATE_CHANGED);
			registerReceiver(receiver, filter);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			connectionService = null;
		}
	};


	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        oximeterControl = findViewById(R.id.oximeterControl);
        button = (Button) findViewById(R.id.connectButton);
        
        if (BluetoothAdapter.getDefaultAdapter() == null) {
        	button.setEnabled(false);
        	button.setText("Bluetooth is unavailable");
        	
        	
        } else {
        
        	spO2Level = (TextView) findViewById(R.id.spO2Level);
        	seekBar = (SeekBar) findViewById(R.id.seekBar);

        	spO2Level.setText(seekBar.getProgress() + levelOffset + "%");
        	seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

        		@Override
        		public void onStopTrackingTouch(SeekBar arg0) {} // Don't care

        		@Override
        		public void onStartTrackingTouch(SeekBar arg0) {} // Don't care

        		@Override
        		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        			Log.v(TAG, "Changed: " + progress + ", from user: " + fromUser);
        			writeLevel();
      		}				
        	});
        	
        	// Determine status
			Intent intent = new Intent(PulseOximeterSimulatorActivity.this, ConnectionService.class); 
			startService(intent);
			
			bindService(intent, 
					connection, Context.BIND_AUTO_CREATE);
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	unregisterReceiver(receiver);
    	unbindService(connection);
    }
    
    // connect/disconnect to Bluetooth device manager
    public void connect(View v) {
    	if (connectionService != null) {
    		if (connectionService.getState() == ConnectionService.STATE_OFFLINE) {
    			Log.i(TAG, "Connecting Bluetooth");
    			connectionService.connect();
    			
    		} else if (connectionService.getState() == ConnectionService.STATE_CONNECTED) {
    			Log.i(TAG, "Disconnecting Bluetooth");
    			connectionService.disconnect();
    		}
    	}
    }
    
    private void writeLevel() {
		int level = seekBar.getProgress() + levelOffset;
		spO2Level.setText(level + "%");
		
		connectionService.write(level);
    }
}