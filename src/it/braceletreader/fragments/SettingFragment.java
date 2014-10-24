package it.braceletreader.fragments;

import it.braceletreader.BraceletReader;
import it.braceletreader.R;
import it.braceletreader.SharedData;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 
 * Fragment that show the setting layout
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class SettingFragment extends MyFragment implements Observer
{
	SharedData m_sharedData; // Data sharing with main class
	BraceletReader m_braceletReader; // Main class

	ImageButton m_imageButton;
	Button m_braceletSelectetionButton;
	Button m_wifiSelectionButton;
	TextView m_bluetoothStateTextview;
	TextView m_wifiStateTextview;
	EditText m_nameEdittext;
	EditText m_serverAddressEdittext;

	/**
	 * 
	 */
	public SettingFragment(BraceletReader braceletReader)
	{
		super(R.layout.settings_tab_bracelet_reader);
		this.m_braceletReader = braceletReader;
		this.m_sharedData = SharedData.getInstance();
	}

	/**
	 * 
	 */
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		/*
		 * UI inizialization
		 */
		this.m_bluetoothStateTextview = (TextView) view.findViewById(R.id.bluetooth_state_textview);
		this.m_wifiStateTextview = (TextView) view.findViewById(R.id.wifi_state_textview);
		this.m_nameEdittext = (EditText) view.findViewById(R.id.name_edittext);
		this.m_serverAddressEdittext = (EditText) view.findViewById(R.id.server_address_edittext);
		this.m_imageButton = (ImageButton) view.findViewById(R.id.imageButton1);
		this.m_braceletSelectetionButton = (Button) view.findViewById(R.id.bracelet_selection_button);
		this.m_wifiSelectionButton = (Button) view.findViewById(R.id.wifi_selection_button);

		/*
		 * UI update
		 */
		if ( this.m_sharedData.get(BraceletReader.USERNAME) != null )
		{
			this.m_nameEdittext.setText((String) this.m_sharedData.get(BraceletReader.USERNAME));
		}
		this.m_nameEdittext.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged( CharSequence s, int start, int before, int count )
			{
			}

			@Override
			public void beforeTextChanged( CharSequence s, int start, int count, int after )
			{
			}

			@Override
			public void afterTextChanged( Editable s )
			{
				m_sharedData.put(BraceletReader.USERNAME, m_nameEdittext.getText().toString());
			}
		});

		List<BluetoothDevice> devices = (List<BluetoothDevice>) this.m_sharedData.get(BraceletReader.CONNECTED_BRACELETS);
		String string = "";
		if ( devices != null && devices.size() > 0 )
		{
			for ( int i = 0; i < devices.size() - 1; i++ )
			{
				string += devices.get(i).getName() + "; ";
			}
			string += devices.get(devices.size() - 1).getName();
		}
		setTextViewText(this.m_bluetoothStateTextview, string);
		setTextViewText(this.m_wifiStateTextview, (String) this.m_sharedData.get(BraceletReader.CONNECTED_WIFI_NAME));

		if ( this.m_sharedData.get(BraceletReader.SERVER_ADDRESS) != null )
		{
			this.m_serverAddressEdittext.setText((String) this.m_sharedData.get(BraceletReader.SERVER_ADDRESS));
		}

		this.m_imageButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				m_braceletReader.pickImage();
			}
		});
		Bitmap image = (Bitmap) this.m_sharedData.get(BraceletReader.USER_IMAGE);
		if ( image != null )
		{
			this.m_imageButton.setImageBitmap(image);
		}

		this.m_braceletSelectetionButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				m_braceletReader.selectBracelet();
			}
		});

		this.m_wifiSelectionButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				m_braceletReader.startWifiConnection();
			}
		});

		/* Register observer to update the UI on data changing */
		this.m_sharedData.addObserver(this);
		return view;
	}

	/**
	 * 
	 */
	@Override
	public void onDestroyView()
	{
		/* Unregister observer */
		this.m_sharedData.deleteObserver(this);
		super.onDestroyView();
	}

	/**
	 * Method to set text of state connection TextViews
	 */
	private void setTextViewText( TextView textview, String name )
	{
		if ( name != null )
		{
			textview.setText(getString(R.string.connected_to_state) + " " + name);
		} else
		{
			textview.setText(getString(R.string.not_connected_state));
		}
	}

	/**
	 * Method called on data changing to update UI
	 */
	@Override
	public void update( Observable observable, Object data )
	{
		this.m_imageButton.setImageBitmap((Bitmap) this.m_sharedData.get(BraceletReader.USER_IMAGE));

		List<BluetoothDevice> devices = (List<BluetoothDevice>) this.m_sharedData.get(BraceletReader.CONNECTED_BRACELETS);
		String string = "";
		if ( devices != null && devices.size() > 0 )
		{
			for ( int i = 0; i < devices.size() - 1; i++ )
			{
				string += devices.get(i).getName() + "; ";
			}
			string += devices.get(devices.size() - 1).getName();
		}
		setTextViewText(this.m_bluetoothStateTextview, string);
		setTextViewText(this.m_wifiStateTextview, (String) this.m_sharedData.get(BraceletReader.CONNECTED_WIFI_NAME));
	}
}