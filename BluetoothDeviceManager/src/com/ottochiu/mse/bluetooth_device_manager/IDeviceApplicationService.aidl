package com.ottochiu.mse.bluetooth_device_manager;

interface IDeviceApplicationService {
  
  void registerDevice(String deviceName, String uuid, String packageName);
}
