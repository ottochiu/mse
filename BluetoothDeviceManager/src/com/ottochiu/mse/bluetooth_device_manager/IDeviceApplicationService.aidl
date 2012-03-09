package com.ottochiu.mse.bluetooth_device_manager;

import android.os.ParcelUuid;
import com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback;

interface IDeviceApplicationService {
  
  void registerDevice(String deviceName, in ParcelUuid uuid, String packageName);
  
  
  // Reads from a stream of data from the corresponding BT device. This may block
  void read(IBluetoothReadCallback callback);
  
  // Writes data to the BT device corresponding to the caller. This may block.
  void write(in List<byte> data);
  
  String version();
}
