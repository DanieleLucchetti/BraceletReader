package it.braceletreader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * A simple structure to contain a single data
 * 
 * \author Lucchetti Daniele
 * 
 */
public class Data
{
	private String m_type;		// The type of containing data
	private long m_timestamp;	// The timestamp of the detection
	private double m_x;			// X-axis value
	private double m_y;			// Y-axis value
	private double m_z;			// Z-axis value

	/**
	 * Constructor
	 */
	public Data()
	{

	}

	/**
	 * Constructor
	 * 
	 * \param type Type of data
	 * \param timestamp Time of detection
	 * \param x X-axis value
	 * \param y Y-axis value
	 * \param z Z-axis value
	 */
	public Data( String type, long timestamp, double x, double y, double z )
	{
		this.setType(type);
		this.setTimestamp(timestamp);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}

	/**
	 * Return type of containing data
	 * 
	 * \return Type
	 */
	public String getType()
	{
		return m_type;
	}

	/**
	 * Set type of data
	 * 
	 * \param type Type
	 */
	public void setType( String type )
	{
		this.m_type = type;
	}

	/**
	 * Return time of detection
	 * 
	 * \return Time
	 */
	public long getTimestamp()
	{
		return m_timestamp;
	}

	/**
	 * Set the time of detection
	 * 
	 * \param timestamp
	 */
	public void setTimestamp( long timestamp )
	{
		this.m_timestamp = timestamp;
	}

	/**
	 * Return x-axis value
	 * 
	 * \return x-axis value
	 */
	public double getX()
	{
		return m_x;
	}

	/**
	 * Set the x-axis value
	 * 
	 * \param x x-axis value
	 */
	public void setX( double x )
	{
		this.m_x = x;
	}

	/**
	 * Return y-axis value
	 * 
	 * \return y-axis value
	 */
	public double getY()
	{
		return m_y;
	}

	/**
	 * Set the y-axis value
	 * 
	 * \param y y-axis value
	 */
	public void setY( double y )
	{
		this.m_y = y;
	}

	/**
	 * Return z-axis value
	 * 
	 * \return z-axis value
	 */
	public double getZ()
	{
		return m_z;
	}

	/**
	 * Set the z-axis value
	 * 
	 * \param z z-axis value
	 */
	public void setZ( double z )
	{
		this.m_z = z;
	}

	/**
	 * Return a JSONObject containing all data
	 * 
	 * \return The JSONObject
	 */
	public JSONObject toJSONObject()
	{
		JSONObject obj = new JSONObject();
		try
		{
			obj.put("type", this.m_type);
			obj.put("timestamp", this.m_timestamp);
			obj.put("x", this.m_x);
			obj.put("y", this.m_y);
			obj.put("z", this.m_z);
			/*
			 * JSONArray value = new JSONArray();
			 * value.put(this.m_x);
			 * value.put(this.m_y);
			 * value.put(this.m_z);
			 * obj.put("value", value);
			 */
		} catch ( JSONException e )
		{
			e.printStackTrace();
		}
		return obj;
	}
}