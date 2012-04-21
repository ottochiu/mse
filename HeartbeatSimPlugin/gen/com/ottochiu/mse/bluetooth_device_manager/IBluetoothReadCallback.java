/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Documents and Settings\\ochiu\\workspace\\mse\\HeartbeatSimPlugin\\src\\com\\ottochiu\\mse\\bluetooth_device_manager\\IBluetoothReadCallback.aidl
 */
package com.ottochiu.mse.bluetooth_device_manager;
public interface IBluetoothReadCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback
{
private static final java.lang.String DESCRIPTOR = "com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback interface,
 * generating a proxy if needed.
 */
public static com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback))) {
return ((com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback)iin);
}
return new com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback.Stub.Proxy(obj);
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
case TRANSACTION_handle:
{
data.enforceInterface(DESCRIPTOR);
byte[] _arg0;
_arg0 = data.createByteArray();
this.handle(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.ottochiu.mse.bluetooth_device_manager.IBluetoothReadCallback
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
public void handle(byte[] data) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeByteArray(data);
mRemote.transact(Stub.TRANSACTION_handle, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_handle = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void handle(byte[] data) throws android.os.RemoteException;
}
