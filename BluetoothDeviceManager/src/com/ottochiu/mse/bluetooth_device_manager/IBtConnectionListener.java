package com.ottochiu.mse.bluetooth_device_manager;

import java.nio.ByteBuffer;

interface IBtConnectionListener {

	void log(final String message);
	void handle(final ByteBuffer data); 
}
