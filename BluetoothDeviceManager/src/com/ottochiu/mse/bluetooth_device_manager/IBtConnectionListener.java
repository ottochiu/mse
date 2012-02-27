package com.ottochiu.mse.bluetooth_device_manager;

import java.nio.ByteBuffer;

interface IBtConnectionListener {

	void log(final int id, final String message);
	void handle(final int id, final ByteBuffer data); 
}
