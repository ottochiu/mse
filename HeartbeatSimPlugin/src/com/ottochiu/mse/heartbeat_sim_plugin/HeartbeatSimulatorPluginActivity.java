package com.ottochiu.mse.heartbeat_sim_plugin;

import android.app.Activity;
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
import android.widget.TextView;

public class HeartbeatSimulatorPluginActivity extends Activity {
	private static final String TAG = "HeartbeatSimulatorPluginActivity";
	private TextView status;
	private Button registerButton;
	
	private BroadcastReceiver connectionServiceReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ConnectionService.STATUS_UPDATE)) {
				updateStatus(intent.getStringExtra(ConnectionService.STATUS_MESSAGE));
			}
		}
	};
	
	private ConnectionService connectionService;
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			connectionService = ((ConnectionService.ConnectionServiceBinder) service).getService();
			
	        IntentFilter filter = new IntentFilter(ConnectionService.STATUS_UPDATE);
	        registerReceiver(connectionServiceReceiver, filter);
			
			status.setText(connectionService.getStatus());
			registerButton.setVisibility(View.VISIBLE);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
	        unregisterReceiver(connectionServiceReceiver);
			connectionService = null;
		}
	};
	
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        status = (TextView) findViewById(R.id.status);
        registerButton = (Button) findViewById(R.id.registerButton);
        
        new StartConnectionService().execute();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (connectionService != null) {
    		unregisterReceiver(connectionServiceReceiver);
    		unbindService(connection);
    	}
    }
    
    
    public void registerPlugin(View v) {
    	
    	// This can only be called when the user clicks on the register button
    	// and the register button is only visible when a connection has been 
    	// binded with ConnectionService. However, null is checked just to be safe.
    	Log.i(TAG, "registering");
    }
    
    private void updateStatus(final String msg) {
    	runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, msg);
				
				String message = msg + "\n";
				
				if (connectionService != null) {
					connectionService.appendStatus(message);
				}
				
				status.append(message);
			}
    		
    	});
    }

    private class StartConnectionService extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {

			Intent intent = new Intent(HeartbeatSimulatorPluginActivity.this, ConnectionService.class); 
			startService(intent);
			
			boolean isBounded = bindService(intent, 
	        		connection, Context.BIND_AUTO_CREATE);
		
			Log.i(TAG, "Connection service started: " + isBounded);
			
			return Boolean.valueOf(isBounded);
		}
		
		@Override
		protected void onPostExecute(Boolean b) {
			// Succeeded
			if (b.booleanValue()) {
				Log.i(TAG, "Service for plugin registered.");
			} else {
				Log.e(TAG, "Service for plugin failed to start.");
			}
		}
   	
    }
    

}