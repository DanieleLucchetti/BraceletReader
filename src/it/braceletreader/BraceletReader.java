package it.braceletreader;

import it.braceletreader.fragments.MainFragment;
import it.braceletreader.fragments.SettingFragment;
import it.braceletreader.listeners.BluetoothBraceletListener;
import it.braceletreader.listeners.BluetoothLeBraceletListener;
import it.braceletreader.listeners.BluetoothLeService;
import it.braceletreader.listeners.BraceletListener;
import it.braceletreader.managers.BluetoothLeCallbacks;
import it.braceletreader.managers.BluetoothManager;
import it.braceletreader.managers.DataManager;
import it.braceletreader.sensortag.SensorTagAccelerometer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/**
 * 
 * Main class. Activity to launch application
 * 
 * \author Lucchetti Daniele
 * 
 */
public class BraceletReader extends FragmentActivity implements android.content.DialogInterface.OnMultiChoiceClickListener, android.content.DialogInterface.OnClickListener, BluetoothLeCallbacks
{
	public static final int REQUEST_PICK_IMAGE = 0;								///< ID for image picking request

	public static final String USERNAME = "username";							///< Key to access to the value in SharedData containing the username
	public static final String USER_IMAGE = "user_image";						///< Key to access to the value in SharedData containing the user's image
	public static final String CONNECTED_BRACELETS = "connected_bracelet";		///< Key to access to the value in SharedData containing the list of connected bracelet
	public static final String CONNECTED_WIFI_NAME = "connected_wifi_name";		///< Key to access to the value in SharedData containing the connected wifi's name
	public static final String SERVER_ADDRESS = "server_address";				///< Key to access to the value in SharedData containing the server address what send data

	private MainFragment m_mainFragment; 										// The main fragment
	private SettingFragment m_settingFragment; 									// Fragment to show the settings
	private Handler m_handler; 													// Handler used to receive message by other thread and show information to user
	private SharedData m_sharedData;											// Data structure to share data between various Fragment

	private boolean m_initialBluetoothState;									// Bluetooth state when the app is created
	private boolean m_initialWifiState;											// Wifi state when the app is created
	private ServiceConnection m_serviceConnection;								// ServiceConnection for state update of bonded service
	private BluetoothManager m_bluetoothManager;								// Bluetooth manager to execute every operation related to Bluetooth
	private WifiManager m_wifiManager;											// Wifi manager to execute every operation related to Wifi
	private ConnectivityManager m_connectivityManager;							// Object to check if the device is connected to network
	private BluetoothDevice[] m_foundDevices;									// Array containing the Bluetooth devices candidate to connection
	private List<BluetoothDevice> m_selectedDevices;							// List that containing indices of devices which the user want to connect
	private boolean m_bluetoothLeMode = true;									// If true indicating that they will be searched Bluetooth LE devices

	private DataManager<Data> m_dataManager;									// Data structure to contain data from Bracelets
	private BraceletListener[] m_braceletListener;								// Threads to communicate with Bracelets
	private Sender m_sender;													// Thread to communicate with Server

