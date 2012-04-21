package com.ottochiu.mse.bluetooth_device_manager;
import java.io.IOException;
import java.util.Hashtable;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class BluetoothService extends Service {

	private static final String TAG = "Bluetooth Service";
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			
	private Hashtable<String, BtConnection> connections;
	private State state = new Idle();
	private Binder binder = new BtBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "OnBind BluetoothService");
		return binder;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Start BluetoothService");
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		
	}
	
	// Binder
	public class BtBinder extends Binder {
		BluetoothService getService() {
			return BluetoothService.this;
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
	
	
	
	/////////////////// Bluetooth adapter state ////////////////////
	
	
	
	// States
	private abstract class State {
		public String toString() {
			return "State";
		}
	}
	
	private class Idle extends State {
		public String toString() {
			return "Idle State";
		}
	}
	
	private class Enabling extends State {
		public String toString() {
			return "Enabling State";
		}
	}
	
	private class Enabled extends State {
		public String toString() {
			return "Enabled State";
		}
	}
	
	private class DiscoveryEnabled extends State {
		public String toString() {
			return "Discovery Enabled State";
		}
	}
}
