package com.ottochiu.mse.bluetooth_device_manager;

import android.os.ParcelUuid;
import com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback;

interface IDeviceApplicationService {
  
  void registerDevice(
    String deviceName,
    in ParcelUuid uuid,
    String packageName,
    IBluetoothReadCallback callback);
  
  
  // Reads from a stream of data from the corresponding BT device. This may block
  // Data are sent to the registered callback
  void read(String deviceName);
  
  // Writes data to the BT device corresponding to the caller. This may block.
  void write(String deviceName, in byte[] data);
  
  String version();
}
