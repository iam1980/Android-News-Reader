package com.suredigit.naftemporikihd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class MainActivity extends SherlockActivity  {

	//protected static final String VERSION = "1.0.3";
	private static final String TAG = "NaftermporikiHD";
	public static final String BASEURL = "http://www.naftemporiki.gr";
	public static final String NEWSFILENAME = "new_file.xml";
	public static final int SECONDSTOREFRESH = 600;

	public static ArrayList<RssChannel> mRssChannels;
	private ArrayList<RssHtmlDownloaderTask> mRssHtmlDownloaderTasks;

	private LinearLayout myLinList;

	public static final ImageDownloader imageDownloader = new ImageDownloader();

	ProgressDialog mProgressDialog;

	public static boolean pNightMode;
	public static int pFontSize;
	public static boolean pCapitalizedCategories;

	private ChangeLog cl;

	public static enum MemSize { LOW, MED, HIGH , ULTRA };
	public static MemSize MEMSIZE = MemSize.HIGH;

	//This is used in ArticlesViewActivity for navigation purposes
	public static String[] mRssTitles;

	public static void loadPreferences() {	
		SharedPreferences preferences = Singleton.getInstance().prefs;
		pNightMode = preferences.getBoolean("nightmode", false);
		pFontSize = preferences.getInt("fontsize",18);
		pCapitalizedCategories = preferences.getBoolean("cs_capitlisecats", false);

	}

	public static void savePreferences() {
		SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
		editor.putBoolean("nightmode", pNightMode);
		editor.putInt("fontsize", pFontSize);
		editor.putBoolean("cs_capitlisecats", pCapitalizedCategories);
		editor.commit();
	}

	public static void updateLastFetch(){
		SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
		long timestamp = System.currentTimeMillis()/1000;
		editor.putLong("lastfetch", timestamp);	
		editor.commit();
	}

	public static long getLastFetch(){
		SharedPreferences preferences = Singleton.getInstance().prefs;
		return preferences.getLong("lastfetch", 0);
	}

	//	public static void saveChannels(){
	//		SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
	//		Gson gson = new Gson();
	//		String json = gson.toJson(mRssChannels); 
	//		editor.putString("channels",json);
	//		editor.commit();
	//	}

	public static void saveChannelsToFile(ArrayList<RssChannel> rssChans){
		Gson gson = new Gson();
		String json = gson.toJson(rssChans);

		FileOutputStream fos;
		try {
			fos = Singleton.getInstance().openFileOutput(NEWSFILENAME, Context.MODE_PRIVATE);
			fos.write(json.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<String> rssChannelTitles = new ArrayList<String>();
		for (RssChannel myChan : rssChans){
			if(myChan.isEnabled())
				rssChannelTitles.add(myChan.getTitle());
		}
		mRssTitles = new String[rssChannelTitles.size()];
		rssChannelTitles.toArray(mRssTitles);		

	}

	public static ArrayList<RssChannel> loadChannelsFromFile(){

		ArrayList<RssChannel> defaultRssChannels = new ArrayList<RssChannel>();

		defaultRssChannels.add(new RssChannel("–œÀ…‘… «",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=2"));		
		defaultRssChannels.add(new RssChannel(" œ…ÕŸÕ…¡",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=3"));		
		defaultRssChannels.add(new RssChannel(" œ”Ãœ”",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=4"));
		defaultRssChannels.add(new RssChannel("œ… œÕœÃ…¡ & ¡√œ—≈”",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=1"));
		defaultRssChannels.add(new RssChannel("¡»À«‘… ¡",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=5"));
		defaultRssChannels.add(new RssChannel("–œÀ…‘…”Ãœ”",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=6"));
		defaultRssChannels.add(new RssChannel("–≈—…¬¡ÀÀœÕ",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=8"));
		defaultRssChannels.add(new RssChannel("‘≈◊ÕœÀœ√…¡ - ≈–…”‘«Ã«",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=7"));
		//		Gson gson = new Gson();
		//		String jsonDef = gson.toJson(defaultRssChannels); 

		FileInputStream fis;
		boolean flag = false;
		StringBuilder sb = null;
		try {
			fis = Singleton.getInstance().openFileInput(NEWSFILENAME);
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			sb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			fis.close();
			if((sb.toString().equalsIgnoreCase("null")) || (sb == null)){
				flag = false;
			} else flag = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<RssChannel> theReturnList;

		Type listType = new TypeToken<ArrayList<RssChannel>>() {}.getType();
		if (flag) {
			Gson gson = new Gson();
			theReturnList =  gson.fromJson(sb.toString(),listType);
		} else {
			theReturnList =  defaultRssChannels;
		}

		//CHANGESET 1.0.3
		if(!(pCapitalizedCategories)){
			for (RssChannel myChan : theReturnList){
				
				String oldTitle = myChan.getTitle().toUpperCase();
				
				oldTitle = oldTitle.replaceAll("\u0386", "¡");
				oldTitle = oldTitle.replaceAll("\u0388", "E");
				oldTitle = oldTitle.replaceAll("\u0389", "H");
				oldTitle = oldTitle.replaceAll("\u038a", "I");
				oldTitle = oldTitle.replaceAll("\u038c", "O");
				oldTitle = oldTitle.replaceAll("\u038e", "Y");
				oldTitle = oldTitle.replaceAll("\u038f", "Ÿ");

				myChan.setTitle(oldTitle);
			}
			pCapitalizedCategories = true;
			savePreferences();
		}
		//

		ArrayList<String> rssChannelTitles = new ArrayList<String>();
		for (RssChannel myChan : theReturnList){
			if(myChan.isEnabled())
				rssChannelTitles.add(myChan.getTitle());
		}
		mRssTitles = new String[rssChannelTitles.size()];
		rssChannelTitles.toArray(mRssTitles);

		return theReturnList;
	}

	//	public static void loadChannels(){
	//		SharedPreferences preferences = Singleton.getInstance().prefs;
	//		ArrayList<RssChannel> defaultRssChannels = new ArrayList<RssChannel>();
	//		defaultRssChannels.add(new RssChannel("œÈÍÔÌÔÏﬂ· & ¡„ÔÒ›Ú",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=1"));
	//		defaultRssChannels.add(new RssChannel("–ÔÎÈÙÈÍﬁ",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=2"));		
	//		defaultRssChannels.add(new RssChannel(" ÔÈÌ˘Ìﬂ·",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=3"));		
	//		defaultRssChannels.add(new RssChannel(" ¸ÛÏÔÚ",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=4"));
	//		defaultRssChannels.add(new RssChannel("¡ËÎÁÙÈÍ‹",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=5"));
	//		defaultRssChannels.add(new RssChannel("–ÔÎÈÙÈÛÏ¸Ú",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=6"));
	//		defaultRssChannels.add(new RssChannel("–ÂÒÈ‚‹ÎÎÔÌ",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=8"));
	//		defaultRssChannels.add(new RssChannel("‘Â˜ÌÔÎÔ„ﬂ· - ≈ÈÛÙﬁÏÁ",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=7"));
	//		Gson gson = new Gson();
	//		String jsonDef = gson.toJson(defaultRssChannels); 
	//
	//		String json = preferences.getString("channels",jsonDef);
	//		Type listType = new TypeToken<ArrayList<RssChannel>>() {}.getType();
	//		mRssChannels = gson.fromJson(json,listType);
	//	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = this.getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onResume(){
		super.onResume();
		imageDownloader.setMemSize(MemSize.ULTRA);

	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}	

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		if (maxMemory < 21000) MEMSIZE = MemSize.LOW;
		if (maxMemory > 21001) MEMSIZE = MemSize.MED;
		if (maxMemory > 30000) MEMSIZE = MemSize.HIGH;
		if (maxMemory > 42000) MEMSIZE = MemSize.ULTRA;

		imageDownloader.setMemSize(MemSize.ULTRA);
		setContentView(R.layout.activity_main);
		myLinList =(LinearLayout) findViewById(R.id.LinearLayout1);

		loadPreferences();

		//mRssChannels = loadChannelsFromFile();
		new LoadChannelsTask().execute();

		cl = new ChangeLog(this);
		if (cl.firstRun())
			cl.getLogDialog().show();


		//int memClass = ( ( ActivityManager )context.getSystemService( Context.ACTIVITY_SERVICE ) ).getMemoryClass();
		//int cacheSize = 1024 * 1024 * memClass / 8;
	}

	class LoadChannelsTask extends AsyncTask<Void, Void, ArrayList<RssChannel>> {
		@Override
		protected ArrayList<RssChannel> doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return loadChannelsFromFile();
		}
		@Override
		protected void onPostExecute(ArrayList<RssChannel> result){
			mRssChannels = result;
			refreshOrBuildUI();
		}
	}


	public void refreshOrBuildUI(){
		if (getLastFetch() + SECONDSTOREFRESH < (System.currentTimeMillis()/1000)){
			refreshChannels();
		} else {
			buildUI();
			updateThumbnails();
		}		
	}

	@SuppressLint("NewApi")
	private void refreshChannels(){
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setMessage("ÃÂÙ·ˆ˛ÒÙ˘ÛÁ ≈È‰ﬁÛÂ˘Ì");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMax(getEnabledChanCount());
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);



		mRssHtmlDownloaderTasks = new ArrayList<RssHtmlDownloaderTask>();
		for (RssChannel theChan : mRssChannels){
			if (theChan.isEnabled()){
				if(theChan.getArticles() != null) theChan.getArticles().clear();
				RssHtmlDownloaderTask task = new RssHtmlDownloaderTask(theChan);	
				if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,theChan.getUrl());
				} else {
					task.execute(theChan.getUrl());
				}
				mRssHtmlDownloaderTasks.add(task);
			}
		}
		updateLastFetch();	
	}

	private void parseChannels(){
		for (RssHtmlDownloaderTask theTask: mRssHtmlDownloaderTasks){
			theTask = null;
		}
		mRssHtmlDownloaderTasks = null;

		//*** - PARSE XML START		
		for (RssChannel theChan : mRssChannels){
			if(!(theChan.getHtml() == null)){
				try {
					Document doc = NaftemporikiParsers.parseXML(theChan.getHtml());
					theChan.setArticles(NaftemporikiParsers.populateArticles(doc));
					theChan.setHtml(null);


				} catch (Exception e) {
					e.printStackTrace();
				} 
				if (!(theChan.getArticles() == null)){
					Collections.reverse((ArrayList<Article>) theChan.getArticles());
					theChan.getArticles().add(new Article("DUMMY"));
				}
			}
		}
		//*** - PARSE XML END

	}

	private void buildUI(){

		//*** - BUILD HORIZONTALLISTVIEWS START
		if(((LinearLayout) myLinList).getChildCount() > 0) 
			((LinearLayout) myLinList).removeAllViews(); 
		if (mRssChannels != null){
			for (RssChannel theChan : mRssChannels){
				if ((!(theChan.getArticles() == null)) && theChan.isEnabled()){
					final RssChannel myChan = theChan;
					HorizontalListView hListView = new HorizontalListView(this,null);
					ArticleAdapter myAdapter = new ArticleAdapter(this, R.layout.horizontal_list_item, theChan.getArticles());
					hListView.setAdapter(myAdapter);

					hListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
					LayoutParams hLparams = hListView.getLayoutParams();
					hLparams.height  = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
					hListView.setLayoutParams(hLparams);

					TextView channTitleTV = new TextView(this);
					//channTitleTV.setText(theChan.getTitle());
					channTitleTV.setText(theChan.getTitle().toUpperCase());
					channTitleTV.setTextAppearance(this, android.R.style.TextAppearance_Medium);
					channTitleTV.setTextSize(14);
					channTitleTV.setTypeface(null,Typeface.BOLD);
					LinearLayout.LayoutParams channTitleTVparams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					channTitleTVparams.setMargins(6, 2, 0, 8); //substitute parameters for left, top, right, bottom
					//channTitleTVparams.setMargins(6, 8, 0, 8); //substitute parameters for left, top, right, bottom
					channTitleTV.setLayoutParams(channTitleTVparams);

					TextView spacerTV = new TextView(this);
					//					spacerTV.setTextAppearance(this, android.R.style.TextAppearance_Small);
					spacerTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 5);

					myLinList.addView(channTitleTV);
					myLinList.addView(hListView);
					myLinList.addView(spacerTV);

					hListView.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							// TODO Auto-generated method stub
							//Log.i(TAG,"Clicked");
							//							Log.i(TAG,v.getClass().getName());
							//							if (v instanceof RelativeLayout){
							//								TextView tvTitle = (TextView)((RelativeLayout) v).getChildAt(1);
							//								if (tvTitle != null)
							//									tvTitle.setBackgroundColor(Color.RED);
							//							}

							v.playSoundEffect(SoundEffectConstants.CLICK);
							//Article clickedArticle = (Article)parent.getItemAtPosition(position);
							Intent intent = new Intent(MainActivity.this, ArticlesViewActivity.class);
							intent.putExtra("parcel", myChan);
							intent.putExtra("position", position);
							intent.putExtra("chanPos", getPositionInEnabledChannels(myChan));
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
							startActivity(intent);
							//Log.i(TAG,"Sent Activity");
							//overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
						}}
							);


				}
			}}
		saveChannelsToFile(mRssChannels);
	}

	private void updateThumbnails(){

		int childcount = myLinList.getChildCount();
		for (int i=0; i < childcount; i++){
			View v = myLinList.getChildAt(i);
			if(v instanceof HorizontalListView){	
				ArticleAdapter theAdapter = (ArticleAdapter)((HorizontalListView) v).getAdapter();
				theAdapter.notifyDataSetChanged();
			}
		}
	}

	private class ArticleAdapter extends ArrayAdapter<Article> {

		private ArrayList<Article> articles;
		private Rect rect;

		public ArticleAdapter(Context context, int textViewResourceId, ArrayList<Article> objects) {
			super(context, textViewResourceId, objects);
			this.articles = objects;
		}

		@Override  
		public View getView(int position, View convertView, ViewGroup parent) {  
			Article article = articles.get(position);

			if (!(article.isDummy())){
				View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_list_item, null);  
				final TextView title = (TextView) retval.findViewById(R.id.title);  
				final ImageView thumb = (ImageView) retval.findViewById(R.id.image);

				thumb.setOnTouchListener(new OnTouchListener()
				{
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {


						case MotionEvent.ACTION_DOWN: {
							title.setBackgroundColor(Color.parseColor("#950005"));
							rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
							break;
						}

						case MotionEvent.ACTION_MOVE:{
							//							Rect rect = new Rect();
							//							rect.left = thumb.getLeft();
							//							rect.top = thumb.getTop();
							//							rect.bottom = thumb.getBottom();
							//							rect.right = thumb.getRight();
							//							System.out.println(rect.left+","+rect.top);



							if(!rect.contains((int)event.getX(), (int)event.getY())) {
								// Outside the bounds
								title.setBackgroundColor(Color.parseColor("#CC000000"));
							}
							break;        
						}

						case MotionEvent.ACTION_CANCEL: {
							title.setBackgroundColor(Color.parseColor("#CC000000"));
							break;
						}

						case MotionEvent.ACTION_UP: {
							title.setBackgroundColor(Color.parseColor("#CC000000"));
							break;
						}
						}
						return true;
					}
				});



				title.setText(article.getTitle());  

				if(!(article.getThumbUrl() == null)){
					thumb.setBackgroundColor(Color.BLACK);
					imageDownloader.download(article.getThumbUrl(), (ImageView) thumb);
					int pxX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
					int pxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());				
					thumb.getLayoutParams().height = pxX;
					thumb.getLayoutParams().width = pxY;
					title.getLayoutParams().width = pxX;

				}

				return retval;  
			} else {
				View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_list_dummy, null); 
				return retval;
			}
		}  		

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case R.id.menu_rss:

			Intent myIntent = new Intent(MainActivity.this, SelectRssChannelsActivity.class);
			MainActivity.this.startActivity(myIntent);

			return true;
		case R.id.menu_refresh:
			refreshChannels();

			return true;			
		case R.id.menu_about:
			Intent intent = new Intent(MainActivity.this, AboutFullscreenActivity.class);
			startActivity(intent);

			return true;

		case R.id.menu_changelog:
			cl.getFullLogDialog().show();
			return true;

		case R.id.menu_rate:
			System.out.println(MainActivity.this.getPackageName());
			Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			try {
				startActivity(goToMarket);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(MainActivity.this, "Couldn't launch the market", Toast.LENGTH_LONG).show();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);	
		return true;
	}

	class RssHtmlDownloaderTask extends AsyncTask<String, Void, String> {
		private String url;
		private RssChannel channel;
		//private final WeakReference<ImageView> imageViewReference;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mProgressDialog.show();
		}		

		public RssHtmlDownloaderTask(RssChannel chan) {
			this.channel = chan;
		}

		/**
		 * Actual download method.
		 */
		@Override
		protected String doInBackground(String... params) {
			url = params[0];
			return downloadHtml(url);
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(String theHtml) {
			if (isCancelled()) {
				channel.setHtml(null);
			}

			if (theHtml != null) {
				channel.setHtml(theHtml);
				int myProg = mProgressDialog.getProgress();
				myProg++;
				mProgressDialog.setProgress(myProg);
				//buildUI();
			}

			int unfinishedTasks = 0;
			for (RssHtmlDownloaderTask myDT : mRssHtmlDownloaderTasks){
				if(!(myDT.getStatus() == AsyncTask.Status.FINISHED)){
					unfinishedTasks++;
				}
			}
			if (unfinishedTasks == 1){
				//We are all done. 1 Because its the current one that hasnt finished post execute
				parseChannels();
				buildUI();
				updateThumbnails();
				//mProgressDialog.dismiss();
				try {
					mProgressDialog.dismiss();
					//dialog = null;
				} catch (Exception e) {
					// nothing
				}

				//prefetchArticleHtml();

			}			
		}
	}

	String downloadHtml(String url) {
		final HttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(url);
		//Log.w(TAG, "starte d d/ling" + url); 
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) { 
				Log.w(TAG, "Error " + statusCode + " while downloading HTML from " + url); 
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {

					inputStream = entity.getContent(); 
					final String html = convertStreamToString(inputStream);
					//Log.w(TAG, "finished d/ling" + url); 
					return html;
				} finally {
					if (inputStream != null) {
						inputStream.close();  
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			// Could provide a more explicit error message for IOException or IllegalStateException
			getRequest.abort();
			Log.w("ImageDownloader", "Error while retrieving bitmap from " + url + e.toString());
		} 
		return null;
	}

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();

	}

	public static int getPositionInEnabledChannels(RssChannel myChan){
		int i = 0;
		String myTitle = myChan.getTitle();
		for (RssChannel theChan: mRssChannels){
			if (theChan.isEnabled()){
				if (theChan.getTitle().equalsIgnoreCase(myTitle))
					break;
				i++;
			}
		}

		return i;
	}

	public static int getPositionInEnabledChannelsByTitle(String title){
		int i = 0;
		String myTitle = title;
		for (RssChannel theChan: mRssChannels){
			if (theChan.isEnabled()){
				if (theChan.getTitle().equalsIgnoreCase(myTitle))
					break;
				i++;
			}
		}

		return i;
	}	

	public static int getRealChanPositionByTitle(String title){
		int i = 0;
		String myTitle = title;
		for (RssChannel theChan: mRssChannels){

			if (theChan.getTitle().equalsIgnoreCase(myTitle))
				break;
			i++;

		}

		return i;		
	}

	public static int getEnabledChanCount(){
		int i = 0;
		for (RssChannel theChan: mRssChannels){
			if (theChan.isEnabled()){
				i++;
			}
		}

		return i;
	}
}
