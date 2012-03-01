package com.ottochiu.mse.heartbeat_sim_plugin;

import java.util.UUID;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService;

public class HeartbeatSimulatorPluginActivity extends Activity {
	
	private static final String TAG = "HeartbeatSimulatorPluginActivity";
	private IDeviceApplicationService applicationService;
	
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e(TAG, "Service connected");
			applicationService = IDeviceApplicationService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e(TAG, "Service disconnected");
			applicationService = null;
		}
		
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void registerPlugin(View v) {
    	Log.i(TAG, "registering");
    	
    	if (applicationService == null) {
    		Log.i(TAG, "binding");
    		
    		bindService(new Intent("com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService"),
    				connection, Context.BIND_AUTO_CREATE);
    	} else {
    		Log.i(TAG, "registering device");
        	try {
    			applicationService.registerDevice("A", new ParcelUuid(UUID.randomUUID()), HeartbeatSimulatorPluginActivity.class.getPackage().getName());
    		} catch (RemoteException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    }
}