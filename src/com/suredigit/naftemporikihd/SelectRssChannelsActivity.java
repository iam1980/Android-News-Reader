package com.suredigit.naftemporikihd;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DragListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;
import com.suredigit.naftemporikihd.MainActivity.LoadChannelsTask;



public class SelectRssChannelsActivity extends SherlockListActivity
{
	private ArrayAdapter<RssChannel> adapter;
	ArrayList<RssChannel> mRssChannels = new ArrayList<RssChannel>();

	private boolean mChangesMade = false;

	private DragSortListView.DropListener onDrop =
			new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				DragSortListView list = getListView();
				RssChannel item = adapter.getItem(from);
				adapter.remove(item);
				adapter.insert(item, to);
				list.moveCheckState(from, to);
				
				mChangesMade = true;

				//				SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
				//				Gson gson = new Gson();
				//				String json = gson.toJson(mRssChannels); 
				//				editor.putString("channels",json);
				//				editor.commit();
			}
		}
	};

	//    private DragListener onDrag = 
	//    		new DragListener() {
	//
	//				@Override
	//				public void drag(int from, int to) {
	//					// TODO Auto-generated method stub
	//					System.out.println("drag detected");
	//					
	//				}
	//    	
	//    };

	//	private RemoveListener onRemove =
	//			new DragSortListView.RemoveListener() {
	//		@Override
	//		public void remove(int which) {
	//			DragSortListView list = getListView();
	//			String item = adapter.getItem(which);
	//			adapter.remove(item);
	//			list.removeCheckState(which);
	//		}
	//	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkable_main);

		//		mRssChannels.add(new RssChannel("Οικονομία & Αγορές" , "/api/legacy/android/GetNews.aspx?&cat=1"));
		//		mRssChannels.add(new RssChannel("Πολιτική","/api/legacy/android/GetNews.aspx?&cat=2"));		
		//		mRssChannels.add(new RssChannel("Κοινωνία" , "/api/legacy/android/GetNews.aspx?&cat=3"));		
		//		mRssChannels.add(new RssChannel("Κόσμος" , "/api/legacy/android/GetNews.aspx?&cat=4"));
		//		mRssChannels.add(new RssChannel("Αθλητικά", "/api/legacy/android/GetNews.aspx?&cat=5"));
		//		mRssChannels.add(new RssChannel("Πολιτισμός", "/api/legacy/android/GetNews.aspx?&cat=6"));
		//		mRssChannels.add(new RssChannel("Περιβάλλον","/api/legacy/android/GetNews.aspx?&cat=8"));
		//		mRssChannels.add(new RssChannel("Τεχνολογία - Επιστήμη", "/api/legacy/android/GetNews.aspx?&cat=7"));	
		//
		//		mRssChannels.get(1).setEnabled(false);

		SharedPreferences preferences = Singleton.getInstance().prefs;
		//		ArrayList<RssChannel> defaultRssChannels = new ArrayList<RssChannel>();
		//		defaultRssChannels.add(new RssChannel("Οικονομία & Αγορές",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=1"));
		//		defaultRssChannels.add(new RssChannel("Πολιτική",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=2"));		
		//		defaultRssChannels.add(new RssChannel("Κοινωνία",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=3"));		
		//		defaultRssChannels.add(new RssChannel("Κόσμος",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=4"));
		//		defaultRssChannels.add(new RssChannel("Αθλητικά",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=5"));
		//		defaultRssChannels.add(new RssChannel("Πολιτισμός",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=6"));
		//		defaultRssChannels.add(new RssChannel("Περιβάλλον",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=8"));
		//		defaultRssChannels.add(new RssChannel("Τεχνολογία - Επιστήμη",MainActivity.BASEURL + "/api/legacy/android/GetNews.aspx?&cat=7"));
		//		Gson gson = new Gson();
		//		String jsonDef = gson.toJson(defaultRssChannels); 

		//		Gson gson = new Gson();
		//		String json = preferences.getString("channels","");
		//		Type listType = new TypeToken<ArrayList<RssChannel>>() {}.getType();
		//		mRssChannels = gson.fromJson(json,listType);

		//mRssChannels = MainActivity.loadChannelsFromFile();
		mRssChannels = MainActivity.mRssChannels;
		//new LoadChannelsTask().execute();

		adapter = new ChannelAdapter(this, R.layout.list_item_checkable, mRssChannels);
		setListAdapter(adapter);

		DragSortListView list = getListView();
		//list.setDragEnabled(true);
		//list.setDragListener(onDrag);
		list.setDropListener(onDrop);
		//		list.setRemoveListener(onRemove);

//		Thread thread = new Thread()
//		{
//			private ArrayList<RssChannel> myChans = null;
//			@Override
//			public void run() {
//				try {
//					mRssChannels = MainActivity.loadChannelsFromFile();
//					myChans = mRssChannels;
//				} finally {
//					System.out.println("LOADED");
//					//adapter.clear();
//					//adapter.add(result.get(0));
//
//					runOnUiThread(new Runnable() {
//						public void run() {
//
//							adapter.clear();
//							
//							if(mRssChannels !=null ){
//								for(RssChannel myChan:mRssChannels){
//									adapter.add(myChan);
//								}
//							}
//							adapter.notifyDataSetChanged();
//							mRssChannels = myChans;
//						}
//					});
//				}
//			}
//		};
//		thread.start();

	}

//	class LoadChannelsTask extends AsyncTask<Void, Void, ArrayList<RssChannel>> {
//		@Override
//		protected ArrayList<RssChannel> doInBackground(Void... params) {
//			// TODO Auto-generated method stub
//			return MainActivity.loadChannelsFromFile();
//		}
//		@Override
//		protected void onPostExecute(ArrayList<RssChannel> result){
//			mRssChannels = result;
//			System.out.println("Loaded " + mRssChannels.size());
//			adapter.clear();
//			adapter.add(result.get(0));
//			adapter.notifyDataSetChanged();
//
//
//		}
//	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		RssChannel chan =(RssChannel)getListView().getItemAtPosition(position);
		if(chan.isEnabled())
			chan.setEnabled(false);
		else chan.setEnabled(true);

		mChangesMade = true;

		//		SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
		//		Gson gson = new Gson();
		//		String json = gson.toJson(mRssChannels); 
		//		editor.putString("channels",json);
		//		editor.commit();
	}



	@Override
	public DragSortListView getListView() {
		return (DragSortListView) super.getListView();
	}

	private class ChannelAdapter extends ArrayAdapter<RssChannel> {

		private ArrayList<RssChannel> channels;

		public ChannelAdapter(Context context, int textViewResourceId,
				ArrayList<RssChannel> objects) {
			super(context, textViewResourceId, objects);
			this.channels = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item_checkable, parent,false);
			}
			RssChannel chan = channels.get(position);
			if (chan != null) {
				CheckedTextView cTv = (CheckedTextView) v.findViewById(R.id.text);
				cTv.setText(chan.getTitle());
				if (chan.isEnabled()) 
					getListView().setItemChecked(position, true);

			}
			return v;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			//finish();

			if(mChangesMade){
				saveChanges();
				NavUtils.navigateUpFromSameTask(this);
			} else 
				finish();

			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if(mChangesMade){
			saveChanges();
			NavUtils.navigateUpFromSameTask(this);
		} else 
			finish();
	}

	private void saveChanges(){
		//		SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
		//		Gson gson = new Gson();
		//		String json = gson.toJson(mRssChannels); 
		//		editor.putString("channels",json);
		//		editor.commit();	
		MainActivity.saveChannelsToFile(mRssChannels);
	}
}