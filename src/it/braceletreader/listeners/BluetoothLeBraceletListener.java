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
	 *
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
	 */
	class MyBluetoothGattCallback extends BluetoothGattCallback
	{
		private Iterator<BluetoothLeService> m_iterator;		// Iterator to access to list of BluetoothLeService

		/**
		 * Called when the connection state change
		 */
		@Override
		public void onConnectionStateChange( BluetoothGatt gatt, int status, int newState )
		{
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
				// If there is an other sensor to active, it will be turned on
				BluetoothLeService currentService = this.m_iterator.next();
				writeConfigurationCharacteristic(gatt, currentService);
			} else
			{
				// Otherwise the list of service is scanned from the beginning
				this.m_iterator = m_services.iterator();
				BluetoothLeService service = this.m_iterator.next();
				// Read characteristic of first Service
				readCharacteristic(gatt, service);
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
		 * Called when the value of Services enabled changing
		 */
		@Override
		public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic )
		{
			Integer x = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
			Integer y = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
			Integer z = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 2) * -1;

			final double scaledX = x / 64.0;
			final double scaledY = y / 64.0;
			final double scaledZ = z / 64.0;

			Data data = new Data();
			data.setTimestamp(System.currentTimeMillis());
			int i = 0;
			BluetoothLeService service;
			while ( i < m_services.size() )
			{
				service = m_services.get(i);
				if ( characteristic.getUuid().equals(service.getUUIDData()) )
				{
					data.setType(service.toString());
					break;
				}
				i++;
			}
			data.setX(scaledX);
			data.setY(scaledY);
			data.setZ(scaledZ);

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