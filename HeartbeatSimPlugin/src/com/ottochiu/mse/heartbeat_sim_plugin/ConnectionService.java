package com.ottochiu.mse.heartbeat_sim_plugin;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

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
	public static final String STATUS_UPDATE = "com.ottochiu.mse.heartbeat_simulator_plugin.STATUS_UPDATE";
	public static final String STATUS_MESSAGE = "com.ottochiu.mse.heartbeat_simulator_plugin.STATUS_MESSAGE";
	
	private static final String TAG = "ConnectionService";
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "Binded");
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Started");
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		
		// TODO:
		if (applicationService != null) {
			unbindService(connection);
		}
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

	// interface exposed to activity
	
	void registerPlugin() {
		new RegisterPluginTask().execute();
	}
	
	void appendStatus(String msg) {
		status += msg + "\n";
	}
	
	String getStatus() {
		return status;
	}

	
	// private components for use within this service
	
	private void updateStatus(String msg) {
		Intent intent = new Intent(STATUS_UPDATE);
		intent.putExtra(STATUS_MESSAGE, msg);
		
		sendBroadcast(intent);
	}
	
	private class RegisterPluginTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
	    	
	    	if (applicationService == null) {
	    		Log.i(TAG, "binding");

	    		startService(new Intent(IDeviceApplicationService.class.getName()));
	    		
	    		boolean isBounded = bindService(new Intent(IDeviceApplicationService.class.getName()),
	    				connection, Context.BIND_AUTO_CREATE);
	    		
	    		Log.i(TAG, "Service bounded: " + isBounded);
	    		
	    		return Boolean.valueOf(isBounded);  
	    		
	    	} else {
	    		Log.i(TAG, "registering device");
	    		
	        	try {
	        		String deviceName = getString(R.string.app_name);
	        		
	        		applicationService.registerDevice(
	        				deviceName,
	        				ParcelUuid.fromString(getString(R.string.uuid)),
	        				HeartbeatSimulatorPluginActivity.class.getPackage().getName());
	        		
	        		return Boolean.TRUE;
	        		
	    		} catch (RemoteException e) {
	    			return Boolean.FALSE;	    			
	    		}
	    	}
		}
		
		@Override
		protected void onPostExecute(Boolean b) {
			// Succeeded
			if (b.booleanValue()) {
				updateStatus("Service registered.");
			} else {
				updateStatus("Service registration failed.");
			}
		}
    	
    }
	
	////////////////////////////////////////////
	
	
	////////////////////////////////////////////
	// For connection to the DeviceApplicationService
    
	private IDeviceApplicationService applicationService;
	
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			updateStatus("Service connected.");
			applicationService = IDeviceApplicationService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			updateStatus("Service disconnected");
			applicationService = null;
		}
	};
    
    
	////////////////////////////////////////////	

}