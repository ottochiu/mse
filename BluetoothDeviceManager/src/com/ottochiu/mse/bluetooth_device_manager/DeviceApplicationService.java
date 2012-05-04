package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

public class DeviceApplicationService extends Service {

	public static final String ACTION_START_REGISTRATION = "com.ottochiu.mse.bluetooth_device_manager.ACTION_START_REGISTRATION";

	private static final String TAG = "DeviceApplicationService";
	
	private final IDeviceApplicationService.Stub binder = new IDeviceApplicationService.Stub() {		
		@Override
		public void registerDevice(
				String deviceName,
				ParcelUuid uuid,
				String packageName,
				IBluetoothReadCallback callback) throws RemoteException {

			Log.i(TAG, "Registering " + deviceName);
			bluetoothService.registerDevice(deviceName, uuid, packageName, callback);
		}

		@Override
		public String version() throws RemoteException {
			
			// default bad version name
			String version = "xx";

			try {
			  version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
			  Log.e(TAG, "Error getting version");
			}

			Log.i(TAG, version);
			return version;
		}

		// This is a blocking call
		@Override
		public void read(String deviceName) throws RemoteException {
			try {
				bluetoothService.getBtConnection(deviceName).read();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} catch (RuntimeException e) {
				Log.e(TAG, e.getMessage());
			}
		}

		@Override
		public void write(String deviceName, byte[] in) {
			try {
				bluetoothService.getBtConnection(deviceName).write(ByteBuffer.wrap(in));
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "Binded");
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Started");

		// Blocking call
		Intent btIntent = new Intent(this, BluetoothService.class);
		startService(btIntent);
		
		if (bindService(btIntent, connection, Context.BIND_AUTO_CREATE)) {
			Log.i(TAG, "Bluetooth Service bounded.");
		} else {
			Log.e(TAG, "Bluetooth Service binding failed.");
		}
		
		return START_STICKY;
	}
	
	
	/////////////////////// BluetoothService /////////////////////
	
	// DeviceApplicationService should be the only client depending on the BluetoothService.
	// Therefore, bind to the service instead of starting it.
	private BluetoothService bluetoothService;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "Bluetooth Service connected");
			bluetoothService = ((BluetoothService.BtBinder) service).getService();
			
			// Once the BluetoothService is started, broadcast to all running Plugins asking them
			// to register with the manager.
			//
			// The original design was to have the Plugins register once and save the information in 
			// a local SQLite database. However, if the user uninstalls a particular Plugin or if the
			// Plugin is not running, then it is difficult for the Manager to determine the connection
			// status.
			Log.i(TAG, "Broadcasting ACTION_START_REGISTRATION");
			Intent intent = new Intent(ACTION_START_REGISTRATION);
			sendBroadcast(intent);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bluetoothService = null;			
		}
	};

}
