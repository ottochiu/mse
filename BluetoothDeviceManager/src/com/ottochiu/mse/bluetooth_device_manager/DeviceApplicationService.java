package com.ottochiu.mse.bluetooth_device_manager;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;

public class DeviceApplicationService extends Service {

	private static final String TAG = "DeviceApplicationService";
	private final RegisteredDevices devices = new RegisteredDevices(this);
	
	private final IDeviceApplicationService.Stub binder = new IDeviceApplicationService.Stub() {

		@Override
		public void registerDevice(String deviceName, ParcelUuid uuid,
				String packageName) throws RemoteException {

			devices.registerDevice(deviceName, uuid.getUuid(), packageName);
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

		@Override
		public void read(IBluetoothReadCallback callback)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void write() {
			// TODO Auto-generated method stub
			
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
		return START_STICKY;
	}
}
