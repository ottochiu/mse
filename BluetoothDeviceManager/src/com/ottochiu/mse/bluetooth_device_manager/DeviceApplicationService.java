package com.ottochiu.mse.bluetooth_device_manager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

class DeviceApplicationService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
}