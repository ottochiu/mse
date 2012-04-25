package com.ottochiu.mse.bluetooth_device_manager;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;
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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class BluetoothDeviceManagerActivity extends Activity {
	private static final String TAG = "BluetoothDeviceManagerActivity";

	RadioGroup btControl;
	TableLayout connectionTable;
	TextView btStatus;
	TextView status;
	
	// device name -> tr
	Hashtable<String, TableRow> rowHash = new Hashtable<String, TableRow>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btControl = (RadioGroup) findViewById(R.id.bluetoothGroup);
        connectionTable = (TableLayout) findViewById(R.id.connectionTable);
        btStatus = (TextView) findViewById(R.id.btStatus);
        status = (TextView) findViewById(R.id.status);
        
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (btAdapter == null) {
        	updateStatus("Bluetooth not available on this device.");
        	btControl.check(R.id.bluetoothOff);
        	
        	btControl.getChildAt(0).setEnabled(false);
        	btControl.getChildAt(1).setEnabled(false);
        	btStatus.setText("Bluetooth is unavailable");
        }
        else {
        	
        	new StartBluetoothService().execute();
        	
        	if (btAdapter.isEnabled()) {
        		btControl.check(R.id.bluetoothOn);
        		btStatus.setText("Bluetooth is ON");
        	} else {
        		btControl.check(R.id.bluetoothOff);
        		btStatus.setText("Bluetooth is OFF");        		
        	}

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

    	private void enableButtons(boolean enable) {
        	for (int i = 0; i < connectionTable.getChildCount(); i++) {
        		try {
        			TableRow row = (TableRow) connectionTable.getChildAt(i);
        			row.getChildAt(2).setEnabled(enable);
        		} catch (ClassCastException e) {}
        	}
    	}
    	
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (action.equals(BluetoothService.ACTION_LOG)) {
                String msg = intent.getStringExtra(BluetoothService.EXTRA_MESSAGE);
                
                updateStatus(msg);
                
			} else if (action.equals(BluetoothService.ACTION_BT_STATUS)) {
                int status = intent.getIntExtra(BluetoothService.EXTRA_BT_STATUS, BluetoothAdapter.ERROR);
                
				String connectionStatus = "Bluetooth is ";
                switch (status) {
                case BluetoothAdapter.STATE_OFF:
                	connectionStatus += "OFF";
                	enableButtons(false);
                	
                	break;
                case BluetoothAdapter.STATE_ON:
                	connectionStatus += "ON";
                	enableButtons(true);
                	
                	break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                	connectionStatus += "TURNING OFF";
                	enableButtons(false);
                	
                	break;
                case BluetoothAdapter.STATE_TURNING_ON:
                	connectionStatus += "TURNING ON";
                	break;
                default:
                	connectionStatus = "Bluetooth error";
                	break;
                }
                
                updateStatus(connectionStatus);
                btStatus.setText(connectionStatus);
                
			} else if (action.equals(BluetoothService.ACTION_REGISTERED_NEW_DEVICE)) {
				updateDeviceList();
			}
		}
	};
	
	private void updateConnectionStatus(BtConnection conn) {
		TableRow tr = rowHash.get(conn.getName());
		
		TextView tv = (TextView) tr.getChildAt(1);
		tv.setText(conn.isConnected() ? "Connected" : "Disconnected");
		
		Button button = (Button) tr.getChildAt(2);
		button.setText(conn.isConnected() ? "Disconnect" : "Connect");
	}

	private void updateDeviceList() {
    	Hashtable<String, BtConnection> connections = 
    			bluetoothService.getBtConnections();

    	Log.i(TAG, String.valueOf(connections.size()) + " registered devices");
    	
    	// Enumerate and display the registered devices
    	for (Entry<String, BtConnection> entry : connections.entrySet()) {

    		String entryName = entry.getKey();
    		String[] names = entryName.split(",");
    		final BtConnection btConn = entry.getValue();
    		
    		TableRow tr = rowHash.get(entryName);
    		
    		// Never been displayed before
    		if (tr == null) {
        		Log.i(TAG, "Adding new row: " + names[1]);
        		
        		tr = new TableRow(this);
        		tr.setLayoutParams(new LayoutParams(
        				LayoutParams.FILL_PARENT,
        				LayoutParams.WRAP_CONTENT));

        		TextView tv = new TextView(this);
        		tv.setText(names[1]);
        		tr.addView(tv);
        		
        		tv = new TextView(this);
        		tv.setGravity(Gravity.CENTER);
        		tr.addView(tv);
        		
        		Button button = new Button(this);
        		button.setEnabled(BluetoothAdapter.getDefaultAdapter().isEnabled());
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                    	Button b = (Button) v;
                    	BtConnection conn = btConn;
                    	
                		try {
                			
                			if (b.getText().toString().equals("Connect")) {

                				try {
                					new AcceptConnectionTask(conn).execute();
                				} catch (NumberFormatException e) {
                					Log.wtf(TAG,  e.getMessage());
                					e.printStackTrace();
                				}

                			} else {
                				conn.close();
                				updateConnectionStatus(conn);
                			}
                    		
						} catch (IOException e) {
							Log.i(TAG, e.getLocalizedMessage());							
						}
                    }
                });

        		tr.addView(button);

        		connectionTable.addView(tr, new TableLayout.LayoutParams(
        				LayoutParams.FILL_PARENT,
        				LayoutParams.WRAP_CONTENT));
        		rowHash.put(entryName, tr);

        		updateConnectionStatus(btConn);
        		
    		} else {
    			
    			// Been displayed
    			Log.i(TAG, "Updating displayed row");
    			
    			TextView tv = (TextView) tr.getChildAt(0);
    			tv.setText(names[1]);
    			
    			updateConnectionStatus(btConn);
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
			Log.i(TAG, "Bluetooth service connected.");

			bluetoothService = ((BluetoothService.BtBinder) service).getService();

			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothService.ACTION_LOG);
			filter.addAction(BluetoothService.ACTION_BT_STATUS);
			filter.addAction(BluetoothService.ACTION_REGISTERED_NEW_DEVICE);
			
	        registerReceiver(broadcastReceiver, filter);

    		updateDeviceList();
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
    
    
    
    
    private class AcceptConnectionTask extends AsyncTask<Void, String, Boolean> {

    	private final BtConnection connection;
    	
    	AcceptConnectionTask(final BtConnection conn) {
    		connection = conn;  
    	}
    	
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				startActivity(intent);

				updateStatus("Listening for connection");
				
				connection.open(Integer.parseInt(getString(R.string.connection_timeout)));
				
				updateStatus("Connection opened");
				return Boolean.TRUE;
				
			} catch (IOException e) {
				updateStatus(e.getMessage());
				return Boolean.FALSE;
			}
		}
    	
		@Override
		protected void onPostExecute(Boolean param) {
			if (param.booleanValue()) {
				Log.i(TAG, "Starting reader task");
				
				updateConnectionStatus(connection);
				
				new ReaderTask(connection).execute();
			}
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