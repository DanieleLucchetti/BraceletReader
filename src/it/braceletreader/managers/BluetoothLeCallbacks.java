package it.braceletreader.managers;

import java.util.Set;

import android.bluetooth.BluetoothDevice;

/**
 * 
 * Interface to receive callbacks by BluetoothManager when Bluetooth LE is used
 * 
 * \author Lucchetti Daniele
 * 
 */
public interface BluetoothLeCallbacks
{
	/**
	 * Callback to signal the scan is stopped
	 */
	public void leScanStopped( Set<BluetoothDevice> foundDevices );

	/**
	 * Callback to signal a device LE is found
	 */
	public void deviceFound( BluetoothDevice device, int rssi, byte[] scanRecord );
}