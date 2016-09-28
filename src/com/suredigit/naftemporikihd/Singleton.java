package com.suredigit.naftemporikihd;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class Singleton extends Application {
	private static Singleton m_Instance;
	private static final String TAG = "Singleton";
	private static final String PRJNAME = "NaftemporikiHD";
	
	public static String PREFS_NAME = Singleton.PRJNAME + "_PREFS";
	public static SharedPreferences prefs	= null;
	public static SharedPreferences.Editor editor = null;
	public Singleton() {
		super();
		m_Instance = this;
		//	
	}
	// Double-checked singleton fetching
	public static Singleton getInstance() {
		// init instance 
		if(m_Instance == null) {
			synchronized(Singleton.class) {
				if(m_Instance == null) new Singleton();
			}
		}
		if (prefs == null) {
			prefs = m_Instance.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			
		}
	 
		return m_Instance;
	}
	 	
}
