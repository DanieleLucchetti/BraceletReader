package it.braceletreader.sensortag;

import java.util.UUID;

import it.braceletreader.listeners.BluetoothLeService;

/**
 * 
 * Information to obtain data from accelerometer of SensorTag
 * 
 * \author Lucchetti Daniele
 * 
 */
public class SensorTagAccelerometer implements BluetoothLeService
{

	@Override
	public UUID getUUIDService()
	{
		return UUID.fromString("F000AA10-0451-4000-B000-000000000000");
	}

	@Override
	public UUID getUUIDConfiguration()
	{
		return UUID.fromString("F000AA12-0451-4000-B000-000000000000");
	}

	@Override
	public byte[] getConfigurationValue()
	{
		byte[] value = { 0x01 };
		return value;
	}

	@Override
	public UUID getUUIDData()
	{
		return UUID.fromString("F000AA11-0451-4000-B000-000000000000");
	}

	@Override
	public byte[] getNotificationConfigValue()
	{
		byte[] value = { 0x01, 0x00 };
		return value;
	}

	@Override
	public UUID getUUIDPeriod()
	{
		return null;
	}

	@Override
	public byte getPeriodValue()
	{
		return 0;
	}

	@Override
	public UUID getUUIDDescriptor()
	{
		return UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
	}

	@Override
	public String toString()
	{
		return "accelerometer";
	}
}