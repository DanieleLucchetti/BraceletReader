package it.braceletreader.listeners;

import it.braceletreader.BraceletReader;
import it.braceletreader.Data;
import it.braceletreader.managers.DataManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * 
 * Implementation of BraceletListener to communicate with traditional Bluetooth device
 * 
 * \author Lucchetti Daniele
 * 
 */
public class BluetoothBraceletListener extends BraceletListener
{
	private UUID m_UUID;	// UUID to communicate with the device

	/**
	 * Constructor
	 * 
	 * \param id Thread id
	 * \param braceletReader Main class
	 * \param dataManager Data structure to put data
	 * \param device Device to get data
	 * \param UUID UUID to communicate with device
	 */
	public BluetoothBraceletListener( int id, BraceletReader braceletReader, DataManager<Data> dataManager, BluetoothDevice device, UUID UUID )
	{
		super(id, braceletReader, dataManager, device);
		this.m_UUID = UUID;
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		/* Creating connection */
		BluetoothSocket socket = null;
		try
		{
			socket = this.m_device.createRfcommSocketToServiceRecord(this.m_UUID);
			socket.connect();
			// Notify to main class that the connection is established
			this.m_braceletReader.notifyListenerStateChanged(this.m_id, BraceletListener.CONNECTION_ESTABLISHED);
			BufferedReader inputStream = null;
			try
			{
				// Input stream
				inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String input;
				while ( !this.m_stop )
				{
					/* Receive and store data and update the UI */
					input = inputStream.readLine();
					JSONObject obj = new JSONObject(input);
					JSONArray array = new JSONArray(obj.getString("value"));

					// Construct Data with type and timestamp
					Data data = new Data(obj.getString("type"), obj.getLong("timestamp"), array.getDouble(0), array.getDouble(1), array.getDouble(2));

					// To update the UI
					this.m_braceletReader.updateUI(data);

					// Add new Data to data structure
					this.m_dataManager.add(data);
				}

			} catch ( IOException e )
			{
				// Error during the streaming
				this.m_braceletReader.notifyListenerStateChanged(this.m_id, BraceletListener.ERROR_STREAMING);
			} catch ( JSONException e1 )
			{
				// The data format is incorrect
				this.m_braceletReader.notifyListenerStateChanged(this.m_id, BraceletListener.ERROR_DATA_FORMAT);
			} finally
			{
				try
				{
					inputStream.close();
					socket.close();
				} catch ( IOException e )
				{
					// Error during closing connection
					this.m_braceletReader.notifyListenerStateChanged(this.m_id, BraceletListener.ERROR_CLOSING_CONNECTION);
				}
			}
		} catch ( IOException e )
		{
			// Error while connection is creating
			this.m_braceletReader.notifyListenerStateChanged(this.m_id, BraceletListener.ERROR_CREATING_CONNECTION);
		}
	}
}