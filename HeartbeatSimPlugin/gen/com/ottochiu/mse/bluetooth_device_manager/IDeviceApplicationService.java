/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\chiuo01\\workspace\\mse\\HeartbeatSimPlugin\\src\\com\\ottochiu\\mse\\bluetooth_device_manager\\IDeviceApplicationService.aidl
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
this.registerDevice(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_version:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.version();
reply.writeNoException();
reply.writeInt(_result);
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
public void registerDevice(java.lang.String deviceName, android.os.ParcelUuid uuid, java.lang.String packageName) throws android.os.RemoteException
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
mRemote.transact(Stub.TRANSACTION_registerDevice, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public int version() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_version, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_registerDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_version = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void registerDevice(java.lang.String deviceName, android.os.ParcelUuid uuid, java.lang.String packageName) throws android.os.RemoteException;
public int version() throws android.os.RemoteException;
}
