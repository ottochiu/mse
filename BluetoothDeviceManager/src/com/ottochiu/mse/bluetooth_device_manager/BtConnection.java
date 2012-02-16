package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


class BtConnection {
	private static final String TAG = "BtConnection";
	private final IBtConnectionListener listener;
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothSocket socket;
	private BluetoothServerSocket serverSocket;
	
	BtConnection(IBtConnectionListener listener, String deviceName, UUID uuid)
			throws IOException {
		this.listener = listener;
		
		// open the server socket for listening.
		// Do not make the server socket.  Otherwise, all accept() will happen in a serial fashion.
		serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(deviceName, uuid);
	}
	
	void close() throws IOException {
		Log.i(TAG, "Closing Bluetooth connection");
		socket.close();
	}
	
	void open(int timeout) throws IOException {
		listener.log("listening on server");
		
		// Blocks
		socket = serverSocket.accept(timeout);
		serverSocket.close();
		
		listener.log("Accepted connection");
	}
	
	synchronized void read() throws IOException, RuntimeException {
		final InputStream in = socket.getInputStream();
		final int headerSize = Integer.SIZE / 8;
		
		final ByteBuffer headerBuf = ByteBuffer.allocate(headerSize);
		
		// Keep reading until somebody closes the connection.
		while (true) {
			listener.log("Reading");
			
			// First get the number of bytes in the data
			if (in.read(headerBuf.array()) != headerSize) {
				throw new RuntimeException("Header corrupted.");
			}
			
			// There are these many bytes in the body.
			int bodySize = headerBuf.getInt(0);
			listener.log("Received " + bodySize + " bytes");
			
			// Allocate space for the rest of the items to be delivered to the listener
			ByteBuffer bodyBuf = ByteBuffer.allocate(bodySize);
			
			if (in.read(bodyBuf.array()) != bodySize) {
				throw new RuntimeException("Body corrupted.");
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
