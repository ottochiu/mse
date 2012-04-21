package com.ottochiu.mse.bluetooth_device_manager;
import java.io.IOException;
import java.util.Hashtable;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class BluetoothService extends Service {

	public static final String ACTION_LOG = "com.ottochiu.mse.bluetooth_device_manager.ACTION_LOG";
	public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
	
	public static final String ACTION_BT_STATUS = "com.ottochiu.mse.bluetooth_device_manager.ACTION_BT_STATUS";
	public static final String EXTRA_BT_STATUS = "EXTRA_BT_STATUS";
	
	private static final String TAG = "Bluetooth Service";
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			
	private Hashtable<String, BtConnection> connections;
	private Binder binder = new BtBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "OnBind BluetoothService");
		return binder;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Start BluetoothService");
		
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(broadcastReceiver);
	}
	
	// Binder
	public class BtBinder extends Binder {
		BluetoothService getService() {
			return BluetoothService.this;
		}
	}
	
	
	public void setEnable(boolean enable) {
    	if (enable) {
    		if (btAdapter.isEnabled()) {
    			updateStatus("Bluetooth is already on");
    		} else {
    			updateStatus("Turning on Bluetooth");
    			if (!btAdapter.enable()) {
    				updateStatus("Failed turning on Bluetooth. Please try again.");
    			}
    		}	
    	} else {
    		if (btAdapter.isEnabled()) {
    			updateStatus("Turning off Bluetooth");
    			if (!btAdapter.disable()) {
    				updateStatus("Failed turning off Bluetooth. Please try again.");
    			}
    				
    		} else {
    			updateStatus("Bluetooth is already off");
    		}
    	}
	}
	
	public void reserveConnection(String deviceName, UUID uuid, IBluetoothReadCallback callback)
	{
		// Will overwrite UUID if the same deviceName is used
		try {
			connections.put(
					deviceName,
					new BtConnection(callback, deviceName, uuid));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public BtConnection getBtConnection(String deviceName) {
		return connections.get(deviceName);
	}
	
	private void updateStatus(String msg) {
		Log.i(TAG, msg);
		Intent intent = new Intent(ACTION_LOG);
		intent.putExtra(EXTRA_MESSAGE, msg);
		sendBroadcast(intent);
	}
	
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				
				String connectionStatus = "Bluetooth is ";
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                case BluetoothAdapter.STATE_OFF:
                	connectionStatus += "OFF";
                	break;
                case BluetoothAdapter.STATE_ON:
                	connectionStatus += "ON";
                	break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                	connectionStatus += "TURNING OFF";
                	break;
                case BluetoothAdapter.STATE_TURNING_ON:
                	connectionStatus += "TURNING ON";
                	break;
                default:
                	connectionStatus = "Bluetooth error";
                	break;
                }
                
                Intent statusIntent = new Intent(ACTION_BT_STATUS);
                statusIntent.putExtra(EXTRA_BT_STATUS, connectionStatus);
                
                sendBroadcast(statusIntent);
			}
		}
	};
}
