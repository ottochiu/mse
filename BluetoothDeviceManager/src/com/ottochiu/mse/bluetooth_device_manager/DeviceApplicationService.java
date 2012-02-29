package com.ottochiu.mse.bluetooth_device_manager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

class DeviceApplicationService extends Service implements IDeviceApplicationService {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerDevice(String deviceName, String uuid,
			String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
