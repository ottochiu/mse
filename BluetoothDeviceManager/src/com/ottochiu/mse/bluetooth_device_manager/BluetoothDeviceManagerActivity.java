package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class BluetoothDeviceManagerActivity extends Activity implements IBtConnectionListener {
	private static final String TAG = "BluetoothDeviceManagerActivity";
	private static final int REQUEST_BT_ENABLE = 1;

	BluetoothAdapter mBtAdapter;
	Button mListen;
	TextView mStatus;
	ScrollView scrollView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mListen = (Button) findViewById(R.id.listenButton);
        mStatus = (TextView) findViewById(R.id.statusView);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (mBtAdapter == null) {
        	updateStatus("Bluetooth not available on this device.");
        	mListen.setEnabled(false);
        }
    }
    
    RegisteredDevices d = new RegisteredDevices(this);
    
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

    
    public void log(final int id, final String message) {
    	updateStatus(message);				
    }
    
    public void handle(final int id, final ByteBuffer data) {
    	// TODO: distribute to appropriate handler
    	
    	// the following really belongs to a specific handler but is here as proof of concept
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
    	
		scrollView.post(new Runnable() {

	        @Override
	        public void run() {
	        	scrollView.fullScroll(ScrollView.FOCUS_DOWN);
	        }
	    });
    }
    
    private class AcceptConnectionTask extends AsyncTask<Void, String, Void> {

    	private final BtConnection connection;
    	
    	AcceptConnectionTask() throws IOException {
    		connection = new BtConnection(
    				BluetoothDeviceManagerActivity.this,
    				0,
    				getString(R.string.app_name),
    				UUID.fromString(getString(R.string.uuid)));  
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				startActivity(intent);

				updateStatus("Listening for connection");
				
				connection.open(Integer.parseInt(getString(R.string.connection_timeout)));
				
				updateStatus("Connection opened");
				
			} catch (IOException e) {
				updateStatus(e.getMessage());
			}
			
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void param) {
			new ReaderTask(connection).execute();
			
			mListen.setEnabled(true);
		}

		@Override
		protected void onCancelled() {
			try {
				// stop waiting for a connection.
				connection.close();
			} catch (IOException e) {
				Log.i(TAG, "Error closing Bluetooth connection.");
			}
		}
		
    }

    private class ReaderTask extends AsyncTask<Void, Void, Void> {
    	private final BtConnection connection;

    	ReaderTask(final BtConnection connection) {
    		this.connection = connection;
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			try {
				connection.read();
				
			} catch (IOException e) {
				updateStatus(e.getMessage());
			} catch (RuntimeException e) {
				updateStatus(e.getMessage());
			}
			
			return null;
		}
		
		@Override
		protected void onCancelled() {
			try {
				connection.close();
			} catch (IOException e) {
				updateStatus("Error closing Bluetooth connection: " + e.getMessage());
			}
		}
    	
    }
    
    
}