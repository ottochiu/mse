package com.ottochiu.mse.bluetooth_device_manager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;

class DeviceApplicationService extends Service {

	private final RegisteredDevices devices = new RegisteredDevices(this);
	
	private final IDeviceApplicationService.Stub binder = new IDeviceApplicationService.Stub() {

		@Override
		public void registerDevice(String deviceName, ParcelUuid uuid,
				String packageName) throws RemoteException {

			devices.registerDevice(deviceName, uuid.getUuid(), packageName);
		}

		@Override
		public int version() throws RemoteException {
			return 0;
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
}
