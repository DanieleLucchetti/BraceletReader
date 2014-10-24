package it.braceletreader.fragments;

import it.braceletreader.BraceletReader;
import it.braceletreader.Data;
import it.braceletreader.R;
import it.braceletreader.SharedData;

import java.util.Observable;
import java.util.Observer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * Fragment that show the main layout
 * 
 * Author: Lucchetti Daniele
 * 
 */
public class MainFragment extends MyFragment implements Observer
{
	BraceletReader m_braceletReader;
	SharedData m_sharedData;
	ImageView m_imageView;
	TextView m_username;
	TextView m_x_accelerometer;
	TextView m_y_accelerometer;
	TextView m_z_accelerometer;
	TextView m_x_gyroscope;
	TextView m_y_gyroscope;
	TextView m_z_gyroscope;
	Handler m_handler; // Handler to update the UI

	/**
	 * 
	 */
	public MainFragment(BraceletReader braceletReader)
	{
		super(R.layout.main_tab_bracelet_reader);
		this.m_braceletReader = braceletReader;
		this.m_sharedData = SharedData.getInstance();
	}

	/**
	 * 
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);

		/*
		 * The Handler update the UI when it receives a message
		 */
		this.m_handler = new Handler(Looper.getMainLooper())
		{
			@Override
			public void handleMessage( Message inputMessage )
			{
				Data data = (Data) inputMessage.obj;
				if ( data.getType().equals("accelerometer") )
				{
					m_x_accelerometer.setText(String.valueOf(data.getX()).substring(0, 6));
					m_y_accelerometer.setText(String.valueOf(data.getY()).substring(0, 6));
					m_z_accelerometer.setText(String.valueOf(data.getZ()).substring(0, 6));
				} else
				{
					m_x_gyroscope.setText(String.valueOf(data.getX()).substring(0, 6));
					m_y_gyroscope.setText(String.valueOf(data.getY()).substring(0, 6));
					m_z_gyroscope.setText(String.valueOf(data.getZ()).substring(0, 6));
				}
			}
		};
	}

	/**
	 * 
	 */
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		/*
		 * UI initialization
		 */
		this.m_imageView = (ImageView) view.findViewById(R.id.imageview);
		this.m_imageView.setImageBitmap((Bitmap) this.m_sharedData.get(BraceletReader.USER_IMAGE));
		this.m_username = (TextView) view.findViewById(R.id.name_textview);
		this.m_username.setText((String) this.m_sharedData.get(BraceletReader.USERNAME));

		this.m_x_accelerometer = (TextView) view.findViewById(R.id.x_accelerometer_textview);
		this.m_y_accelerometer = (TextView) view.findViewById(R.id.y_accelerometer_textview);
		this.m_z_accelerometer = (TextView) view.findViewById(R.id.z_accelerometer_textview);
		this.m_x_gyroscope = (TextView) view.findViewById(R.id.x_gyroscope_textview);
		this.m_y_gyroscope = (TextView) view.findViewById(R.id.y_gyroscope_textview);
		this.m_z_gyroscope = (TextView) view.findViewById(R.id.z_gyroscope_textview);

		this.m_sharedData.addObserver(this);
		return view;
	}

	/**
	 * 
	 */
	@Override
	public void onDestroyView()
	{
		this.m_sharedData.deleteObserver(this);
		super.onDestroyView();
	}

	/**
	 * Update the UI
	 * 
	 * data Object is a JSON string
	 */
	public void updateUI( Object data )
	{
		Message message = this.m_handler.obtainMessage();
		message.obj = data;
		message.sendToTarget();
	}

	@Override
	public void update( Observable observable, Object data )
	{
		this.m_imageView.setImageBitmap((Bitmap) this.m_sharedData.get(BraceletReader.USER_IMAGE));
		this.m_username.setText((String) this.m_sharedData.get(BraceletReader.USERNAME));
	}
}