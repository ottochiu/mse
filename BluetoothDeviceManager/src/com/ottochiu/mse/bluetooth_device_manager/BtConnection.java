package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.RemoteException;
import android.util.Log;


class BtConnection {
	private static final String TAG = "BtConnection";
	private static final int HEADER_SIZE = Integer.SIZE / 8;
	
	private final String deviceName;
	private final IBluetoothReadCallback callback;
	private final UUID uuid;
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothSocket socket;
	private BluetoothServerSocket serverSocket;
	
	private boolean isConnected = false;

	BtConnection(
			IBluetoothReadCallback callback,
			String deviceName,
			UUID uuid)
			throws IOException {
		
		this.deviceName = deviceName;
		this.callback = callback;
		this.uuid = uuid;
		
		// open the server socket for listening.
		// Do not make the server socket static.  Otherwise, all accept() will happen in a serial fashion.
		serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(deviceName, uuid);
		
		// Test whether the connection is opened
		try {
			// If available() does not throw IOException, then it socket is connected.
			socket.getInputStream().available();
			isConnected = true;
		} catch (NullPointerException e) {
			// isConnected = false;
		} catch (IOException e) {
			// isConnected = false;
		}
	}
	
	void close() throws IOException {
		Log.i(TAG, "Closing Bluetooth connection");
		socket.close();
		isConnected = false;
	}
	
	void open(int timeout) throws IOException {
		Log.i(TAG, "listening on server");
		
		// Blocks
		socket = serverSocket.accept(timeout);
		serverSocket.close();
		
		isConnected = true;
		
		Log.i(TAG, "Accepted connection");
	}
	
	boolean isConnected() {
		return isConnected;
	}
	
	synchronized void read() throws IOException, RuntimeException {
		final InputStream in = socket.getInputStream();
		
		final ByteBuffer headerBuf = ByteBuffer.allocate(HEADER_SIZE);
		headerBuf.order(ByteOrder.LITTLE_ENDIAN);
		
		// Keep reading until somebody closes the connection.
		while (true) {
			Log.i(TAG, "Reading for " + deviceName);
			
			// First get the number of bytes in the data
			if (in.read(headerBuf.array()) != HEADER_SIZE) {
				throw new RuntimeException("Header corrupted.");
			}
			
			// There are these many bytes in the body.
			int bodySize = headerBuf.getInt(0);
			Log.i(TAG, "Received " + bodySize + " bytes");
			
			// Allocate space for the rest of the items to be delivered to the callback
			ByteBuffer bodyBuf = ByteBuffer.allocate(bodySize);
			bodyBuf.order(ByteOrder.LITTLE_ENDIAN);
			
			if (in.read(bodyBuf.array()) != bodySize) {
				throw new RuntimeException("Body corrupted.");
			}

			// Reset position for user
			bodyBuf.position(0);
			try {
				callback.handle(bodyBuf.array());
			} catch (RemoteException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
	synchronized void write(ByteBuffer buf) throws IOException {
		Log.i(TAG, "Writing to " + deviceName);
		
		final OutputStream out = socket.getOutputStream();
		
		final ByteBuffer headerBuf = ByteBuffer.allocate(HEADER_SIZE);
		headerBuf.order(ByteOrder.LITTLE_ENDIAN);
		
		// limit <= capacity. Always assume buffer is full.
		headerBuf.putInt(buf.limit());
		
		// write the size and then content. Then flush the stream
		out.write(headerBuf.array());
		out.write(buf.array());
		out.flush();
	}
}
