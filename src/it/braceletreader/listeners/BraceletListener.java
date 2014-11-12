package it.braceletreader.listeners;

import it.braceletreader.BraceletReader;
import it.braceletreader.Data;
import it.braceletreader.managers.DataManager;
import android.bluetooth.BluetoothDevice;

/**
 * 
 * Thread that connect to a single bracelet and receive data. Run method must be implemented
 * 
 * \author Lucchetti Daniele
 * 
 */
public abstract class BraceletListener extends Thread
{
	public static final int CONNECTION_ESTABLISHED = 0;			///< The connection is established
	public static final int ERROR_CREATING_CONNECTION = 1;		///< Error when trying to connect
	public static final int ERROR_STREAMING = 2;				///< Error during the data streaming
	public static final int ERROR_DATA_FORMAT = 3;				///< Error of data format
	public static final int ERROR_CLOSING_CONNECTION = 4;		///< Error during closing connection

	protected int m_id;											///< Id of the Thread
	protected BraceletReader m_braceletReader;					///< Main class to communicate state changing
	protected DataManager<Data> m_dataManager;					///< Data structure to put data receiving by bracelet
	protected BluetoothDevice m_device;							///< Bluetooth device to receive data
	protected boolean m_stop; 									///< When is set true, the Thread die

	/**
	 * Constructor
	 * 
	 * \param id Thread id
	 * \param braceletReader Main class
	 * \param dataManager Data structure to put data
	 * \param device Device to get data
	 */
	public BraceletListener( int id, BraceletReader braceletReader, DataManager<Data> dataManager, BluetoothDevice device )
	{
		this.m_id = id;
		this.m_braceletReader = braceletReader;
		this.m_dataManager = dataManager;
		this.m_device = device;
		this.m_stop = false;
	}

	/**
	 * Stop the Thread
	 */
	public void alt()
	{
		this.m_stop = true;
	}

	/**
	 * Return the connected device
	 * 
	 * \return Connected device
	 */
	public BluetoothDevice getDevice()
	{
		return this.m_device;
	}
}