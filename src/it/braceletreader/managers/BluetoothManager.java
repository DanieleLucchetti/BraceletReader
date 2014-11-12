package it.braceletreader.managers;

import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

/**
 * 
 * Class that manage Bluetooth. First to be used, you need starting it as Service
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class BluetoothManager extends Service implements LeScanCallback
{
	IBinder m_binder;
	BluetoothAdapter m_bluetoothAdapter;
	BluetoothLeCallbacks m_callback;
	Set<BluetoothDevice> m_foundDevices;

	/**
	 * 
	 */
	public BluetoothManager()
	{
		super();
		this.m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.m_binder = new MyBinder();
	}

	/**
	 * 
	 */
	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		return START_NOT_STICKY;
	}

	/**
	 * Return if Bluetooth is supported
	 */
	public boolean isSupported()
	{
		return this.m_bluetoothAdapter != null;
	}

	/**
	 * Enable Bluetooth
	 */
	public void enable()
	{
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * Disable Bluetooth
	 */
	public void disable()
	{
		this.m_bluetoothAdapter.disable();
	}

	/**
	 * Return if Bluetooth is enabled
	 */
	public boolean isEnabled()
	{
		return this.m_bluetoothAdapter.isEnabled();
	}

	/**
	 * Open the Bluetooth setting on the screen
	 */
	public void showSettings()
	{
		Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * Return the paired Bluetooth device
	 */
	public Set<BluetoothDevice> getBondedDevices()
	{
		return this.m_bluetoothAdapter.getBondedDevices();
	}

	/**
	 * Start Bluetooth Low Energy scanning
	 * 
	 * callback is the one who manages the found devices
	 * 
	 * timeout indicates when stopping scan automatically is milliseconds
	 */
	public void startLeScan( final BluetoothLeCallbacks callback, int timeout )
	{
		this.m_callback = callback;
		this.m_foundDevices = new HashSet<BluetoothDevice>();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				m_bluetoothAdapter.stopLeScan(BluetoothManager.this);
				m_callback.leScanStopped(m_foundDevices);
				m_foundDevices = null;
				m_callback = null;
			}
		}, timeout);

		this.m_bluetoothAdapter.startLeScan(this);
	}

	/**
	 * Stop Bluetooth Low Energy scanning
	 * 
	 * callback is the one who manages the found devices
	 */
	public void stopLeScan()
	{
		this.m_bluetoothAdapter.stopLeScan(this);
		this.m_callback.leScanStopped(this.m_foundDevices);
		this.m_foundDevices = null;
		this.m_callback = null;
	}

	/**
	 * Callback for devices LE found
	 */
	@Override
	public void onLeScan( BluetoothDevice device, int rssi, byte[] scanRecord )
	{
		this.m_foundDevices.add(device);
		this.m_callback.deviceFound(device, rssi, scanRecord);
	}

	/**
	 * Return the IBinder used to communicate to the Service launcher
	 */
	@Override
	public IBinder onBind( Intent intent )
	{
		return this.m_binder;
	}

	/**
	 * Class used communicate to the Service launcher when the Service is started and is stopped
	 */
	public class MyBinder extends Binder
	{
		/**
		 * Return the Service
		 */
		public BluetoothManager getService()
		{
			return BluetoothManager.this;
		}
	}
}