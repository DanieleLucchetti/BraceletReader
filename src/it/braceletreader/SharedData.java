package it.braceletreader;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * 
 * Class singleton created to contain shared data with a String key and an Object value
 * 
 * \author Lucchetti Daniele
 * 
 */
public class SharedData extends Observable
{
	private static final SharedData m_instance = new SharedData(); 	// Instance
	private Map<String, Object> m_data; 							// Map to contain data

	/**
	 * Constructor
	 */
	private SharedData()
	{
		m_data = new HashMap<String, Object>();
	}

	/**
	 * Map value in data structure with the specified key
	 * 
	 * \param key The key
	 * \param value The value
	 */
	public void put( String key, Object value )
	{
		this.m_data.put(key, value);
		// Notify to Observers
		setChanged();
		notifyObservers();
	}

	/**
	 * Return the value corresponding the specified key
	 * 
	 * \param key The key
	 * \return The object mapped with the specified key or null if it does not exist
	 */
	public Object get( String key )
	{
		return this.m_data.get(key);
	}

	/**
	 * Remove the object mapped with the specified key
	 * 
	 * \param key The key of the object we want delete
	 */
	public void remove( String key )
	{
		this.m_data.remove(key);
		// Notify to Observers
		setChanged();
		notifyObservers();
	}

	/**
	 * Return the only instance of SharedData
	 * 
	 * \return The instance
	 */
	public static SharedData getInstance()
	{
		return m_instance;
	}
}