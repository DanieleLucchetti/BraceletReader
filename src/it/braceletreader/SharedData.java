package it.braceletreader;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * 
 * Class created to contain shared data with a String key and an Object value
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class SharedData extends Observable
{
	private static final SharedData m_instance = new SharedData();
	private Map<String, Object> m_data;

	private SharedData()
	{
		m_data = new HashMap<String, Object>();
	}

	public void put( String key, Object value )
	{
		this.m_data.put(key, value);
		setChanged();
		notifyObservers();
	}

	public Object get( String key )
	{
		return this.m_data.get(key);
	}

	public void remove( String key )
	{
		this.m_data.remove(key);
		setChanged();
		notifyObservers();
	}

	public static SharedData getInstance()
	{
		return m_instance;
	}
}