package com.ottochiu.mse.heartbeat_simulator;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
	
	final static String timeSuffix = " ms\n";
	
	// The ON/OFF toggle
	ToggleButton mToggle;
	
	// The Beat button
	Button mBeat;
	
	// The info window
	TextView mIntervalDisplay;
	
	// The text "Session started"
	TextView mSessionStarted;
	
	// The start time of the session
	TextView mStartTime;
	
	// Scroll view
	ScrollView mScrollView;
	
	// Time of previous beat
	long mBeatTime;
	
	// The HTTP Client and Post
	HttpClient mClient;
	HttpPost mPost;
	
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
        
        mClient = new DefaultHttpClient();
        mPost = new HttpPost(getString(R.string.url));

    }
    
    
    /// Handles the ON/OFF toggle button
    public void toggleUpdate(View v) {

    	// if user is enabling beat
    	if (mToggle.isChecked()) {
    		// steps involved:
    		// initializing all data values
        	mBeatTime = 0;
        	mIntervalDisplay.setText("");
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
    		mIntervalDisplay.append(String.format("%5d" + timeSuffix, interval));
    		
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
    
    /// Sends interval data and clear the intervals list.
    private void sendIntervals() {
    	
    	// send only when there is >1 interval
		mToggle.setEnabled(false);
		
		String startTime = mStartTime.getText().toString();
		mSessionStarted.setVisibility(View.GONE);
		mStartTime.setText("Sending data...");

		new SendDataTask().execute(startTime, mIntervalDisplay.getText().toString());
    }
    
    
    
    
    private class SendDataTask extends AsyncTask<String, Void, String> {
    	
    	protected String doInBackground(String... strings) {

    	    try {
    	        // Construct POST request
    	        String intervals = strings[1].replaceAll(timeSuffix, ",");
    	        intervals = intervals.substring(0, intervals.length()-1);
    	        
    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    	        nameValuePairs.add(new BasicNameValuePair("start_time", strings[0]));
    	        nameValuePairs.add(new BasicNameValuePair("intervals", intervals));
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

        protected void onPostExecute(String result) {
        	mToggle.setEnabled(true);
        	mStartTime.setText(result);
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }
}