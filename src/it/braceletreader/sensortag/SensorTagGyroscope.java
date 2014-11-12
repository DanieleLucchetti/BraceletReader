package it.braceletreader.sensortag;

import it.braceletreader.listeners.BluetoothLeService;

import java.util.UUID;

/**
 * 
 * Information to obtain data from gyroscope of SensorTag
 * 
 * \author Lucchetti Daniele
 * 
 */
public class SensorTagGyroscope implements BluetoothLeService
{

	@Override
	public UUID getUUIDService()
	{
		return UUID.fromString("F000AA50-0451-4000-B000-000000000000");
	}

	@Override
	public UUID getUUIDConfiguration()
	{
		return UUID.fromString("F000AA52-0451-4000-B000-000000000000");
	}

	@Override
	public byte[] getConfigurationValue()
	{
		byte[] value = { 0x07 };
		return value;
	}

	@Override
	public UUID getUUIDData()
	{
		return UUID.fromString("F000AA51-0451-4000-B000-000000000000");
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
		return "gyroscope";
	}
}