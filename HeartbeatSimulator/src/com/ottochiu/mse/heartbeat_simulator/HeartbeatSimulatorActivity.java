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

	// Responsible for sending data
	DataSender mSender;

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

        mSender = (DataSender) getLastNonConfigurationInstance();
        
        // If first time rendering the activity
        if (mSender == null) {
        	// default to no Bluetooth if intent does not include the option name
        	createSender(getIntent().getBooleanExtra(getString(R.string.use_bluetooth_connection), false));
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mSender;
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
        					ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        	
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
    
    
    public void createSender(boolean useBluetooth) {
    	mSender = useBluetooth ? 
    			new BluetoothDataSender() : new HttpDataSender(getString(R.string.url));
    }
    
    /// Sends interval data and clear the intervals list.
    private void sendIntervals() {
    	
    	// send only when there is >1 interval
		mToggle.setEnabled(false);
		
		mSessionStarted.setVisibility(View.GONE);
		
		String startTime = mStartTime.getText().toString();
		mStartTime.setText("Sending data...");

		new SenderTask(mSender, startTime, mIntervals).execute();
    }
    
    /////////////////////////////////
    // Data Sender
    
    private class SenderTask extends AsyncTask<Void, Void, String> {
    	
    	private DataSender mSender;
    	private String mTimestamp;
    	private List<Long> mIntervals;
    	
    	SenderTask(DataSender sender, String timestamp, List<Long> intervals) {
    		mSender = sender;
    		mTimestamp = timestamp;
    		mIntervals = intervals;
    	}
    	
    	protected String doInBackground(Void... args) {
    		return mSender.send(mTimestamp, mIntervals);
        }

        protected void onPostExecute(String result) {
        	mToggle.setEnabled(true);
        	mStartTime.setText(result);
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }

    
    
    private abstract class DataSender {
    	abstract String send(String timestamp, List<Long> intervals);
    }    	


    // Send data via HTTP POST method
    private class HttpDataSender extends DataSender {
		
		HttpClient mClient;
    	HttpPost mPost;
    	
    	HttpDataSender(String url) {
    		mClient = new DefaultHttpClient();
    		mPost = new HttpPost(url);
    	}
        
    	@Override
    	String send(String timestamp, List<Long> intervals) {
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
    private class BluetoothDataSender extends DataSender {
		
    	@Override
    	String send(String timestamp, List<Long> intervals) {
    		return "Bluetooth not yet implemented";
    	}
    }
}