package com.ottochiu.mse.bluetooth_device_manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RegisteredDevices extends SQLiteOpenHelper {

	private static final String TAG = "RegisteredDevices";
	private static final String TABLE_NAME = "registered_devices";
	private static final String COL_DEVICE_NAME = "device_name";
	private static final String COL_UUID = "uuid";
	private static final String COL_PKG_NAME = "package_name";
	private static final int DATABASE_VERSION = 1;
	
	private static final String CREATE_TABLE = 
			String.format("CREATE TABLE %s (%s TEXT PRIMARY KEY, %s TEXT, %s TEXT);",
					TABLE_NAME, COL_DEVICE_NAME, COL_UUID, COL_PKG_NAME);
	
	
	public class Device {
		final String deviceName;
		final UUID uuid;
		final String pkgName;
		
		Device(String deviceName, String uuid, String pkgName) {
			this.deviceName = deviceName;
			this.uuid = UUID.fromString(uuid);
			this.pkgName = pkgName;			
		}
		
		@Override
		public String toString() {
			return String.format("Device name: %s, UUID: %s, package: %s",
					deviceName, uuid.toString(), pkgName);
		}
	}
	
	
	
	RegisteredDevices(Context context) {
		super(context, TABLE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Creating table");
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// Implement this method when schema changes
		Log.e(TAG, "onUpgrade not implemented");
	}
	
	
	public void registerDevice(String deviceName, UUID uuid, String pkgName) {
		Log.v(TAG, String.format("Registering %s, UUID: %s, package: %s", deviceName, uuid.toString(), pkgName));
		
		SQLiteDatabase db = getWritableDatabase();
		
		
		// First check to see if the device is already registered. 
		Cursor c = db.query(
				TABLE_NAME,
				new String[] { COL_UUID, COL_PKG_NAME },
				COL_DEVICE_NAME + "=?", 
				new String[] { deviceName }, 
				null, null, null);

		ContentValues values = new ContentValues();
		values.put(COL_UUID, uuid.toString());
		values.put(COL_PKG_NAME, pkgName);
		
		// Device registered already. Update if necessary
		if (c.getCount() > 0) {
			c.moveToFirst();
			
			String oldUuid = c.getString(c.getColumnIndex(COL_UUID));
			String oldPkgName = c.getString(c.getColumnIndex(COL_PKG_NAME));
			
			Log.i(TAG, deviceName + " already registered.");
			
			// if UUID and/or package name have been updated
			// note that this implies a device with the same name can hijack another
			// device's connection
			if (! (oldUuid.equals(uuid.toString()) && oldPkgName.equals(pkgName)) ) {
				Log.v(TAG, "Updating registration.");
				db.update(TABLE_NAME, values, COL_DEVICE_NAME + "=?", new String[] { deviceName });
			}
			// else no change
			
		} else {
			// device not registered. Insert a new row
			Log.i(TAG, "Registering new device");
			values.put(COL_DEVICE_NAME, deviceName);
			long id = db.insert(TABLE_NAME, null, values);
			Log.v(TAG, "New row id: " + id);
		}
	}
	
	public Device getRegisteredDevices(String deviceName) {
		SQLiteDatabase db = getReadableDatabase();

		Cursor c = db.query(TABLE_NAME, null, COL_DEVICE_NAME + "=?", new String[] { deviceName }, null, null, null);
		
		// device name is a key, therefore expect only 1 row
		if (c.getCount() == 1) {
			Log.v(TAG, "Registered device found: " + deviceName);
			c.moveToFirst();
			
			return new Device(
					c.getString(c.getColumnIndex(COL_DEVICE_NAME)),
					c.getString(c.getColumnIndex(COL_UUID)),
					c.getString(c.getColumnIndex(COL_PKG_NAME)));
			
		} else {
			Log.i(TAG, "Device not found: " + deviceName);
			return null;
		}
		
	}
	
	public List<Device> getRegisteredDevices() {
		SQLiteDatabase db = getReadableDatabase();

		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
		
		Log.v(TAG, "Number of registered devices: " + c.getCount());
		
		ArrayList<Device> devices = new ArrayList<Device>(c.getCount());
		
		int deviceNameIdx = c.getColumnIndex(COL_DEVICE_NAME);
		int uuidIdx = c.getColumnIndex(COL_UUID);
		int pkgNameIdx = c.getColumnIndex(COL_PKG_NAME);
		
		c.moveToFirst();
		while (!c.isAfterLast()) {
			Device d = new Device(
					c.getString(deviceNameIdx),
					c.getString(uuidIdx),
					c.getString(pkgNameIdx));
			
			Log.i(TAG, d.toString());
			devices.add(d);
			
			c.moveToNext();
		}
		
		return devices;
	}
}
