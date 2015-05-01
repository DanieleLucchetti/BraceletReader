package it.braceletreader;

import it.braceletreader.managers.DataManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	private String m_username;					// The username to send to server
	private boolean m_stop; 					// When is set true, the Thread die

	/**
	 * Constructor
	 */
	public Sender( DataManager<Data> dataManager, String URL, String username )
	{
		this.m_dataManager = dataManager;
		this.m_URL = URL;
		this.m_username = username;
		this.m_stop = false;
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		int id = -1;
		/* Creation HTTP client */
		DefaultHttpClient client = new DefaultHttpClient();

		/* Send a post to register the client */
		HttpPost post = new HttpPost(this.m_URL);
		post.setHeader("Content-type", "application/json");
		try
		{
			post.setEntity(new StringEntity("{\n\"name\":\"" + this.m_username + "\"\n}"));
			HttpResponse response = client.execute(post);

			// Read the response
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String stringResponse = "";
			String line = "";
			while ( (line = br.readLine()) != null )
			{
				stringResponse += line;
			}
			br.close();
			// Getting the id by the response
			JSONObject obj = new JSONObject(stringResponse);
			id = obj.getInt("id");
		} catch ( Exception e )
		{
			alt();
		}
		// If the client is registered, begin to send data
		while ( !this.m_stop )
		{
			/* Get data */
			ArrayList<Data> data = this.m_dataManager.removeAll();
			JSONArray array = null;
			JSONObject obj = null;
			// Transform data to JSON
			try
			{
				array = new JSONArray();
				for ( int i = 0; i < data.size(); i++ )
				{
					array.put(data.get(i).toJSONObject());
				}
				obj = new JSONObject();
				obj.put("id", id);
				obj.put("data", array);
			} catch ( JSONException e1 )
			{
				e1.printStackTrace();
			}

			/* Create request */
			post = new HttpPost(this.m_URL);
			post.setHeader("Content-type", "application/json");
			try
			{
				post.setEntity(new StringEntity(obj.toString()));
				client.execute(post);
				sleep(500);
			} catch ( Exception e )
			{
				e.printStackTrace();
			}
		}

		/* Create disconnect */
		post = new HttpPost(this.m_URL);
		post.setHeader("Content-type", "application/json");
		try
		{
			post.setEntity(new StringEntity("{\n\"name\":\"" + this.m_username + "\",\n\"id\":"+id+"\n}"));
			client.execute(post);
			sleep(500);
		} catch ( Exception e )
		{
			e.printStackTrace();
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