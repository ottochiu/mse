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
    			
//    			BtConnection connection = 
//    					new BtConnection(this, getString(R.string.app_name), UUID.fromString(getString(R.string.uuid))); 
//    			new AcceptConnectionTask(connection).execute();
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

    
    public void log(final String message) {
    	updateStatus(message);				
    }
    
    public void handle(final ByteBuffer data) {
    	// TODO: distribute to appropriate handler
    	
    	runOnUiThread(new Runnable() {
    	
    		@Override
    		public void run() {
    			final LongBuffer buf = data.asLongBuffer();

    			updateStatus("Handling data");
    			
    			try {
    				while (true) {
    					updateStatus(buf.get() + " ms");
    				}

    			} catch (BufferUnderflowException e) {
    				// not an error
    			}
    		}
    	});
    }
    
    
    
    private void updateStatus(final String msg) {
    	runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mStatus.append(msg + "\n");
			}
    	});
    }
    
//    private class AcceptConnectionTask extends AsyncTask<Void, String, Boolean> {
//
//    	private final BtConnection btConnection;
//    	
//    	AcceptConnectionTask(BtConnection connection) {
//    		btConnection = connection;
//		}
//    	
//		@Override
//		protected Boolean doInBackground(Void... params) {
//
//			try {
//				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//				startActivity(intent);
//				
//				btConnection.open(Integer.parseInt(getString(R.string.connection_timeout)));
//				
//				return Boolean.TRUE;
//				
//			} catch (NumberFormatException e) {
//				updateStatus(e.getMessage());
//			} catch (IOException e) {
//				updateStatus(e.getMessage());
//			}
//
//			return Boolean.FALSE;
//		}
//    	
//		@Override
//		protected void onPostExecute(Boolean status) {
//			mListen.setEnabled(true);
//
//			if (status.booleanValue()) {
//				new ReaderTask(btConnection).execute();
//			} else {
//				updateStatus("Failed to establish Bluetooth connection.");
//			}
//		}
//
//		@Override
//		protected void onCancelled() {
//			try {
//				// stop waiting for a connection.
//				btConnection.close();
//			} catch (IOException e) {
//				Log.i(TAG, "Error closing Bluetooth connection.");
//			}
//		}
//		
//    }
    
    
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
			new someTask(socket).execute();
			
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

    private class someTask extends AsyncTask<Void, Void, Void> {
    	BluetoothSocket socket;

    	someTask(BluetoothSocket socket) {
    		this.socket = socket;
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			try {
				updateStatus("Accepted connection from socket: " + socket);
				
				InputStream in = socket.getInputStream();
				
				while (!isCancelled()) {
					ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE / 8);

					in.read(buf.array());
					int numBytes = buf.getInt(0);
					updateStatus("Received " + numBytes + " bytes.");

					buf = ByteBuffer.allocate(numBytes);
					in.read(buf.array());

					LongBuffer longBuf = buf.asLongBuffer();
					longBuf.position(0);

					try {
						while (true) {
							updateStatus(longBuf.get() + " ms");
						}
					} catch (BufferUnderflowException e) {
					}
				}
			} catch (NullPointerException e) {
				updateStatus("Bluetooth connection failed.");
			} catch (IOException e) {
				updateStatus("IO Exception");
			}
			
			try {
				socket.close();
			} catch (IOException e) {
				updateStatus("Error closing Bluetooth connection: " + e.getMessage());				
			}
			
			return null;
		}
		
		@Override
		protected void onCancelled() {
			try {
				socket.close();
			} catch (IOException e) {
				updateStatus("Error closing Bluetooth connection: " + e.getMessage());
			}
		}
    	
    }
    
    
    
    
    // ReaderTask
    private class ReaderTask extends AsyncTask<Void, Void, Void> {
    	private final BtConnection btConnection;
    	
    	ReaderTask(final BtConnection connection) {
    		btConnection = connection;
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {

			try {
				btConnection.read();
			} catch (IOException e) {
			} catch (RuntimeException e) {
				updateStatus("Error reading Bluetooth data: " + e.getMessage());
			}
			
			return null;
		}
		
		@Override
		protected void onCancelled() {
			try {
				btConnection.close();
			} catch (IOException e) {
				updateStatus("Error closing Bluetooth connection: " + e.getMessage());
			}
		}
    }
    
}