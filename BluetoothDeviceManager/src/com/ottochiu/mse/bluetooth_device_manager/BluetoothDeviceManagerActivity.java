package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothDeviceManagerActivity extends Activity implements IBtConnectionListener {
	private static final String TAG = "BluetoothDeviceManagerActivity";
	private static final int REQUEST_BT_ENABLE = 1;

	BluetoothAdapter mBtAdapter;
	Button mListen;
	TextView mStatus;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mListen = (Button) findViewById(R.id.listenButton);
        mStatus = (TextView) findViewById(R.id.statusView);
        
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (mBtAdapter == null) {
        	updateStatus("Bluetooth not available on this device.");
        	mListen.setEnabled(false);
        }
    }
    
    public void listen(View v) {
    	updateStatus("Listening for connection");
    	v.setEnabled(false);
    	
    	Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	startActivityForResult(intent, REQUEST_BT_ENABLE);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// Check for the correct intent
    	if (requestCode == REQUEST_BT_ENABLE) {
    		if (resultCode == Activity.RESULT_OK) {
    			updateStatus("Bluetooth enabled.");
    			mBtAdapter.setName(getString(R.string.app_name));
    			
        		try {
        			BtConnection connection = 
        					new BtConnection(this, getString(R.string.app_name), UUID.fromString(getString(R.string.uuid))); 
        			new AcceptConnectionTask(connection).execute();
        			
        			
        			
            	} catch (IOException e) {
            		updateStatus("Failed to open Bluetooth communication channel: " + e.getMessage());
            		mListen.setEnabled(true);
            	}
    		} else {
    			updateStatus("Bluetooth NOT enabled.");
    			mListen.setEnabled(true);
    		}
    	}
    }

    
    public void log(String message) {
    	updateStatus(message);
    }
    
    public void handle(ByteBuffer data) {
    	// TODO
    }
    
    
    
    private void updateStatus(String msg) {
    	mStatus.append(msg + "\n");
    }
    
    private class AcceptConnectionTask extends AsyncTask<Void, String, BluetoothSocket> {

    	private final BtConnection btConnection;
    	
    	public AcceptConnectionTask(BtConnection connection) throws IOException {
    		btConnection = connection;
		}
    	
    	
		@Override
		protected BluetoothSocket doInBackground(Void... params) {

			try {
				btConnection.open(Integer.parseInt(getString(R.string.connection_timeout)));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// TODO:
			BluetoothSocket socket = null;
			return socket;
		}
    	
		@Override
		protected void onPostExecute(BluetoothSocket socket) {
			try {
				updateStatus("Accepted connection from socket: " + socket);
				
				InputStream in = socket.getInputStream();
				ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / 8);

				in.read(buf.array());
				int items = buf.getInt(0);
				updateStatus("Received " + items + " items.");
				
				buf = ByteBuffer.allocate(items * Long.SIZE / 8);
				in.read(buf.array());
				
				LongBuffer longBuf = buf.asLongBuffer();
				longBuf.position(0);
				
				try {
					while (items-- > 0) {
						updateStatus(longBuf.get() + " ms");
					}
				} catch (BufferUnderflowException e) {
					updateStatus("Data packet format error.");
				}
				
				socket.close();
			} catch (NullPointerException e) {
				updateStatus("Bluetooth connection failed.");
			} catch (IOException e) {
				updateStatus("IO Exception");
			}
			
			mListen.setEnabled(true);
		}

		@Override
		protected void onProgressUpdate(String... s) {
			updateStatus(s[0]);
		}
		
		@Override
		protected void onCancelled() {
			try {
				// stop waiting for a connection.
				btConnection.close();
			} catch (IOException e) {
				Log.i(TAG, "Error closing Bluetooth connection.");
			}
		}
		
    }
}