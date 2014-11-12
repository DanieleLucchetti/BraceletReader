package it.braceletreader.fragments;

import it.braceletreader.R;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * My implementation of Fragment to navigate from tab
 * 
 * \author Lucchetti Daniele
 * 
 */
public class MyFragment extends Fragment implements android.app.ActionBar.TabListener
{
	private int m_layout;	// ID of layout to show

	/**
	 * Constructor
	 * 
	 * \param layoutID ID of layout to show
	 */
	public MyFragment( int layoutID )
	{
		super();
		this.m_layout = layoutID;
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		/* Inflate the layout for this fragment */
		return inflater.inflate(this.m_layout, container, false);
	}

	/**
	 * Called when a Tab is reselected
	 */
	@Override
	public void onTabReselected( Tab tab, FragmentTransaction ft )
	{

	}

	/**
	 * Called when a Tab is selected
	 */
	@Override
	public void onTabSelected( Tab tab, FragmentTransaction ft )
	{
		ft.replace(R.id.fragment_container, this);
	}

	/**
	 * Called when a Tab is unselected
	 */
	@Override
	public void onTabUnselected( Tab tab, FragmentTransaction ft )
	{
		ft.remove(this);
	}
}