	/**
	 * Method called when the app is created
	 */
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bracelet_reader);

		/*
		 * Structure initialization
		 */
		this.m_dataManager = new DataManager<Data>();
		this.m_sharedData = SharedData.getInstance();

		/*
		 * UI initialization
		 */
		ActionBar bar = getActionBar();
		/* Tabs creating */
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		this.m_mainFragment = new MainFragment();
		this.m_settingFragment = new SettingFragment(this);
		Tab mainTab = bar.newTab();
		mainTab.setText(getString(R.string.tab1_name));
		mainTab.setTabListener(this.m_mainFragment);
		Tab settingsTab = bar.newTab();
		settingsTab.setText(getString(R.string.tab2_name));
		settingsTab.setTabListener(this.m_settingFragment);
		bar.addTab(mainTab);
		bar.addTab(settingsTab);

		/* Handler definition */
		this.m_handler = new Handler(Looper.getMainLooper())
		{
			@Override
			public void handleMessage( Message inputMessage )
			{
				/* The Message must contain an ID of a string to stamp and can contain a String in obj attribute as additional information */
				String text = getString(inputMessage.arg1);
				if ( inputMessage.obj != null )
				{
					text += " " + inputMessage.obj;
				}
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
			}
		};

		/*
		 * WiFi initialization
		 */
		this.m_wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		this.m_connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		this.m_initialWifiState = this.m_wifiManager.isWifiEnabled();

		// Initialization of server address (//TODO this lines must remove)
		//this.m_sharedData.put(BraceletReader.SERVER_ADDRESS, getString(R.string.server_URL));
		this.m_sharedData.put(BraceletReader.SERVER_ADDRESS, "http://192.168.1.4:8000");
		this.m_sharedData.put(BraceletReader.USERNAME, "Prova");
	}

	/**
	 * Method called when the app is resumed
	 */
	@Override
	protected void onResume()
	{
		/*
		 * Bluetooth initialization
		 */
		if ( this.m_bluetoothManager == null )
		{
			// Create a ServiceConnection object and bind the Bluetooth service. ServiceConnection give us information about binding
			this.m_serviceConnection = new MyServiceConnection();
			bindService(new Intent(this, BluetoothManager.class), this.m_serviceConnection, Context.BIND_AUTO_CREATE);
		}

		// Enabling wifi
		if ( !this.m_wifiManager.isWifiEnabled() )
		{
			this.m_wifiManager.setWifiEnabled(true);
		}

		super.onResume();
	}

	/**
	 * Method called when the app is paused
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	/**
	 * Method called when the app is stopped
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
	}

	/**
	 * Method called when the app is destroyed
	 */
	@Override
	protected void onDestroy()
	{
		// All thread are stopped
		for ( int i = 0; i < this.m_braceletListener.length; i++ )
		{
			if ( this.m_braceletListener[i] != null )
			{
				this.m_braceletListener[i].alt();
			}
		}
		if ( this.m_sender != null )
		{
			this.m_sender.alt();
		}

		// Turn off Bluetooth and Wifi if they were turned off at the app start
		if ( !this.m_initialBluetoothState )
		{
			this.m_bluetoothManager.disable();
		}
		if ( !this.m_initialWifiState )
		{
			this.m_wifiManager.setWifiEnabled(false);
		}
		// Unbind and stop Bluetooth service
		unbindService(this.m_serviceConnection);
		stopService(new Intent(this, BluetoothManager.class));
		super.onDestroy();
	}

	/**
	 * Method to give information about started activity
	 */
	@Override
	public void onActivityResult( int request, int result, Intent intent )
	{
		switch ( request )
		{
		case (REQUEST_PICK_IMAGE):
			// Activity to pick an image as user image
			if ( result == Activity.RESULT_OK )
			{
				Uri uri = intent.getData();
				setUserImage(uri);
			}
			break;
		}
	}

	/**
	 * Set the type of Bluetooth devices to find
	 * 
	 * \param enabled If true, Bluetooth LE are used; traditional Bluetooth otherwise
	 */
	public void setBluetoothLeMode( boolean enabled )
	{
		this.m_bluetoothLeMode = enabled;
	}

	/**
	 * Return true if they will searched Bluetooth LE devices
	 */
	public boolean getBluetoothLeMode()
	{
		return this.m_bluetoothLeMode;
	}

	/**
	 * Show Bluetooth binding device in case it's already paired or start Bluetooth LE device scan
	 */
	public void selectBracelets()
	{
		if ( !this.m_bluetoothLeMode )
		{
			Set<BluetoothDevice> bondedDevices = this.m_bluetoothManager.getBondedDevices();
			this.m_foundDevices = new BluetoothDevice[bondedDevices.size()];
			bondedDevices.toArray(this.m_foundDevices);
			showAlertDialog();
		} else
		{
			this.m_bluetoothManager.startLeScan(this, 1000);
		}
	}

	/**
	 * Show an alert dialog with a multi-choice list of devices
	 */
	public void showAlertDialog()
	{
		int size = this.m_foundDevices.length;
		this.m_selectedDevices = new ArrayList<BluetoothDevice>();
		/* Construct an array containing names of Blueooth devices */
		CharSequence[] names = new CharSequence[size];
		for ( int i = 0; i < size; i++ )
		{
			names[i] = this.m_foundDevices[i].getName();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title);
		builder.setMultiChoiceItems(names, new boolean[size], this);
		builder.setPositiveButton(R.string.ok, this); // If positive button is pressed, a connection is attempted with selected device
		builder.setNegativeButton(R.string.search_other_device, this); // If positive button is pressed, Bluetooth settings are opened to paired other devices 
		builder.create().show();
	}

	/**
	 * Connect to a Wifi network and verify Internet connection
	 */
	public void startWifiConnection()
	{
		if ( this.m_braceletListener != null )
		{
			if ( this.m_sender == null || (this.m_sender != null && !this.m_sender.isAlive()) )
			{
				if ( !this.m_wifiManager.isWifiEnabled() )
				{
					this.m_wifiManager.setWifiEnabled(true);
				}

				/* Check Network and start thread to send data to server */
				NetworkInfo networkInfo = this.m_connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if ( networkInfo != null && networkInfo.isConnected() )
				{
					this.m_sender = new Sender(this.m_dataManager, (String) this.m_sharedData.get(BraceletReader.SERVER_ADDRESS), (String) this.m_sharedData.get(BraceletReader.USERNAME));
					this.m_sender.start();
				} else
				{
					// Wifi settings are opened if device is not connected to network
					Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
					startActivity(intent);
				}
			} else
			{
				this.m_sender.alt();
			}
		}
	}

	/**
	 * Called when the items are clicked in the dialog
	 */
	@Override
	public void onClick( DialogInterface dialog, int which, boolean isChecked )
	{
		// If device is selected, it is add to list, else it is removed
		if ( isChecked )
		{
			this.m_selectedDevices.add(this.m_foundDevices[which]);
		} else
		{
			this.m_selectedDevices.remove(Integer.valueOf(which));
		}
	}

	/**
	 * Called when the buttons are clicked in the dialog
	 */
	@Override
	public void onClick( DialogInterface dialog, int which )
	{
		if ( which == DialogInterface.BUTTON_POSITIVE )
		{
			// If the number of selected devices is less than eight, the connections are created. Else a message is shown to user  
			if ( this.m_selectedDevices.size() < 8 )
			{
				createBluetoothConnections(this.m_selectedDevices);
			} else
			{
				Message message = this.m_handler.obtainMessage();
				message.arg1 = R.string.error_max_connection_possible;
				message.sendToTarget();
			}
		} else
		{
			// If is clicked on negative button, it means that the wanted devices was not in the list
			if ( !this.m_bluetoothLeMode )
			{
				// Bluetooth setting is opened to search and pair other devices
				this.m_bluetoothManager.showSettings();
			} else
			{
				// If the interesting LE devices aren't found in 1 seconds another search in 5 seconds is made
				this.m_bluetoothManager.startLeScan(this, 5000);
			}
		}
		this.m_selectedDevices = null;
	}

	/**
	 * Start a new connection with the specified device
	 */
	public void createBluetoothConnections( List<BluetoothDevice> devices )
	{
		// If the BraceletListeners are running, they are stopped
		if ( this.m_braceletListener != null )
		{
			for ( int i = 0; i < this.m_braceletListener.length; i++ )
			{
				this.m_braceletListener[i].alt();
			}
		}

		// Than they are instanced again
		this.m_braceletListener = new BraceletListener[devices.size()];
		if ( !this.m_bluetoothLeMode )
		{
			// If the user want using a Bluetooth version less than 4.0
			for ( int i = 0; i < devices.size(); i++ )
			{
				this.m_braceletListener[i] = new BluetoothBraceletListener(i, this, this.m_dataManager, devices.get(i), UUID.fromString(getString(R.string.UUID)));
				this.m_braceletListener[i].start();
			}
		} else
		{
			// If the user want using Bluetooth LE devices
			for ( int i = 0; i < devices.size(); i++ )
			{
				// It is created the list of Services that we want activating
				List<BluetoothLeService> services = new ArrayList<BluetoothLeService>();
				services.add(new SensorTagAccelerometer());
				//services.add(new SensorTagGyroscope());
				this.m_braceletListener[i] = new BluetoothLeBraceletListener(i, this, this.m_dataManager, devices.get(i), services);
				this.m_braceletListener[i].start();
			}
		}
		this.m_sharedData.put(BraceletReader.CONNECTED_BRACELETS, new ArrayList<BluetoothDevice>());
	}

	/**
	 * Called when the LE scan is finished
	 */
	@Override
	public void leScanStopped( Set<BluetoothDevice> foundDevices )
	{
		// Bluetooth LE scan is finished, so the found devices are shown in the dialog
		this.m_foundDevices = new BluetoothDevice[foundDevices.size()];
		foundDevices.toArray(this.m_foundDevices);
		showAlertDialog();
	}

	/**
	 * Called when a device LE is found
	 */
	@Override
	public void deviceFound( BluetoothDevice device, int rssi, byte[] scanRecord )
	{
	}

	/**
	 * Notify errors or state changing by BraceletListener
	 * 
	 * \param id The BraceletListener id
	 * \param state An integer representing the new state of listener
	 */
	public void notifyListenerStateChanged( int id, int state )
	{
		switch ( state )
		{
		case BraceletListener.CONNECTION_ESTABLISHED:
			// The connection was successful, so the list of connected devices is update
			List<BluetoothDevice> devices = (List<BluetoothDevice>) this.m_sharedData.get(BraceletReader.CONNECTED_BRACELETS);
			devices.add(this.m_braceletListener[id].getDevice());
			this.m_sharedData.put(BraceletReader.CONNECTED_BRACELETS, devices);
			break;
		case BraceletListener.ERROR_CREATING_CONNECTION:
			// Connection error when listener was trying to connect, so a message is shown to user
			this.m_braceletListener[id].alt();
			Message message = this.m_handler.obtainMessage();
			message.arg1 = R.string.error_not_connected;
			message.obj = this.m_braceletListener[id].getDevice().getName();
			message.sendToTarget();
			break;
		}
	}

	/**
	 * Update the UI
	 * 
	 * \param data A JSON string representing the data to show
	 */
	public void updateUI( Object data )
	{
		this.m_mainFragment.updateUI(data);
	}

	/**
	 * Start activity to pick an image
	 */
	public void pickImage()
	{
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_PICK_IMAGE);
	}

	/**
	 * Resize and set the new user image
	 */
	public void setUserImage( Uri uri )
	{
		/* Take the image */
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
		cursor.moveToFirst();
		String filePath = cursor.getString(0);
		cursor.close();
		Bitmap image = BitmapFactory.decodeFile(filePath);

		/* Resize image */
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, 130, 130, true);

		this.m_sharedData.put(BraceletReader.USER_IMAGE, scaledBitmap);
	}

	/**
	 * Signal that the service in started
	 * 
	 * \param bluetoothManager The instance of BluetoothManager by the Service
	 */
	public void serviceStarted( BluetoothManager bluetoothManager )
	{
		// The BLuetooth service is started, so it is verified that Bluetooth is supported. In this case, the app is closed
		this.m_bluetoothManager = bluetoothManager;
		if ( !this.m_bluetoothManager.isSupported() )
		{
			/* Error message */
			AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
			builder.setCancelable(false);
			builder.setMessage(R.string.error_bluetooth_not_supported);
			builder.setPositiveButton(R.string.closing_message, new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick( DialogInterface arg0, int arg1 )
				{
					finish();
				}

			});
		}
		this.m_initialBluetoothState = this.m_bluetoothManager.isEnabled();

		// Enablig Bluetooth
		if ( !this.m_bluetoothManager.isEnabled() )
		{
			this.m_bluetoothManager.enable();
		}
	}

	/**
	 * Signal that the service is stopped
	 */
	public void serviceStopped()
	{
		this.m_bluetoothManager = null;
	}

	/**
	 * My implementation of ServiceConnection to receive changing state of bonded service
	 */
	class MyServiceConnection implements ServiceConnection
	{

		/**
		 * Called when a Service is binded
		 */
		public void onServiceConnected( ComponentName className, IBinder binder )
		{
			serviceStarted(((BluetoothManager.MyBinder) binder).getService());
		}

		/**
		 * Called when a Service is unbinded
		 */
		public void onServiceDisconnected( ComponentName className )
		{
			serviceStopped();
		}
	}
}