package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;


class BtConnection {
	private static final String TAG = "BtConnection";
	private final IBtConnectionListener listener;
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private final String deviceName;
	private final UUID uuid;
	private BluetoothSocket socket;
	
	BtConnection(
			IBtConnectionListener listener,
			String deviceName,
			UUID uuid) {
		this.listener = listener;
		this.deviceName = deviceName;
		this.uuid = uuid;
	}
	
	void close() throws IOException {
		socket.close();
	}
	
	void open(int timeout) throws IOException {
		
		// open the server socket for listening.
		// Do not make the server socket.  Otherwise, all accept() will happen in a serial fashion.
		BluetoothServerSocket serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(deviceName, uuid);			
		
		listener.log("listening on server");
		
		// Blocks
		socket = serverSocket.accept(timeout);
		serverSocket.close();
	}
	
	synchronized void read() throws IOException {
		final InputStream in = socket.getInputStream();
		final int headerSize = Integer.SIZE / 8;
		
		final ByteBuffer headerBuf = ByteBuffer.allocate(headerSize);
		
		// Keep reading until somebody closes the connection.
		while (true) {
			
			// First get the number of bytes in the data
			if (in.read(headerBuf.array()) != headerSize) {
				throw new IOException("Header corrupted.");
			}
			
			// There are these many bytes in the body.
			int bodySize = headerBuf.getInt(0);
			listener.log("Received " + bodySize + " bytes");
			
			// Allocate space for the rest of the items to be delivered to the listener
			ByteBuffer bodyBuf = ByteBuffer.allocate(bodySize);
			
			if (in.read(bodyBuf.array()) != bodySize) {
				throw new IOException("Body corrupted.");
			}

			// Reset position for user
			bodyBuf.position(0);
			listener.handle(bodyBuf);
		}
	}
	
	synchronized void write(ByteBuffer buf) throws IOException {
		OutputStream out = socket.getOutputStream();
		
		out.flush();
	}
}
