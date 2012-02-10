package com.ottochiu.mse.heartbeat_simulator;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
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
	
	// The text "Session started"
	TextView mSessionStarted;
	
	// The start time of the session
	TextView mStartTime;
	
	// Scroll view
	ScrollView mScrollView;

	// The activity's data
	ActivityData mData;

	// The async task for sending data
	SenderTask mTask;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mToggle = (ToggleButton) findViewById(R.id.beatToggle);
        mBeat = (Button) findViewById(R.id.beatButton);
        mIntervalDisplay = (TextView) findViewById(R.id.intervalDisplay);
        mSessionStarted = (TextView) findViewById(R.id.sessionStarted);
        mStartTime = (TextView) findViewById(R.id.startTime);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        mData = (ActivityData) getLastNonConfigurationInstance();
        
        // If first time rendering the activity
        if (mData == null) {
        	mData = new ActivityData();
        } else {
        	mToggle.setChecked(mData.mBeatVisibility);
        	mBeat.setVisibility(mData.mBeatVisibility ? View.VISIBLE : View.INVISIBLE);
        	
        	for (Long interval : mData.mIntervals) {
        		addIntervalToDisplay(interval);
        	}
        	
        	mSessionStarted.setVisibility(mData.mSessionStartedVisible ? View.VISIBLE : View.GONE);
        	mStartTime.setText(mData.mStartTime);
        }
        
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }
    
    @Override
    protected void onStop() {
    	super.onStart();
    	
    	try {
    		// Do not allow data transfer to be interrupted.
    		mTask.cancel(false);
    		SimulatorApplication.getApplication(this).getSocket().close();
    	} catch (NullPointerException e) {
    	} catch (IOException e) {
		}
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        return new ActivityData(mData);
    }
    
    
    /// Handles the ON/OFF toggle button
    public void toggleUpdate(View v) {

    	// if user is enabling beat
    	if (mToggle.isChecked()) {
    		// steps involved:
    		// initializing all data values
        	mData.mBeatTime = 0;
        	mIntervalDisplay.setText("");
        	mData.mIntervals.clear();
        	mSessionStarted.setVisibility(View.VISIBLE);
        	mData.mSessionStartedVisible = true;
        	mData.mStartTime = new Timestamp(System.currentTimeMillis()).toString();
        	mStartTime.setText(mData.mStartTime);
    	} else {
    		// stopping the beat means: send intervals to server
        	sendIntervals();
    	}
    	
    	// enable/disable the beat button.
    	mBeat.setVisibility(mToggle.isChecked() ? View.VISIBLE : View.INVISIBLE);
    	mData.mBeatVisibility = mToggle.isChecked();
    }
    
    /// Handles the Beat button
    public void beat(View v) {
    	
    	// use current as the current time. Calling currentTimeMillis() again
    	// may result in a new time.
    	long current = System.currentTimeMillis();
    	
    	if (mData.mBeatTime != 0) {
    		long interval = current - mData.mBeatTime;
    		addIntervalToDisplay(interval);
    		mData.mIntervals.add(new Long(interval));
    		
    		mScrollView.post(new Runnable() {

    	        @Override
    	        public void run() {
    	        	mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    	        }
    	    });
    	}
    	
    	mData.mBeatTime = current;
    	v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }
    
    
    
    /// Sends interval data and clear the intervals list.
    private void sendIntervals() {
    	
    	// send only when there is >1 interval
		mToggle.setEnabled(false);
		
		mSessionStarted.setVisibility(View.GONE);
		mData.mSessionStartedVisible = false;
		
		String startTime = mStartTime.getText().toString();
		mData.mStartTime = "Sending data...";
		mStartTime.setText(mData.mStartTime);
		
		mTask = new SenderTask(startTime, mData.mIntervals);
		mTask.execute();
    }
    
    private void addIntervalToDisplay(long interval) {
    	mIntervalDisplay.append(String.format("%5d ms%n", interval));
    }
    
    /////////////////////////////////
    // Sender Async Task
    
    private class SenderTask extends AsyncTask<Void, Void, String> {
    	
    	private String mTimestamp;
    	private List<Long> mIntervals;
    	
    	SenderTask(String timestamp, List<Long> intervals) {
    		mTimestamp = timestamp;
    		mIntervals = intervals;
    	}
    	
    	protected String doInBackground(Void... args) {
    		return SimulatorApplication.getApplication(HeartbeatSimulatorActivity.this).getSender().
    				send(mTimestamp, mIntervals);
        }

        protected void onPostExecute(String result) {
        	mToggle.setEnabled(true);
        	mStartTime.setText(result);
        	mData.mStartTime = result;
        }
    }

    
    ////////////////////////////////
    // The data to save onStop and restored onCreate
    private class ActivityData {
    	ActivityData() {
    		mBeatVisibility = false;
    		mIntervals = new LinkedList<Long>();
    		mSessionStartedVisible = false;
    		mStartTime = "";
    		mBeatTime = 0;
    	}
    	
    	ActivityData(ActivityData data) {
    		mBeatVisibility = data.mBeatVisibility;
        	mIntervals = new LinkedList<Long>(data.mIntervals);
        	mSessionStartedVisible = data.mSessionStartedVisible;
        	mStartTime = new String(data.mStartTime);
        	mBeatTime = data.mBeatTime;
    	}
    	
    	boolean mBeatVisibility;
    	List<Long> mIntervals;
		boolean mSessionStartedVisible;
    	String mStartTime;
    	long mBeatTime;
    }
}