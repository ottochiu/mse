package com.ottochiu.mse.heartbeat_simulator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SendMethodActivity extends Activity {
	private static final String TAG = "SendMethodActivity";
	private static final int REQUEST_BT_ENABLE = 1;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);
		
		// Bluetooth not supported
		if (BluetoothAdapter.getDefaultAdapter() == null) {
			findViewById(R.id.bluetoothOption).setEnabled(false);
		}
	}
	
	public void useHttp(View v) {
		simulatorActivity(false);
	}
	
	public void useBluetooth(View v) {
		// Determine whether Bluetooth capability is enabled
    	try {
    		// If BT not enabled (BT adapter should not be null. Taken care of in onCreate().)
    		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {

    			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    			startActivityForResult(discoverableIntent, 	REQUEST_BT_ENABLE);

    		} else {
    			choosePairing();
    		}

    	} catch (NullPointerException e) {
    		// should not happen. disable view again
    		Log.wtf(TAG, "NullPointerException in useBluetooth()");
    		v.setEnabled(false);
    	}
	}
	

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// Check for the correct intent
    	if (requestCode == REQUEST_BT_ENABLE) {
    		if (resultCode == Activity.RESULT_OK) {
    			choosePairing();
    		}
    	}
    }
    
    
    private void choosePairing() {
    	// TODO: check for connected devices
    	// if none match name of server, then make device discoverable.
    }
    
    private void simulatorActivity(boolean isBluetoothEnabled) {
    	Intent intent = new Intent(getApplicationContext(), HeartbeatSimulatorActivity.class);
		intent.putExtra(getString(R.string.use_bluetooth_connection), isBluetoothEnabled);
        startActivity(intent);	
    }
}
