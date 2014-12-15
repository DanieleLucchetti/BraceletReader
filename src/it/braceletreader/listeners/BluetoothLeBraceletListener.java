package it.braceletreader.listeners;

import it.braceletreader.BraceletReader;
import it.braceletreader.Data;
import it.braceletreader.managers.DataManager;

import java.util.Iterator;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

/**
 * 
 * Implementation of BraceletListener for Bluetooth LE devices
 * 
 * \author Lucchetti Daniele
 * 
 */
public class BluetoothLeBraceletListener extends BraceletListener
{
	private BluetoothGattCallback m_callback;		// Callback to receive information by device
	private List<BluetoothLeService> m_services;	// List of Services to active

	/**
	 * Constructor
	 * 
	 * \param id Thread id
	 * \param braceletReader Main class
	 * \param dataManager Data structure to put data
	 * \param device Device to get data
	 * \param services List of services to active
	 */
	public BluetoothLeBraceletListener( int id, BraceletReader braceletReader, DataManager<Data> dataManager, BluetoothDevice device, List<BluetoothLeService> services )
	{
		super(id, braceletReader, dataManager, device);
		this.m_services = services;
	}

	/**
	 * Run implementation
	 */
	@Override
	public void run()
	{
		this.m_callback = new MyBluetoothGattCallback();
		this.m_device.connectGatt(this.m_braceletReader.getApplicationContext(), false, this.m_callback);
	}

	/**
	 * 
	 * My implementation of BluetoothGattCallback to manage callbacks by LE device
	 * 
	 * \author Lucchetti Daniele
	 * 
	 */
	public class MyBluetoothGattCallback extends BluetoothGattCallback
	{
		private Iterator<BluetoothLeService> m_iterator;		// Iterator to access to list of BluetoothLeService
		private boolean m_periodCharacteristic;					// Indicate that it is time to set notification's period

		/**
		 * Called when the connection state change
		 */
		@Override
		public void onConnectionStateChange( BluetoothGatt gatt, int status, int newState )
		{
			this.m_periodCharacteristic = false;
			if ( status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED )
			{
				// If the device is connected, the Services are loaded
				gatt.discoverServices();
			} else if ( status != BluetoothGatt.GATT_SUCCESS )
			{
				gatt.disconnect();
			}
		}

		/**
		 * Called when the Services are loaded
		 */
		@Override
		public void onServicesDiscovered( BluetoothGatt gatt, int status )
		{
			this.m_iterator = m_services.iterator();
			if ( this.m_iterator.hasNext() )
			{
				BluetoothLeService currentService = this.m_iterator.next();
				/* Turn on first sensor */
				writeConfigurationCharacteristic(gatt, currentService);
			}
		}

		/**
		 * Called when a characteristic is written
		 */
		@Override
		public void onCharacteristicWrite( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status )
		{
			if ( this.m_iterator.hasNext() )
			{
				// If not all list is scanned, the scan must be completed
				BluetoothLeService currentService = this.m_iterator.next();
				if ( !this.m_periodCharacteristic )
				{
					// If it is time to turn on sensor, it will be done
					writeConfigurationCharacteristic(gatt, currentService);
				} else
				{
					// If it is time to set notification's period, it will be done
					writePeriodCharacteristic(gatt, currentService);
				}
			} else
			{
				// Otherwise the list of service is scanned from the beginning
				this.m_iterator = m_services.iterator();
				BluetoothLeService service = this.m_iterator.next();
				this.m_periodCharacteristic = !this.m_periodCharacteristic;
				if ( this.m_periodCharacteristic )
				{
					// If the notification's period must be already setted, it will be done
					writePeriodCharacteristic(gatt, service);
				} else
				{
					// Otherwise read characteristic of first Service to enable notifications
					readCharacteristic(gatt, service);
				}
			}
		}

		/**
		 * Called when a characteristic is read
		 */
		@Override
		public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status )
		{
			if ( this.m_iterator.hasNext() )
			{
				// If there is an other characteristic to read, it will be done
				BluetoothLeService service = this.m_iterator.next();
				readCharacteristic(gatt, service);
			} else
			{
				// Otherwise the list of service is scanned from the beginning
				this.m_iterator = m_services.iterator();
				BluetoothLeService service = this.m_iterator.next();
				// Enabling of notification of first sensor
				enablingNotifications(gatt, service);
			}
		}

		/**
		 * Called when a descriptor is written
		 */
		@Override
		public void onDescriptorWrite( BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status )
		{
			if ( this.m_iterator.hasNext() )
			{
				// Until there are Services, notifications are enabled
				BluetoothLeService service = this.m_iterator.next();
				enablingNotifications(gatt, service);
			}
		}

		/**
		 * Called when the value of enabled Services changing
		 */
		@Override
		public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic )
		{
			int i = 0;
			// Search the correct service to which the data appertain
			BluetoothLeService service = m_services.get(i);
			while ( characteristic.getUuid().compareTo(service.getUUIDData()) != 0 )
			{
				service = m_services.get(++i);
			}
			// Construct the Data
			Data data = service.createData(characteristic);

			m_braceletReader.updateUI(data); // To update the UI
			m_dataManager.add(data);
		}

		/**
		 * Write the configuration value of service in configuration characteristic
		 * 
		 * \param gatt Gatt device
		 * \param service Service to active
		 */
		private void writeConfigurationCharacteristic( BluetoothGatt gatt, BluetoothLeService service )
		{
			BluetoothGattService gattService = gatt.getService(service.getUUIDService());
			BluetoothGattCharacteristic config = gattService.getCharacteristic(service.getUUIDConfiguration());
			byte[] value = service.getConfigurationValue();
			config.setValue(value);
			gatt.writeCharacteristic(config);
		}

		/**
		 * Write the period value of service in period characteristic
		 * 
		 * \param gatt Gatt device
		 * \param service Service to active
		 */
		private void writePeriodCharacteristic( BluetoothGatt gatt, BluetoothLeService service )
		{
			BluetoothGattService gattService = gatt.getService(service.getUUIDService());
			BluetoothGattCharacteristic period = gattService.getCharacteristic(service.getUUIDPeriod());
			byte[] value = service.getPeriodValue();
			period.setValue(value);
			gatt.writeCharacteristic(period);
		}

		/**
		 * Read the data characteristic from gatt
		 * 
		 * \param gatt Gatt device
		 * \param service Service to active
		 */
		private void readCharacteristic( BluetoothGatt gatt, BluetoothLeService service )
		{
			BluetoothGattService gattService = gatt.getService(service.getUUIDService());
			BluetoothGattCharacteristic data = gattService.getCharacteristic(service.getUUIDData());
			gatt.readCharacteristic(data);
		}

		/**
		 * Enabled notifications for data characteristic of service
		 * 
		 * \param gatt Gatt device
		 * \param service Service to active
		 */
		private void enablingNotifications( BluetoothGatt gatt, BluetoothLeService service )
		{
			BluetoothGattService gattService = gatt.getService(service.getUUIDService());
			BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(service.getUUIDData());
			/* Local enabling notification */
			gatt.setCharacteristicNotification(gattCharacteristic, true);
			BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(service.getUUIDDescriptor());
			descriptor.setValue(service.getNotificationConfigValue());
			/* Remote enabling notification */
			gatt.writeDescriptor(descriptor);
		}
	};
}