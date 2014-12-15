package it.braceletreader.sensortag;

import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;

import it.braceletreader.Data;
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
	public String getIdentification()
	{
		return "accelerometer";
	}

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
		return UUID.fromString("F000AA13-0451-4000-B000-000000000000");
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
		Integer x = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
		Integer y = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
		Integer z = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 2) * -1;

		double scaledX = x / 64.0;
		double scaledY = y / 64.0;
		double scaledZ = z / 64.0;

		return new Data("accelerometer", System.currentTimeMillis(), scaledX, scaledY, scaledZ);
	}
}