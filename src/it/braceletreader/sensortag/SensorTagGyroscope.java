package it.braceletreader.sensortag;

import it.braceletreader.Data;
import it.braceletreader.listeners.BluetoothLeService;

import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;

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
	public String getIdentification()
	{
		return "gyroscope";
	}

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
		return UUID.fromString("F000AA53-0451-4000-B000-000000000000");
	}

	@Override
	public byte[] getPeriodValue()
	{
		byte[] value = { 0x0A };
		return value;
	}

	@Override
	public UUID getUUIDDescriptor()
	{
		return UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
	}

	@Override
	public Data createData( BluetoothGattCharacteristic characteristic )
	{
		float y = shortSignedAtOffset(characteristic, 0) * (500f / 65536f) * -1;
		float x = shortSignedAtOffset(characteristic, 2) * (500f / 65536f);
		float z = shortSignedAtOffset(characteristic, 4) * (500f / 65536f);

		return new Data("gyroscope", System.currentTimeMillis(), x, y, z);
	}

	private static Integer shortSignedAtOffset( BluetoothGattCharacteristic characteristic, int offset )
	{
		Integer lowerByte = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
		Integer upperByte = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1);

		return (upperByte << 8) + lowerByte;
	}
}