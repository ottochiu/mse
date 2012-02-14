package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothDeviceManagerActivity extends Activity {
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
    
    private void updateStatus(String msg) {
    	mStatus.append(msg + "\n");
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// Check for the correct intent
    	if (requestCode == REQUEST_BT_ENABLE) {
    		if (resultCode == Activity.RESULT_OK) {
    			updateStatus("Bluetooth enabled.");
    			mBtAdapter.setName(getString(R.string.app_name));
    			
    			for (BluetoothDevice device : mBtAdapter.getBondedDevices()) {
    				updateStatus("Bonded device: " + device.getName());
    				
    				// TODO: clean up
    				if (device.getName().equals("Heartbeat Simulator")) {
    					updateStatus("Client found @ " + device.getAddress());
    			
    	        		try {
    	        			new PairedConnectionTask(device).execute();
    	        			return;
    	            	} catch (IOException e) {
    	            		updateStatus("Failed to open paired Bluetooth communication channel: " + e.getMessage());
    	            		mListen.setEnabled(true);
    	            	}
    				}
    			}
    			
        		try {
        			new AcceptConnectionTask().execute();
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
    
    private class PairedConnectionTask extends AsyncTask<Void, String, Void> {
    	private BluetoothSocket mSocket;
    	
    	public PairedConnectionTask(BluetoothDevice device) throws IOException {
//    		mSocket = device.createRfcommSocketToServiceRecord(
//    				UUID.fromString(getString(R.string.uuid)));
    		
    		try {
	    		Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	            mSocket = (BluetoothSocket) m.invoke(device, 1);
    		} catch (IllegalArgumentException e) {
    			
    		} catch (IllegalAccessException e) {
    			
    		} catch (InvocationTargetException e) {
    			
    		} catch (NoSuchMethodException e) {
    			
    		}
		}
    	
    	
		@Override
		protected Void doInBackground(Void... params) {
			publishProgress("Paired async task execute");
			try {
				mBtAdapter.cancelDiscovery();
				mSocket.connect();
				InputStream in = mSocket.getInputStream();
				
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

			} catch (IOException e) {
				publishProgress("Paired execute problem: " + e.getMessage());
			}
			
			return null;
		}


		@Override
		protected void onProgressUpdate(String... s) {
			updateStatus(s[0]);
		}
    }
    
    private class AcceptConnectionTask extends AsyncTask<Void, String, BluetoothSocket> {

    	private final BluetoothServerSocket mServerSocket;
    	
    	public AcceptConnectionTask() throws IOException {
    		mServerSocket = mBtAdapter.listenUsingRfcommWithServiceRecord(
    				getString(R.string.app_name), UUID.fromString(getString(R.string.uuid)));
		}
    	
    	
		@Override
		protected BluetoothSocket doInBackground(Void... params) {
			
			BluetoothSocket socket = null;
			
			try {
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				startActivity(intent);

				publishProgress("listening on server");
				socket = mServerSocket.accept(Integer.parseInt(getString(R.string.connection_timeout)));
				mServerSocket.close();
			} catch (IOException e) {
				publishProgress(e.getMessage());
			}
			
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
				mServerSocket.close();
			} catch (IOException e) {
				Log.i(TAG, "Error closing Bluetooth connection.");
			}
		}
		
    }
}