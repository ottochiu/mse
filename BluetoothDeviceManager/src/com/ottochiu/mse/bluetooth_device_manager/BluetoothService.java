package com.ottochiu.mse.bluetooth_device_manager;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;


public class BluetoothService extends Service {

	private static final String TAG = "Bluetooth Service";
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			
	private List<BtConnection> connections;
	private State state = new Idle();
	private Binder binder = new BtBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Bundle extras = intent.getExtras();
		
		if (extras != null) {
			String deviceName = extras.getString("device name");
			UUID uuid = UUID.fromString(extras.getString("uuid"));
		
			try {
				// TODO
				connections.add(new BtConnection(null, 0, deviceName, uuid));
			} catch (IOException e) {
				// TODO: update status e.getMessage();
			}
		}
		
		
		
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
