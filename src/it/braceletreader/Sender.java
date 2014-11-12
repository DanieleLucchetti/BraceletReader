package it.braceletreader;

import it.braceletreader.managers.DataManager;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * 
 * Thread that convert the data in DataManager in JSON string and sent it to the HTTP server
 * 
 * \author Lucchetti Daniele
 * 
 */
public class Sender extends Thread
{
	private DataManager<Data> m_dataManager;	// Data structure containing data to send to server
	private String m_URL; 						// Server URL
	private boolean m_stop; 					// When is set true, the Thread die

	/**
	 * Constructor
	 */
	public Sender(DataManager<Data> dataManager, String URL)
	{
		this.m_dataManager = dataManager;
		this.m_URL = URL;
		this.m_stop = false;
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		/* Creation HTTP client */
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post;
		while ( !this.m_stop )
		{
			/* Get data */
			ArrayList<Data> data = this.m_dataManager.removeAll();
			JSONArray array = null;
			try
			{
				array = new JSONArray(data.toString());
			} catch ( JSONException e1 )
			{
				e1.printStackTrace();
			}

			/* Create request */
			post = new HttpPost(this.m_URL);
			post.setHeader("Content-type", "application/json");
			try
			{
				post.setEntity(new StringEntity(array.toString(4)));
				client.execute(post);
				sleep(500);
			} catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stop the Thread
	 */
	public void alt()
	{
		this.m_stop = true;
	}
}