package com.ottochiu.mse.bluetooth_device_manager;

oneway interface IBluetoothReadCallback {
  void handle(in byte[] data); 
}
