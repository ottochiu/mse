package com.ottochiu.mse.pulse_oximeter_sim;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ConnectionService extends Service {
	public final static String STATE_CHANGED = "com.ottochiu.mse.pulse_oximeter_sim.STATE_CHANGED";
	public final static String STATE_EXTRA = "com.ottochiu.mse.pulse_oximeter_sim.STATE_EXTRA";

	public final static int STATE_OFFLINE = 0;
	public final static int STATE_CONNECTING = 1;
	public final static int STATE_CONNECTED = 2;
	public final static int STATE_DISCONNECTING = 3;
	public final static int STATE_DISCOVERING = 4;

	private final static String TAG = "ConnectionService";

	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private final IBinder binder = new ConnectionServiceBinder();
	
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    
    private int state = STATE_OFFLINE;
    private int level;
    
    private long period;

    private void changeState(int newState) {
		
		// Close socket if currently connected.
		if (state == STATE_CONNECTED) {
			try {
				btSocket.close();
				timer.cancel();
			} catch (IOException e) {
				Log.e(TAG, "Error closing socket: " + e.getMessage());
			}
		}

		state = newState;
		
		// Reset all connections
		if (newState == STATE_CONNECTED) {
			Log.i(TAG, "Connected");
			timer = new Timer();
			timer.scheduleAtFixedRate(
					new TimerTask() {

						@Override
						public void run() {
							if (state == STATE_CONNECTED) {
								Log.i(TAG, "Sending level: " + level);

								try {
									// Message format is:
									// Message size (4 bytes)
									// SpO2 level (1 byte) - since the value can be at most 100, which fits in a byte.
									final int INT_SIZE = Integer.SIZE / 8;

									// cap level at 100
									if (level > 100) {
										Log.wtf(TAG,  "SpO2 level > 100");
										level = 100;
									}

									ByteBuffer buf = ByteBuffer.allocate(INT_SIZE + 1);
									buf.order(ByteOrder.LITTLE_ENDIAN);

									buf.putInt(1); // only 1 byte for the level
									buf.put((byte) level);

									btSocket.getOutputStream().write(buf.array());
								} catch (IOException e) {
									Log.e(TAG, "Error writing: " + e.getMessage());
									changeState(STATE_OFFLINE);
								}
							}
						}
					},
					period,
					period);
		} else {
			btDevice = null;
			btSocket = null;
		}
		
		Intent intent = new Intent(STATE_CHANGED);
		intent.putExtra(STATE_EXTRA, state);
		sendBroadcast(intent);
    }
    
    // Bluetooth state update
    private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
					
			// Bluetooth state changes
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR); 
				
				switch (btState) {
				case BluetoothAdapter.STATE_ON:
					if (state == STATE_CONNECTING) {
						// after Bluetooth is ON, connect to manager
						Log.i(TAG, "BT turned on");
						connectToManager();
					}
					
					break;
				case BluetoothAdapter.STATE_OFF:
					Log.i(TAG, "BT turned off");
					changeState(STATE_OFFLINE);
					break;
					
				case BluetoothAdapter.ERROR:
					Log.wtf(TAG, "Bluetooth error!");
					break;
					
				default:
					// do nothing
				}
				
			} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				
				if (state == STATE_CONNECTING) {
					// Found a device. Check and see if it is the right one.

					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					Log.i(TAG, "Discovered " + device.getName() + " @ " + device.getAddress());

					if (device.getName().equals(getString(R.string.server_name))) {
						Log.i(TAG, "Server found.");
						
						btDevice = btAdapter.getRemoteDevice(device.getAddress());
						
						btAdapter.cancelDiscovery();
						startConnection();
					}
				}
				
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				// Bluetooth discovery completed.
				Log.i(TAG, "Bluetooth discovery finished");
				
				if (state == STATE_CONNECTING) {
					// Try to discover manager
		        	changeState(STATE_DISCOVERING);
		    		btAdapter.startDiscovery();
					
				} else if ((state == STATE_DISCOVERING) && (btDevice == null)) {
					Log.i(TAG, "Failed to discover Manager");
					
					// Failed
					changeState(STATE_OFFLINE);
				}
			}
		}
	};

	///// Timer sends current SpO2 level periodically once connected
	private Timer timer;
	

    
	public class ConnectionServiceBinder extends Binder {
		ConnectionService getService() {
			return ConnectionService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "Binded");
		
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Started");
		
		period = Integer.parseInt(getString(R.string.period));
		
		// Register the BroadcastReceiver.
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
		
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
		
		return START_STICKY;
	}

	
	int getState() {
		return state;
	}
	
	void connect() {
		if (state == STATE_OFFLINE) {
			changeState(STATE_CONNECTING);
			
			
			// enable() is a non-blocking call
			if (btAdapter.isEnabled()) {
				Log.i(TAG, "Bluetooth adapter already enabled");
				connectToManager();
				
			} else if (!btAdapter.enable()) { // status update comes as broadcast
				Log.i(TAG, "Cannot enable Bluetooth");
				changeState(STATE_OFFLINE); 
			}
		}
	}
	
	void disconnect() {
		if (state == STATE_CONNECTED) {
			changeState(STATE_DISCONNECTING);
			
			if (btSocket != null) {
				try {
					btSocket.close();
				} catch (IOException e) {
					Log.e(TAG, "Cannot close BT socket: " + e.getMessage());
				}
			}
				
			changeState(STATE_OFFLINE);
		}
	}
	
	void write(int level) {
		this.level = level;
	}
	
	
	////////// Perform Bluetooth connection
	class ConnectionTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				Log.i(TAG, "Connecting to BT device");
				btSocket = btDevice.createRfcommSocketToServiceRecord(
						UUID.fromString(getString(R.string.uuid)));
			
				btSocket.connect();
				
				return Boolean.TRUE;
				
			} catch (IOException e) {
				Log.e(TAG, "Connection exception: " + e.getMessage());
			}

			return Boolean.FALSE;
		}
		
		@Override
		protected void onPostExecute(Boolean status) {
			if (status.booleanValue()) {
				changeState(STATE_CONNECTED);
				
			} else {
				changeState(STATE_OFFLINE);
			}
		}
	}
	
	//////////// Connect to manager via Bluetooth
	void connectToManager() {
		// non-blocking calls. Don't need async task
		btAdapter.setName(getString(R.string.app_name));
		
    	// First check if the server is connected already.
    	for (BluetoothDevice device : btAdapter.getBondedDevices()) {
    		Log.i(TAG, "Bonded device: " + device.getName());
    		
    		if (device.getName().equals(getString(R.string.server_name))) {
    			Log.i(TAG, "Server found @ " + device.getAddress());
    			btDevice = btAdapter.getRemoteDevice(device.getAddress());
    			startConnection();
    			return;
    		}
    	}
    	
    	// server has not been paired before. attempt to discover it.
    	Log.i(TAG, "Discovering server.");

    	// Restart discovery.
    	if (btAdapter.isDiscovering()) {
    		btAdapter.cancelDiscovery();
    	} else {
    		changeState(STATE_DISCOVERING);
    		btAdapter.startDiscovery();
    	}
	}

	// Got a BT device. Try to open a socket
	void startConnection() {
		Log.i(TAG, "Opening socket");
		
		new ConnectionTask().execute();
	}
}
