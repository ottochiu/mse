package com.ottochiu.mse.bluetooth_device_manager;

import android.os.ParcelUuid;

interface IDeviceApplicationService {

  void registerDevice(String deviceName, in ParcelUuid uuid, String packageName);
  
  String version();
}
