package com.ottochiu.mse.heartbeat_simulator;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class SimulatorApplication extends Application {
	private BluetoothDevice mDevice;
	private BluetoothSocket mSocket;
	
	static SimulatorApplication getApplication(Activity a) {
		return (SimulatorApplication) a.getApplication();
	}
	
	void setDevice(BluetoothDevice device) {
		mDevice = device;
	}
	
	BluetoothDevice getDevice() {
		return mDevice;
	}
	
	void setSocket(BluetoothSocket socket) {
		mSocket = socket;
	}
	
	BluetoothSocket getSocket() {
		return mSocket;
	}
}
