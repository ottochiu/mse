package com.ottochiu.mse.pulse_oximeter_plugin;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

import com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback;
import com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService;

// This service interacts with the DeviceApplicationService.
// This design allows the Activity to be restarted without missing any data
// from the DeviceApplicationService.  If this service is not implemented,
// the activity needs to unbind when the activity stops and re-bind when the 
// activity starts again. During this time, data from the DeviceApplicationService
// may be dropped unexpectedly.
// This service will not be interrupted and can therefore receive all data.
// The Activity can then retrieve data it needs from this service when it is 
// done restarting.
public class ConnectionService extends Service {
	public static final String STATUS_UPDATE = "com.ottochiu.mse.pulse_oximeter_plugin.STATUS_UPDATE";
	public static final String STATUS_LEVEL = "com.ottochiu.mse.pulse_oximeter_plugin.STATUS_LEVEL";
	
	private static final String TAG = "ConnectionService";
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "Binded");
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		Log.i(TAG, "Started");
		new RegisterPluginTask().execute();
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (applicationService != null) {
			unbindService(connection);
		}
	}
	
	public IBluetoothReadCallback getHandler() {
		return handler;
	}
	
	////////////////////////////////////////////
	// To support services provided to activity
	// Also used for sending data to the activity via broadcast messages
	
    private final IBinder binder = new ConnectionServiceBinder();
    private String status = "";
	
	public class ConnectionServiceBinder extends Binder {
		ConnectionService getService() {
			return ConnectionService.this;
		}
	}

	// Allows an activity to store status messages
	void appendStatus(String msg) {
		status += msg + "\n";
	}
	
	String getStatus() {
		return status;
	}

	
	// private components for use within this service
	
	private class RegisterPluginTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
	    	
	    	if (applicationService == null) {
	    		Log.i(TAG, "binding");

	    		startService(new Intent(IDeviceApplicationService.class.getName()));
	    		
	    		boolean isBounded = bindService(new Intent(IDeviceApplicationService.class.getName()),
	    				connection, Context.BIND_AUTO_CREATE);
	    		
	    		Log.i(TAG, "Service bounded: " + isBounded);
	    		
	    	} else {
		    	Log.i(TAG, "registering device");

		    	try {
		    		String deviceName = getString(R.string.app_name);

		    		applicationService.registerDevice(
		    				deviceName,
		    				ParcelUuid.fromString(getString(R.string.uuid)),
		    				PulseOximeterPluginActivity.class.getPackage().getName(),
		    				handler);

		    		Log.i(TAG, "Service registered.");

		    	} catch (RemoteException e) {
		    		Log.e(TAG, "Service registration failed: " + e.getMessage());	    			
		    	}
	    	}
	    	
	    	return null;
		}
    }
	
	////////////////////////////////////////////
	
	
	////////////////////////////////////////////
	// For connection to the DeviceApplicationService
    
	private IDeviceApplicationService applicationService;
	
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "Service connected.");
			applicationService = IDeviceApplicationService.Stub.asInterface(service);
			
	    	try {
	    		String deviceName = getString(R.string.app_name);

	    		Log.i(TAG, "Registering " + deviceName);
	    		
	    		applicationService.registerDevice(
	    				deviceName,
	    				ParcelUuid.fromString(getString(R.string.uuid)),
	    				PulseOximeterPluginActivity.class.getPackage().getName(),
	    				handler);
	    	} catch (RemoteException e) {
	    	}
			
			IntentFilter filter = new IntentFilter("com.ottochiu.mse.bluetooth_device_manager.START_REGISTRATION");
			registerReceiver(appServiceReceiver, filter);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Service disconnected");

			unregisterReceiver(appServiceReceiver);
			applicationService = null;
		}
	};
    
    
	////////////////////////////////////////////	

	/////////////// Callback ///////////////////
	private IBluetoothReadCallback.Stub handler = new IBluetoothReadCallback.Stub() {
		@Override
		public void handle(byte[] data) throws RemoteException {
			Log.i(TAG, "received data: " + data.length + " bytes");
			
			ByteBuffer buf = ByteBuffer.wrap(data);
			buf.order(ByteOrder.LITTLE_ENDIAN);

			byte level = buf.get();
			
			Intent intent = new Intent(STATUS_UPDATE);
			intent.putExtra(STATUS_LEVEL, level);
			
			sendBroadcast(intent);
		}
	};
	
	//////////// BroadcastReceiver ////////////
	BroadcastReceiver appServiceReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals("com.ottochiu.mse.bluetooth_device_manager.START_REGISTRATION")) {
		    	try {

		    		Log.i(TAG, "Responding to register request");
		    		applicationService.registerDevice(
		    				getString(R.string.app_name),
		    				ParcelUuid.fromString(getString(R.string.uuid)),
		    				PulseOximeterPluginActivity.class.getPackage().getName(),
		    				handler);
		    	} catch (RemoteException e) {
		    	}
			}
		}
	};
}
