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
	 * 
	 * \param foundDevices A Set of found devices
	 */
	public void leScanStopped( Set<BluetoothDevice> foundDevices );

	/**
	 * Callback to signal a device LE is found
	 * 
	 * \param device The found device
	 * \param rssi The rssi value of found device
	 * \param scanRecord The content of the advertisement record offered by the remote device
	 */
	public void deviceFound( BluetoothDevice device, int rssi, byte[] scanRecord );
}