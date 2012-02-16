package com.ottochiu.mse.bluetooth_device_manager;

import java.nio.ByteBuffer;

interface IBtConnectionListener {

	void log(String message);
	void handle(ByteBuffer data); 
}
