package com.ottochiu.mse.heartbeat_sim_plugin;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
		new RegisterPluginTask().execute();
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
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
		    				HeartbeatSimulatorPluginActivity.class.getPackage().getName(),
		    				handler);

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
			
	    	try {
	    		String deviceName = getString(R.string.app_name);

	    		Log.i(TAG, "Registering " + deviceName);
	    		
	    		applicationService.registerDevice(
	    				deviceName,
	    				ParcelUuid.fromString(getString(R.string.uuid)),
	    				HeartbeatSimulatorPluginActivity.class.getPackage().getName(),
	    				handler);
	    	} catch (RemoteException e) {
	    	}
			
			IntentFilter filter = new IntentFilter("com.ottochiu.mse.bluetooth_device_manager.START_REGISTRATION");
			registerReceiver(appServiceReceiver, filter);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			updateStatus("Service disconnected");

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

			String timestamp = "";
			String joinedIntervals = "";
			
			try {
				byte[] str = new byte[buf.getInt()];
				buf.get(str);
				
				timestamp = new String(str);
				updateStatus("Start timestamp: " + timestamp);
				
				while (true) {
					long vti = buf.getLong();
					
					joinedIntervals += Long.toString(vti) + ",";
					updateStatus(vti + " ms");
				}

			} catch (BufferUnderflowException e) {
				// not an error
			}
			
			try {
		        joinedIntervals = joinedIntervals.substring(0, joinedIntervals.length()-1); // can throw IndexOutOfBoundsException
		        
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("start_time", timestamp));
		        nameValuePairs.add(new BasicNameValuePair("intervals", joinedIntervals));
		        HttpPost post = new HttpPost(getString(R.string.url));
		        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		        new DefaultHttpClient().execute(post);
		        
	            updateStatus("Data transmission completed");
	            
		    } catch (IndexOutOfBoundsException e) {
		    	updateStatus("No data transmitted");
		    } catch (ClientProtocolException e) {
		    	updateStatus("Client protocol failed");
		    } catch (IOException e) {
		    	updateStatus("IO failed");
		    }
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
		    				HeartbeatSimulatorPluginActivity.class.getPackage().getName(),
		    				handler);
		    	} catch (RemoteException e) {
		    	}
			}
		}
	};
}
