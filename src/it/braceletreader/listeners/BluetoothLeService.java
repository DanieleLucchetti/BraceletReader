package it.braceletreader.listeners;

import it.braceletreader.Data;

import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * 
 * Interface to implement to specify the informations of a service that must be actived
 * 
 * \author Lucchetti Daniele
 * 
 */
public interface BluetoothLeService
{
	/**
	 * Return an identification as String
	 * 
	 * \return Identification
	 */
	public String getIdentification();

	/**
	 * Return the UUID of Service to active
	 * 
	 * \return UUID
	 */
	public UUID getUUIDService();

	/**
	 * Return the UUID of configuration Characteristic
	 * 
	 * \return UUID
	 */
	public UUID getUUIDConfiguration();

	/**
	 * Return the value to write in configuration Characteristic (generally to turn on sensor)
	 * 
	 * \return value
	 */
	public byte[] getConfigurationValue();

	/**
	 * Return the UUID of data Characteristic
	 * 
	 * \return UUID
	 */
	public UUID getUUIDData();

	/**
	 * Return the value to write in data Characteristic for remote enabling of notifications
	 * 
	 * \return value
	 */
	public byte[] getNotificationConfigValue();

	/**
	 * Return the UUID of period Characteristic
	 * 
	 * \return UUID
	 */
	public UUID getUUIDPeriod();

	/**
	 * Return the value to write in period Characteristic
	 * 
	 * \return value
	 */
	public byte[] getPeriodValue();

	/**
	 * Return the UUID of Descriptor of Characteristic
	 * 
	 * \return UUID
	 */
	public UUID getUUIDDescriptor();

	/**
	 * Create a Data containing all information with the correct elaboration
	 * 
	 * \param characteristic The gatt characteristic with the data to elaborate
	 * \return The Data object
	 */
	public Data createData( BluetoothGattCharacteristic characteristic );
}