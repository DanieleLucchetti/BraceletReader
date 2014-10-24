package it.braceletreader;

import it.braceletreader.fragments.MainFragment;
import it.braceletreader.fragments.SettingFragment;
import it.braceletreader.managers.BluetoothManager;
import it.braceletreader.managers.DataManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.ActionBar;
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

/*
 * 
 * Main class
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class BraceletReader extends FragmentActivity implements android.content.DialogInterface.OnMultiChoiceClickListener, android.content.DialogInterface.OnClickListener
{
	public static final int REQUEST_PICK_IMAGE = 0; // ID for image picking request

	/*
	 * Key for SharedData
	 */
	public static final String USERNAME = "username";
	public static final String USER_IMAGE = "user_image";
	public static final String CONNECTED_BRACELETS = "connected_bracelet";
	public static final String CONNECTED_WIFI_NAME = "connected_wifi_name";
	public static final String SERVER_ADDRESS = "server_address";

	MainFragment m_mainFragment; // The main fragment
	SettingFragment m_settingFragment; // Fragment to show the settings
	Handler m_handler; // Handler used to receive message by other thread
	SharedData m_sharedData;

	boolean m_initialBluetoothState; // Bluetooth state when the app is created
	boolean m_initialWifiState; // Wifi state when the app is created
	ServiceConnection m_serviceConnection; // ServiceConnection for state update of bonded service
	BluetoothManager m_bluetoothManager;
	WifiManager m_wifiManager;
	ConnectivityManager m_connectivityManager;
	List<Integer> m_selectedItemsIndexList; // List that containing indices of devices which the
											// user want to connect

	DataManager<Data> m_dataManager; // Data structure to contain data from Bracelets
	BraceletListener[] m_braceletListener; // Threads to communicate with Bracelets
	Sender m_sender; // Thread to communicate with Server

	/**
	 * 
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
		this.m_mainFragment = new MainFragment(this);
		this.m_settingFragment = new SettingFragment(this);
		bar.addTab(bar.newTab().setText(getString(R.string.tab1_name)).setTabListener(this.m_mainFragment));
		bar.addTab(bar.newTab().setText(getString(R.string.tab2_name)).setTabListener(this.m_settingFragment));

		this.m_handler = new Handler(Looper.getMainLooper())
		{
			@Override
			public void handleMessage( Message inputMessage )
			{
				Toast.makeText(getApplicationContext(), getString(inputMessage.arg1), Toast.LENGTH_LONG).show();
			}
		};

		/*
		 * WiFi initialization
		 */
		this.m_wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		this.m_connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		this.m_initialWifiState = this.m_wifiManager.isWifiEnabled();
	}

	/**
     * 
     */
	@Override
	protected void onResume()
	{
		this.m_sharedData.put(BraceletReader.SERVER_ADDRESS, getString(R.string.server_URL));

		/*
		 * Bluetooth initialization
		 */
		if ( this.m_bluetoothManager == null )
		{
			this.m_serviceConnection = new MyServiceConnection();
			bindService(new Intent(this, BluetoothManager.class), this.m_serviceConnection, Context.BIND_AUTO_CREATE);
		}

		if ( !this.m_wifiManager.isWifiEnabled() )
		{
			this.m_wifiManager.setWifiEnabled(true);
		}

		super.onResume();
	}

	/**
     * 
     */
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	/**
     * 
     */
	@Override
	protected void onStop()
	{
		super.onStop();
	}

	/**
     * 
     */
	@Override
	protected void onDestroy()
	{
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
		if ( !this.m_initialBluetoothState )
		{
			this.m_bluetoothManager.disable();
		}
		if ( !this.m_initialWifiState )
		{
			this.m_wifiManager.setWifiEnabled(false);
		}
		unbindService(this.m_serviceConnection);
		stopService(new Intent(this, BluetoothManager.class));
		super.onDestroy();
	}

	/**
	 * 
	 */
	@Override
	public void onActivityResult( int request, int result, Intent intent )
	{
		switch ( request )
		{
		case (REQUEST_PICK_IMAGE):
			if ( result == Activity.RESULT_OK )
			{
				Uri uri = intent.getData();
				setUserImage(uri);
			}
			break;
		}
	}

	/**
	 * Show Bluetooth binding device in case it's already paired
	 */
	public void selectBracelet()
	{
		Set<BluetoothDevice> bondedDevices = this.m_bluetoothManager.getBondedDevices();
		int size = bondedDevices.size();
		this.m_selectedItemsIndexList = new ArrayList<Integer>();
		CharSequence[] names = new CharSequence[size];
		Iterator<BluetoothDevice> iter = bondedDevices.iterator();
		int i = 0;
		while ( iter.hasNext() )
		{
			names[i++] = iter.next().getName();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title);
		builder.setMultiChoiceItems(names, new boolean[size], this);
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.search_other_device, this);
		builder.create().show();
	}

	/**
	 * Connect to a Wifi network and verify Internet connection
	 */
	public void startWifiConnection()
	{
		if ( this.m_braceletListener != null && this.m_sender == null )
		{
			if ( !this.m_wifiManager.isWifiEnabled() )
			{
				this.m_wifiManager.setWifiEnabled(true);
			}

			/* Check Network */
			NetworkInfo networkInfo = this.m_connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if ( networkInfo != null && networkInfo.isConnected() )
			{
				this.m_sender = new Sender(this.m_dataManager, (String) this.m_sharedData.get(BraceletReader.SERVER_ADDRESS));
				this.m_sender.start();
			} else
			{
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				startActivity(intent);
			}
		}
	}

	/**
	 * When the devices are chosen in the dialog
	 */
	@Override
	public void onClick( DialogInterface dialog, int which, boolean isChecked )
	{
		if ( isChecked )
		{
			this.m_selectedItemsIndexList.add(which);
		} else
		{
			this.m_selectedItemsIndexList.remove(Integer.valueOf(which));
		}
	}

	@Override
	public void onClick( DialogInterface dialog, int which )
	{
		if ( which == DialogInterface.BUTTON_POSITIVE )
		{
			if ( this.m_selectedItemsIndexList.size() < 8 )
			{
				Object[] bondedDevices = this.m_bluetoothManager.getBondedDevices().toArray();
				List<BluetoothDevice> checkedDevices = new ArrayList<BluetoothDevice>();
				for ( int i = 0; i < this.m_selectedItemsIndexList.size(); i++ )
				{
					checkedDevices.add((BluetoothDevice) bondedDevices[this.m_selectedItemsIndexList.get(i)]);
				}
				createBluetoothConnections(checkedDevices);
			} else
			{
				Message message = this.m_handler.obtainMessage();
				message.arg1 = R.string.error_max_connection_possible;
				message.sendToTarget();
			}
		} else
		{
			/* Bluetooth setting is opened to search and pair other devices */
			this.m_bluetoothManager.showSettings();
		}
		this.m_selectedItemsIndexList = null;
	}

	/**
	 * Start a new connection with the specified device
	 */
	public void createBluetoothConnections( List<BluetoothDevice> devices )
	{
		if ( this.m_braceletListener != null )
		{
			for ( int i = 0; i < this.m_braceletListener.length; i++ )
			{
				this.m_braceletListener[i].alt();
			}
		}

		this.m_braceletListener = new BraceletListener[devices.size()];
		for ( int i = 0; i < devices.size(); i++ )
		{
			this.m_braceletListener[i] = new BraceletListener(i, this, this.m_dataManager, devices.get(i), UUID.fromString(getString(R.string.UUID)));
			this.m_braceletListener[i].start();
		}
		this.m_sharedData.put(BraceletReader.CONNECTED_BRACELETS, new ArrayList<BluetoothDevice>());
	}

	/**
	 * Signal error
	 */
	public void notifyListenerStateChanged( int id, int state )
	{
		switch ( state )
		{
		case BraceletListener.CONNECTION_ESTABLISHED:
			((List<BluetoothDevice>) this.m_sharedData.get(BraceletReader.CONNECTED_BRACELETS)).add(this.m_braceletListener[id].getDevice());
			break;
		case BraceletListener.ERROR_CREATING_CONNECTION:
			this.m_braceletListener[id].alt();
			Message message = this.m_handler.obtainMessage();
			/*
			 * List<BluetoothDevice> devices = (List<BluetoothDevice>)
			 * this.m_sharedData.get(BraceletReader.CONNECTED_BRACELETS);
			 * devices.remove(this.m_braceletListener[id].getDevice());
			 */
			message.arg1 = R.string.error_not_connected;
			message.sendToTarget();
			break;
		}
	}

	/**
	 * Update the UI
	 * 
	 * data Object is a JSON string
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
	 */
	public void serviceStarted( BluetoothManager bluetoothManager )
	{
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

		public void onServiceConnected( ComponentName className, IBinder binder )
		{
			serviceStarted(((BluetoothManager.MyBinder) binder).getService());
		}

		public void onServiceDisconnected( ComponentName className )
		{
			serviceStopped();
		}
	}
}