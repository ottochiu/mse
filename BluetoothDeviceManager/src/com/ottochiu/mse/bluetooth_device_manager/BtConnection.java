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
import android.util.Log;


class BtConnection {
	private static final String TAG = "BtConnection";
	private static final int HEADER_SIZE = Integer.SIZE / 8;
	private final IBtConnectionListener listener;
	private final int id;
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothSocket socket;
	private BluetoothServerSocket serverSocket;
	

	BtConnection(
			IBtConnectionListener listener,
			int id, // the unique id of this connection
			String deviceName,
			UUID uuid)
			throws IOException {
		this.listener = listener;
		this.id = id;
		
		// open the server socket for listening.
		// Do not make the server socket static.  Otherwise, all accept() will happen in a serial fashion.
		serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(deviceName, uuid);
	}
	
	void close() throws IOException {
		Log.i(TAG, "Closing Bluetooth connection");
		socket.close();
	}
	
	void open(int timeout) throws IOException {
		listener.log(id, "listening on server");
		
		// Blocks
		socket = serverSocket.accept(timeout);
		serverSocket.close();
		
		listener.log(id, "Accepted connection");
	}
	
	synchronized void read() throws IOException, RuntimeException {
		final InputStream in = socket.getInputStream();
		
		final ByteBuffer headerBuf = ByteBuffer.allocate(HEADER_SIZE);
		headerBuf.order(ByteOrder.LITTLE_ENDIAN);
		
		// Keep reading until somebody closes the connection.
		while (true) {
			listener.log(id, "Reading");
			
			// First get the number of bytes in the data
			if (in.read(headerBuf.array()) != HEADER_SIZE) {
				throw new RuntimeException("Header corrupted.");
			}
			
			// There are these many bytes in the body.
			int bodySize = headerBuf.getInt(0);
			listener.log(id, "Received " + bodySize + " bytes");
			
			// Allocate space for the rest of the items to be delivered to the listener
			ByteBuffer bodyBuf = ByteBuffer.allocate(bodySize);
			bodyBuf.order(ByteOrder.LITTLE_ENDIAN);
			
			if (in.read(bodyBuf.array()) != bodySize) {
				throw new RuntimeException("Body corrupted.");
			}

			// Reset position for user
			bodyBuf.position(0);
			listener.handle(id, bodyBuf);
		}
	}
	
	synchronized void write(ByteBuffer buf) throws IOException {
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
