package it.braceletreader;

/*
 * 
 * A simple structure to contain a single data
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class Data
{
	private String m_type;
	private long m_timestamp;
	private double m_x;
	private double m_y;
	private double m_z;
	
	public Data(String type, long timestamp, double x, double y, double z)
	{
		this.setType(type);
		this.setTimestamp(timestamp);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}

	public String getType()
	{
		return m_type;
	}

	public void setType( String m_type )
	{
		this.m_type = m_type;
	}

	public long getTimestamp()
	{
		return m_timestamp;
	}

	public void setTimestamp( long m_timestamp )
	{
		this.m_timestamp = m_timestamp;
	}

	public double getX()
	{
		return m_x;
	}

	public void setX( double x )
	{
		this.m_x = x;
	}

	public double getY()
	{
		return m_y;
	}

	public void setY( double y )
	{
		this.m_y = y;
	}

	public double getZ()
	{
		return m_z;
	}

	public void setZ( double z )
	{
		this.m_z = z;
	}
	
	@Override
	public String toString()
	{
		return "{\"type\":\""+m_type+"\",\"timestamp\":"+m_timestamp+",\"value\":["+m_x+","+m_y+","+m_z+"]}";
	}
}