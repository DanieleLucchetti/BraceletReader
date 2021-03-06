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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 
 * Fragment that show the setting layout
 * 
 * \author Lucchetti Daniele
 * 
 */
public class SettingFragment extends MyFragment implements Observer
{
	private SharedData m_sharedData; 				// Data structure to share data between various Fragment
	private BraceletReader m_braceletReader; 		// Main class
	private Handler m_handler;						// Handler to update the UI

	private ImageButton m_imageButton;				// ImageButton to pick user image
	private Button m_braceletsSelectionButton;		// Button to search and select bracelets
	private Button m_wifiSelectionButton;			// Button to search and select Wifi
	private TextView m_bluetoothStateTextview;		// TextView to show Bluetooth state
	private TextView m_wifiStateTextview;			// TextView to show Wifi state
	private EditText m_nameEdittext;				// EditText to insert username
	private EditText m_serverAddressEdittext;		// EditText to insert server address
	private CheckBox m_bluetoothLeSearching;		// CheckBox to select type of Bluetooth to use

	/**
	 * Costructor
	 * 
	 * \param braceletReader Main class
	 */
	public SettingFragment( BraceletReader braceletReader )
	{
		super(R.layout.settings_tab_bracelet_reader);
		this.m_braceletReader = braceletReader;
		this.m_sharedData = SharedData.getInstance();
	}

	/**
	 * Called when the view is created
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
		this.m_braceletsSelectionButton = (Button) view.findViewById(R.id.bracelet_selection_button);
		this.m_wifiSelectionButton = (Button) view.findViewById(R.id.wifi_selection_button);
		this.m_bluetoothLeSearching = (CheckBox) view.findViewById(R.id.bluetooth_le_searching_checkbox);

		/*
		 * UI update
		 */
		if ( this.m_sharedData.get(BraceletReader.USERNAME) != null )
		{
			this.m_nameEdittext.setText((String) this.m_sharedData.get(BraceletReader.USERNAME));
		}
		// TextChangeListener to get new username inserted
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

		// Construction the string for Bluetooth state
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

		// Load server address
		if ( this.m_sharedData.get(BraceletReader.SERVER_ADDRESS) != null )
		{
			this.m_serverAddressEdittext.setText((String) this.m_sharedData.get(BraceletReader.SERVER_ADDRESS));
		}
		// TextChangeListener to get new username inserted
		this.m_serverAddressEdittext.addTextChangedListener(new TextWatcher()
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
				m_sharedData.put(BraceletReader.SERVER_ADDRESS, m_serverAddressEdittext.getText().toString());
			}
		});

		// OnClickListeren to pick image for user image
		this.m_imageButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				m_braceletReader.pickImage();
			}
		});

		// Load user image
		Bitmap image = (Bitmap) this.m_sharedData.get(BraceletReader.USER_IMAGE);
		if ( image != null )
		{
			this.m_imageButton.setImageBitmap(image);
		}

		// OnClickListener to select bracelets
		this.m_braceletsSelectionButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				m_braceletReader.selectBracelets();
			}
		});

		// OnClickListener to select Wifi network
		this.m_wifiSelectionButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				m_braceletReader.startWifiConnection();
			}
		});

		// Load type of Bluetooth used
		this.m_bluetoothLeSearching.setChecked(this.m_braceletReader.getBluetoothLeMode());

		// OnCheckedChangeListener to get checked update
		this.m_bluetoothLeSearching.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
			{
				m_braceletReader.setBluetoothLeMode(isChecked);
			}
		});

		// Implementation of hander
		this.m_handler = new Handler(Looper.getMainLooper())
		{
			@Override
			public void handleMessage( Message message )
			{
				// Update user image
				m_imageButton.setImageBitmap((Bitmap) m_sharedData.get(BraceletReader.USER_IMAGE));

				// Update of Bluetooth state TextView
				List<BluetoothDevice> devices = (List<BluetoothDevice>) m_sharedData.get(BraceletReader.CONNECTED_BRACELETS);
				String string = "";
				if ( devices != null && devices.size() > 0 )
				{
					for ( int i = 0; i < devices.size() - 1; i++ )
					{
						string += devices.get(i).getName() + "; ";
					}
					string += devices.get(devices.size() - 1).getName();
				}
				setTextViewText(m_bluetoothStateTextview, string);

				// Update of Wifi state TextView
				setTextViewText(m_wifiStateTextview, (String) m_sharedData.get(BraceletReader.CONNECTED_WIFI_NAME));
			}
		};

		/* Register observer to update the UI on data changing */
		this.m_sharedData.addObserver(this);
		return view;
	}

	/**
	 * Called when the view is deleted
	 */
	@Override
	public void onDestroyView()
	{
		/* Unregister observer */
		this.m_sharedData.deleteObserver(this);
		super.onDestroyView();
	}

	/**
	 * Method to set text of state connection (Bluetooth or Wifi) TextViews
	 * 
	 * \param textview TextView
	 * \param name Name of Bluetooth devices or Wifi network (it can be null)
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
		this.m_handler.obtainMessage().sendToTarget();
	}
}