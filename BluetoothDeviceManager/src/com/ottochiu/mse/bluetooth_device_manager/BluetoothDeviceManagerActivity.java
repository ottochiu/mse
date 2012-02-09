package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothDeviceManagerActivity extends Activity {
	private static final String TAG = "BluetoothDeviceManagerActivity";

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
    	
    	try {
    		new AcceptConnectionTask().execute();
    	} catch (IOException e) {
    		updateStatus("Failed to open Bluetooth communication channel.");
    	}
    }
    
    private void updateStatus(String msg) {
    	mStatus.append(msg + "\n");
    }
    
    private class AcceptConnectionTask extends AsyncTask<Void, Void, BluetoothSocket> {

    	private final BluetoothServerSocket mServerSocket;
    	
    	public AcceptConnectionTask() throws IOException {
    		mServerSocket = mBtAdapter.listenUsingInsecureRfcommWithServiceRecord(
    				getString(R.string.app_name), UUID.fromString(getString(R.string.uuid)));
		}
    	
    	
		@Override
		protected BluetoothSocket doInBackground(Void... params) {
			
			BluetoothSocket socket = null;
			
			while (true) {
				try {
					
					socket = mServerSocket.accept(Integer.parseInt(getString(R.string.connection_timeout)));
					mServerSocket.close();
				} catch (IOException e) {
					Log.i(TAG, "#####" + e.getMessage());
					break;
				}
			}
			
			return socket;
		}
    	
		@Override
		protected void onPostExecute(BluetoothSocket socket) {
			try {
				updateStatus("Accepted connection from socket: " + socket);
			} catch (NullPointerException e) {
				updateStatus("Bluetooth connection failed.");
			}
			
			mListen.setEnabled(true);
		}
		
    }
}