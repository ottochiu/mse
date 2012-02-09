package com.ottochiu.mse.heartbeat_simulator;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class SimulatorApplication extends Application {
	private BluetoothDevice mDevice;
	private BluetoothSocket mSocket;
	private DataSender mSender;
	
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
	
	void setSender(boolean useBluetooth) {
		mSender = useBluetooth ? new BluetoothDataSender() : new HttpDataSender(getString(R.string.url));
	}
	
	DataSender getSender() {
		return mSender;
	}
}
