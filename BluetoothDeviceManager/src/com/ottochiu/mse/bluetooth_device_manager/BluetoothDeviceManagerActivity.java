package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;

public class BluetoothDeviceManagerActivity extends Activity {
	private static final String TAG = "BluetoothDeviceManagerActivity";
	private static final int REQUEST_BT_ENABLE = 1;

	BluetoothAdapter btAdapter;
//	Button mListen;
//	TextView mStatus;
//	ScrollView scrollView;
	RadioGroup btControl;
	TableLayout connectionTable;
	TextView btStatus;
	TextView status;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btControl = (RadioGroup) findViewById(R.id.bluetoothGroup);
        connectionTable = (TableLayout) findViewById(R.id.connectionTable);
        btStatus = (TextView) findViewById(R.id.btStatus);
        status = (TextView) findViewById(R.id.status);
        
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (btAdapter == null) {
        	updateStatus("Bluetooth not available on this device.");
        	btControl.check(R.id.bluetoothOff);
        	
        	btControl.getChildAt(0).setEnabled(false);
        	btControl.getChildAt(1).setEnabled(false);
        	btStatus.setText("Bluetooth is unavailable");
        }
        else {
        	
        	if (btAdapter.isEnabled()) {
        		btControl.check(R.id.bluetoothOn);
        		btStatus.setText("Bluetooth is ON");
        	} else {
        		btControl.check(R.id.bluetoothOff);
        		btStatus.setText("Bluetooth is OFF");        		
        	}
        	
        	new StartBluetoothService().execute();
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	unbindService(connection);
    	unregisterReceiver(broadcastReceiver);
    }
    
    public void setBtStatus(View v) {
    	bluetoothService.setEnable(btControl.getCheckedRadioButtonId() == R.id.bluetoothOn);
    }
    
    
    
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothService.ACTION_LOG)) {
                String msg = intent.getStringExtra(BluetoothService.EXTRA_MESSAGE);
                
                updateStatus(msg);
                
			} else if (intent.getAction().equals(BluetoothService.ACTION_BT_STATUS)) {
                String msg = intent.getStringExtra(BluetoothService.EXTRA_BT_STATUS);
                
                btStatus.setText(msg);
			}
		}
	};
    
    
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// Check for the correct intent
    	if (requestCode == REQUEST_BT_ENABLE) {
    		if (resultCode == Activity.RESULT_OK) {
    			updateStatus("Bluetooth enabled.");
    			btAdapter.setName(getString(R.string.app_name));

//    			try {
//					new AcceptConnectionTask().execute();
//				} catch (IOException e) {
//            		updateStatus("Failed to open Bluetooth communication channel: " + e.getMessage());
//            		mListen.setEnabled(true);
//				}
    			
    		} else {
    			updateStatus("Bluetooth NOT enabled.");
    			btControl.check(R.id.bluetoothOff);
//    			mListen.setEnabled(true);
    		}
    	}
    }
    
    
    private void updateStatus(final String msg) {
    	Log.i(TAG, msg);
    	runOnUiThread(new Runnable() {

			@Override
			public void run() {
				status.setText(msg + "\n");
			}
    	});
    	
//		scrollView.post(new Runnable() {

//	        @Override
//	        public void run() {
//	        	scrollView.fullScroll(ScrollView.FOCUS_DOWN);
//	        }
//	    });
    }
    
    //////////////// BluetoothService /////////////////
	private BluetoothService bluetoothService;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bluetoothService = ((BluetoothService.BtBinder) service).getService();

			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothService.ACTION_LOG);
			filter.addAction(BluetoothService.ACTION_BT_STATUS);
			
	        registerReceiver(broadcastReceiver, filter);
			
			Log.i(TAG, "Bluetooth service connected.");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bluetoothService = null;
			Log.i(TAG, "Bluetooth service disconnected.");
		}
	};
    
    
    class StartBluetoothService extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {

			Intent intent = new Intent(BluetoothDeviceManagerActivity.this, BluetoothService.class); 
			startService(intent);
			
			boolean isBounded = bindService(intent, 
	        		connection, Context.BIND_AUTO_CREATE);
		
			Log.i(TAG, "Bluetooth service started: " + isBounded);
			
			return Boolean.valueOf(isBounded);
		}
    	
		@Override
		protected void onPostExecute(Boolean b) {
			// Succeeded
			if (b.booleanValue()) {
				Log.i(TAG, "Bluetooth service bounded.");
			} else {
				Log.e(TAG, "Bluetooth service failed to bind.");
			}
		}
    }
    
    
    
    
    // TODO move to activity for connecting bluetooth devices
    // only allow connection for registered plugins
    private class AcceptConnectionTask extends AsyncTask<Void, String, Void> {

    	private final BtConnection connection;
    	
    	AcceptConnectionTask() throws IOException {
    		connection = new BtConnection(
    				null,
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
			
//			mListen.setEnabled(true);
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