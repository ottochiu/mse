package com.ottochiu.mse.heartbeat_simulator;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HeartbeatSimulatorActivity extends Activity {
	
	// The ON/OFF toggle
	ToggleButton mToggle;
	
	// The Beat button
	Button mBeat;
	
	// The info window
	TextView mIntervalDisplay;
	
	// The list of intervals
	List<Long> mIntervals = new LinkedList<Long>();;
	
	// The text "Session started"
	TextView mSessionStarted;
	
	// The start time of the session
	TextView mStartTime;
	
	// Scroll view
	ScrollView mScrollView;
	
	// Time of previous beat
	long mBeatTime;

	static DataSender mSender;

	private static final int REQUEST_ENABLE_BT = 1;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mToggle = (ToggleButton) findViewById(R.id.beatToggle);
        mBeat = (Button) findViewById(R.id.beatButton);
        mIntervalDisplay = (TextView) findViewById(R.id.intervalDisplay);
        mSessionStarted = (TextView) findViewById(R.id.sessionStarted);
        mStartTime = (TextView) findViewById(R.id.startTime);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        // Avoid repeated BT permissions request once the user has decided.
        if (mSender == null) {
        	detectBluetooth();
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// Check for the correct intent
    	if (requestCode == REQUEST_ENABLE_BT) {
    		if (resultCode == Activity.RESULT_OK) {
    			createBluetoothSender();
    		} else {
    			createHttpSender();
    		}
    	}
    }
    
    
    /// Handles the ON/OFF toggle button
    public void toggleUpdate(View v) {

    	// if user is enabling beat
    	if (mToggle.isChecked()) {
    		// steps involved:
    		// initializing all data values
        	mBeatTime = 0;
        	mIntervalDisplay.setText("");
        	mIntervals.clear();
        	mSessionStarted.setVisibility(View.VISIBLE);
        	mStartTime.setText(new Timestamp(System.currentTimeMillis()).toString());
        	setRequestedOrientation(
        			getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
        					ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        	
    	} else {
    		// stopping the beat means: send intervals to server
        	sendIntervals();
    	}
    	
    	// enable/disable the beat button.
    	mBeat.setVisibility(mToggle.isChecked() ? View.VISIBLE : View.INVISIBLE);
    }
    
    /// Handles the Beat button
    public void beat(View v) {
    	
    	// use current as the current time. Calling currentTimeMillis() again
    	// may result in a new time.
    	long current = System.currentTimeMillis();
    	
    	if (mBeatTime != 0) {
    		long interval = current - mBeatTime;
    		mIntervalDisplay.append(String.format("%5d ms%n", interval));
    		mIntervals.add(new Long(interval));
    		
    		mScrollView.post(new Runnable() {

    	        @Override
    	        public void run() {
    	        	mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    	        }
    	    });
    	}
    	
    	mBeatTime = current;
    	v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }
    
    public void detectBluetooth() {
    	// Determine whether Bluetooth capability is enabled
    	try {
    		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {

    			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    			startActivityForResult(enableBtIntent, 	REQUEST_ENABLE_BT);

    		} else {
    			createBluetoothSender();
    		}

    	} catch (NullPointerException e) {
    		createHttpSender();
    	}
    }
    
    /// Sends interval data and clear the intervals list.
    private void sendIntervals() {
    	
    	// send only when there is >1 interval
		mToggle.setEnabled(false);
		
		mSessionStarted.setVisibility(View.GONE);
		
		String startTime = mStartTime.getText().toString();
		mStartTime.setText("Sending data...");

		mSender.sendInBackground(startTime);
    }
    
    private void createHttpSender() {
    	mSender = new HttpDataSender(new DefaultHttpClient(), new HttpPost(getString(R.string.url)));
    }
    
    private void createBluetoothSender() {
    	mSender = new BluetoothDataSender();
    }
    
    
    /////////////////////////////////
    // Data Sender

    private abstract class DataSender extends AsyncTask<String, Void, String> {

    	// data[0] = timestamp
    	// data[1] = Intervals formatted in the same way as they appear in the TextView box.
    	protected String doInBackground(String... strings) {
    		// mIntervals is safe because the toggle is disabled and therefore cannot be emptied.
    		return send(strings[0], mIntervals);
        }

        protected void onPostExecute(String result) {
        	mToggle.setEnabled(true);
        	mStartTime.setText(result);
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        abstract void sendInBackground(String timestamp);
    	abstract protected String send(String timestamp, List<Long> intervals);
    }    	


    // Send data via HTTP POST method
    class HttpDataSender extends DataSender {

    	HttpClient mClient;
    	HttpPost mPost;
    	
    	HttpDataSender(HttpClient client, HttpPost post) {
    		this.mClient = client;
    		this.mPost = post;
    	}
    	
    	@Override
    	protected void sendInBackground(String timestamp) {
    		// a convoluated way of starting an async task. In order to call this method there needs to
    		// be an existing instance of HttpDataSender already. This instance is created by the activity
    		// when it determines whether to send using Bluetooth or HTTP.
    		new HttpDataSender(mClient, mPost).execute(timestamp);
    	}
        
    	@Override
    	protected String send(String timestamp, List<Long> intervals) {
    	    try {
    	        // Construct POST request
    	        String joinedIntervals = "";
    	        
    	        for (Long vti : intervals) {
    	        	joinedIntervals += Long.toString(vti) + ",";
    	        }
    	        
    	        joinedIntervals = joinedIntervals.substring(0, joinedIntervals.length()-1); // can throw IndexOutOfBoundsException
    	        
    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    	        nameValuePairs.add(new BasicNameValuePair("start_time", timestamp));
    	        nameValuePairs.add(new BasicNameValuePair("intervals", joinedIntervals));
    	        mPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    	        mClient.execute(mPost);
    	        
                return "Data transmission completed";
                
    	    } catch (IndexOutOfBoundsException e) {
    	    	return "No data transmitted";
    	    } catch (ClientProtocolException e) {
    	    	return "Client protocol failed";
    	    } catch (IOException e) {
    	    	return "IO failed";
    	    }
    	}

    }



    // Send data via Bluetooth connection
    class BluetoothDataSender extends DataSender {

    	@Override
    	protected void sendInBackground(String timestamp) {
    		// a convoluated way of starting an async task. In order to call this method there needs to
    		// be an existing instance of BluetoothDataSender already. This instance is created by the activity
    		// when it determines whether to send using Bluetooth or HTTP.
    		new BluetoothDataSender().execute(timestamp);
    	}
    	
    	
    	@Override
    	protected String send(String timestamp, List<Long> intervals) {
    		return "Bluetooth not yet implemented";
    	}
    }
}