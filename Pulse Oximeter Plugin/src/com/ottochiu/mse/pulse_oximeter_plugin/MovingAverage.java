package com.ottochiu.mse.pulse_oximeter_plugin;

import java.util.Arrays;

// Can also make this a template/generic
// but for purposes of the pulse oximeter, this is made a double array.
public class MovingAverage {
	
	private double[] buf;
	private int index = 0;
		
	private double runningSum;
	
	public MovingAverage(int size, double initialVal) {
		buf = new double[size];
		Arrays.fill(buf, initialVal);
		runningSum = size * initialVal;
	}
	
	// Returns the moving average after adding value
	public double add(double value) {
		index = (index + 1) % buf.length;
		runningSum = runningSum - buf[index] + value;
		buf[index] = value;
		
		return runningSum / buf.length;
	}
	
	public void reset(double value) {
		Arrays.fill(buf, value);
		runningSum = buf.length * value;
	}
}
