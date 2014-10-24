package it.braceletreader;

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
import android.util.Log;

/*
 * 
 * Thread that manage the communication with the selected bracelet and put data in DataMnager
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class BraceletListener extends Thread
{
	public static final int CONNECTION_ESTABLISHED = 0;
	public static final int ERROR_CREATING_CONNECTION = 1;
	public static final int ERROR_STREAMING = 2;
	public static final int ERROR_DATA_FORMAT = 3;
	public static final int ERROR_CLOSING_CONNENCTION = 4;

	int m_id;
	BraceletReader m_braceletReader;
	DataManager<Data> m_dataManager;
	BluetoothDevice m_device;
	UUID m_UUID;
	boolean m_stop; // When is set true, the Thread die

	/**
	 *
	 */
	public BraceletListener(int id, BraceletReader braceletReader, DataManager<Data> dataManager, BluetoothDevice device, UUID UUID)
	{
		this.m_id = id;
		this.m_braceletReader = braceletReader;
		this.m_dataManager = dataManager;
		this.m_device = device;
		this.m_UUID = UUID;
		this.m_stop = false;
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
			this.m_braceletReader.notifyListenerStateChanged(this.m_id, CONNECTION_ESTABLISHED);
			BufferedReader inputStream = null;
			try
			{
				inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String input;
				while ( !this.m_stop )
				{
					/* Receive and store data and update the UI */
					input = inputStream.readLine();
					JSONObject obj = new JSONObject(input);
					JSONArray array = new JSONArray(obj.getString("value"));
					Data data = new Data(obj.getString("type"), obj.getLong("timestamp"), array.getDouble(0), array.getDouble(1), array.getDouble(2));

					this.m_braceletReader.updateUI(data); // To update the UI
					this.m_dataManager.add(data);
				}

			} catch ( IOException e )
			{
				this.m_braceletReader.notifyListenerStateChanged(this.m_id, ERROR_STREAMING);
			} catch ( JSONException e1 )
			{
				this.m_braceletReader.notifyListenerStateChanged(this.m_id, ERROR_DATA_FORMAT);
			} finally
			{
				try
				{
					inputStream.close();
					socket.close();
				} catch ( IOException e )
				{
					this.m_braceletReader.notifyListenerStateChanged(this.m_id, ERROR_CLOSING_CONNENCTION);
				}
			}
		} catch ( IOException e )
		{
			Log.e("ME", e.getMessage());
			this.m_braceletReader.notifyListenerStateChanged(this.m_id, ERROR_CREATING_CONNECTION);
		}
	}

	/**
	 * Stop the Thread
	 */
	public void alt()
	{
		this.m_stop = true;
	}

	/**
	 * 
	 */
	public BluetoothDevice getDevice()
	{
		return this.m_device;
	}
}