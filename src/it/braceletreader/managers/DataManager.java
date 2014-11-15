package it.braceletreader.managers;

import java.util.ArrayList;

/**
 * 
 * Data structure to contain all data from the bracelets waiting to be send to the server
 * 
 * \author Lucchetti Daniele
 * 
 */
public class DataManager<T>
{
	ArrayList<T> m_data;		// List of data

	/**
	 * Constructor
	 */
	public DataManager()
	{
		this.m_data = new ArrayList<T>();
	}

	/**
	 * Add data in the end of list
	 * 
	 * \param data Data to add
	 */
	public synchronized void add( T data )
	{
		this.m_data.add(data);
	}

	/**
	 * Return an ArrayList<T> that contains all data
	 * 
	 * \return List of containing data
	 */
	public synchronized ArrayList<T> removeAll()
	{
		ArrayList<T> ret = this.m_data;
		this.m_data = new ArrayList<T>();
		return ret;
	}
}