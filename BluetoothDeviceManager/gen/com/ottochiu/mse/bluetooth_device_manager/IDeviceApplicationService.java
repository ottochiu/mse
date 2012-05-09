/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Documents and Settings\\ochiu\\workspace\\mse\\BluetoothDeviceManager\\src\\com\\ottochiu\\mse\\bluetooth_device_manager\\IDeviceApplicationService.aidl
 */
package com.ottochiu.mse.bluetooth_device_manager;
public interface IDeviceApplicationService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService
{
private static final java.lang.String DESCRIPTOR = "com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService interface,
 * generating a proxy if needed.
 */
public static com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService))) {
return ((com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService)iin);
}
return new com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_registerDevice:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
android.os.ParcelUuid _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.ParcelUuid.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
java.lang.String _arg2;
_arg2 = data.readString();
com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback _arg3;
_arg3 = com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback.Stub.asInterface(data.readStrongBinder());
this.registerDevice(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_read:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.read(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_write:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
byte[] _arg1;
_arg1 = data.createByteArray();
this.write(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getManagerActivityName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getManagerActivityName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_version:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.version();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.ottochiu.mse.bluetooth_device_manager.IDeviceApplicationService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void registerDevice(java.lang.String deviceName, android.os.ParcelUuid uuid, java.lang.String packageName, com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(deviceName);
if ((uuid!=null)) {
_data.writeInt(1);
uuid.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(packageName);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerDevice, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Reads from a stream of data from the corresponding BT device. This may block.
// Data are sent to the registered callback

public void read(java.lang.String deviceName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(deviceName);
mRemote.transact(Stub.TRANSACTION_read, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Writes data to the BT device corresponding to the caller. This may block.

public void write(java.lang.String deviceName, byte[] data) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(deviceName);
_data.writeByteArray(data);
mRemote.transact(Stub.TRANSACTION_write, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Returns the class name of the Manager activity

public java.lang.String getManagerActivityName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getManagerActivityName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String version() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_version, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_registerDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_read = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_write = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getManagerActivityName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_version = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void registerDevice(java.lang.String deviceName, android.os.ParcelUuid uuid, java.lang.String packageName, com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback callback) throws android.os.RemoteException;
// Reads from a stream of data from the corresponding BT device. This may block.
// Data are sent to the registered callback

public void read(java.lang.String deviceName) throws android.os.RemoteException;
// Writes data to the BT device corresponding to the caller. This may block.

public void write(java.lang.String deviceName, byte[] data) throws android.os.RemoteException;
// Returns the class name of the Manager activity

public java.lang.String getManagerActivityName() throws android.os.RemoteException;
public java.lang.String version() throws android.os.RemoteException;
}
