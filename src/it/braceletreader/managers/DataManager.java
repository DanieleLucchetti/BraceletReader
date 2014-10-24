package it.braceletreader.managers;

import java.util.ArrayList;

/**
 * 
 * Data structure to contain all data from the bracelet waiting to be send to the server
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class DataManager<T>
{
	ArrayList<T> m_data;

	/**
	 * 
	 */
	public DataManager()
	{
		this.m_data = new ArrayList<T>();
	}

	/**
	 * Add data in the end of list
	 */
	public synchronized void add( T data )
	{
		this.m_data.add(data);
	}

	/**
	 * Return an ArrayList<T> that contains all data
	 */
	public synchronized ArrayList<T> removeAll()
	{
		ArrayList<T> ret = this.m_data;
		this.m_data = new ArrayList<T>();
		return ret;
	}
}