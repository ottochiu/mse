package com.ottochiu.mse.heartbeat_simulator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.bluetooth.BluetoothSocket;
import android.util.Log;


abstract class DataSender {
	abstract String send(String timestamp, List<Long> intervals);
}    	


// Send data via HTTP POST method
class HttpDataSender extends DataSender {
	
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
class BluetoothDataSender extends DataSender {
	private static final String TAG = "BluetoothDataSender";
	
	BluetoothSocket mSocket;
	
	BluetoothDataSender(BluetoothSocket socket) {
		mSocket = socket;
	}
	
	@Override
	String send(String timestamp, List<Long> intervals) {
		try {
			OutputStream out = mSocket.getOutputStream();
			
			// Message format is: interval size (4 bytes), intervals (arbitrary number of bytes)
			int numBytes = intervals.size() * Long.SIZE / 8;
			ByteBuffer buf = ByteBuffer.allocate((Integer.SIZE / 8) + numBytes);

			// Write the size so the server knows how many bytes to read.
			buf.putInt(numBytes);
			
			for (Long val : intervals) {
				buf.putLong(val.longValue());
			}
			
			out.write(buf.array());
			out.flush();
			
			Log.i(TAG, "Sending via Bluetooth");
			
			return "Finished writing";
		} catch (IOException e) {
			return "Bluetooth output error";
		}
	}
}