package com.suredigit.naftemporikihd;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class MainActivity extends SherlockActivity  {

	private static ArrayList<RssChannel> mRssChannels;
	private static final String TAG = "NaftermporikiHD";
	private ArrayList<RssHtmlDownloaderTask> mRssHtmlDownloaderTasks;
	private LinearLayout myLinList;
	public static final String BASEURL = "http://www.naftemporiki.gr";


	private final ImageDownloader imageDownloader = new ImageDownloader();

	ProgressDialog mProgressDialog;

	public static boolean pNightMode;
	public static int pFontSize;

	public static void loadPreferences() {	
		SharedPreferences preferences = Singleton.getInstance().prefs;
		pNightMode = preferences.getBoolean("nightmode", false);
		pFontSize = preferences.getInt("fontsize",18);	

	}

	public static void savePreferences() {
		SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
		editor.putBoolean("nightmode", pNightMode);
		editor.putInt("fontsize", pFontSize);
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

	public static void saveChannels(){
		SharedPreferences.Editor editor = Singleton.getInstance().prefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(mRssChannels); 
		editor.putString("channels",json);
		editor.commit();
	}

	public static void loadChannels(){
		SharedPreferences preferences = Singleton.getInstance().prefs;
		ArrayList<RssChannel> defaultRssChannels = new ArrayList<RssChannel>();
		defaultRssChannels.add(new RssChannel("Οικονομία & Αγορές",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=1"));
		defaultRssChannels.add(new RssChannel("Πολιτική",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=2"));		
		defaultRssChannels.add(new RssChannel("Κοινωνία",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=3"));		
		defaultRssChannels.add(new RssChannel("Κόσμος",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=4"));
		defaultRssChannels.add(new RssChannel("Αθλητικά",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=5"));
		defaultRssChannels.add(new RssChannel("Πολιτισμός",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=6"));
		defaultRssChannels.add(new RssChannel("Περιβάλλον",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=8"));
		defaultRssChannels.add(new RssChannel("Τεχνολογία - Επιστήμη",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=7"));
		Gson gson = new Gson();
		String jsonDef = gson.toJson(defaultRssChannels); 

		String json = preferences.getString("channels",jsonDef);
		Type listType = new TypeToken<ArrayList<RssChannel>>() {}.getType();
		mRssChannels = gson.fromJson(json,listType);

	}
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = this.getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		loadPreferences();
		loadChannels();
		System.out.println("NIGHT MODE!!!" + pNightMode);
		setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		Log.i(TAG,"On Create");
		getSupportActionBar().setIcon(R.drawable.ic_naftemporiki);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		setContentView(R.layout.activity_main);

		System.out.println("preferences font:"+ pFontSize);


		//*** - HTTP INIT START

		//		// Create and initialize HTTP parameters
		//		HttpParams params = new BasicHttpParams();
		//		ConnManagerParams.setMaxTotalConnections(params, 100);
		//		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		//
		//		// Create and initialize scheme registry 
		//		SchemeRegistry schemeRegistry = new SchemeRegistry();
		//		schemeRegistry.register(
		//				new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		//
		//		// Create an HttpClient with the ThreadSafeClientConnManager.
		//		// This connection manager must be used if more than one thread will
		//		// be using the HttpClient.
		//		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		//		HttpClient httpClient = new DefaultHttpClient(cm, params);

		//*** - HTTP INIT END


		//		mRssChannels.add(new RssChannel("Οικονομία & Αγορές",BASEURL + "/rssFeed?mode=section&id=1&atype=story"));
		//		mRssChannels.add(new RssChannel("Πολιτική",BASEURL + "/rssFeed?mode=section&id=2&atype=story"));		
		//		mRssChannels.add(new RssChannel("Κοινωνία",BASEURL + "/rssFeed?mode=section&id=3&atype=story"));		
		//		mRssChannels.add(new RssChannel("Κόσμος",BASEURL + "/rssFeed?mode=section&id=4&atype=story"));
		//		mRssChannels.add(new RssChannel("Αθλητικά",BASEURL + "/rssFeed?mode=section&id=5&atype=story"));
		//		mRssChannels.add(new RssChannel("Πολιτισμός",BASEURL + "/rssFeed?mode=section&id=6&atype=story"));
		//		mRssChannels.add(new RssChannel("Περιβάλλον",BASEURL + "/rssFeed?mode=section&id=8&atype=story"));
		//		mRssChannels.add(new RssChannel("Τεχνολογία - Επιστήμη",BASEURL + "/rssFeed?mode=section&id=7&atype=story"));

		//		mRssChannels.add(new RssChannel("Οικονομία & Αγορές",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=1"));
		//		mRssChannels.add(new RssChannel("Πολιτική",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=2"));		
		//		mRssChannels.add(new RssChannel("Κοινωνία",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=3"));		
		//		mRssChannels.add(new RssChannel("Κόσμος",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=4"));
		//		mRssChannels.add(new RssChannel("Αθλητικά",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=5"));
		//		mRssChannels.add(new RssChannel("Πολιτισμός",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=6"));
		//		mRssChannels.add(new RssChannel("Περιβάλλον",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=8"));
		//		mRssChannels.add(new RssChannel("Τεχνολογία - Επιστήμη",BASEURL + "/api/legacy/android/GetNews.aspx?&cat=7"));

		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setMessage("Μεταφώρτωση Ειδήσεων");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(mRssChannels.size());
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		//*** - DOWNLOAD RSS CHANNELS START

		// create a thread for each RssChannel
		//		GetHttpTextThread[] rssThreads = new GetHttpTextThread[rssChannels.size()];
		//		for (int i = 0; i < rssChannels.size(); i++) {
		//			HttpGet httpget = new HttpGet(rssChannels.get(i).getUrl());
		//			rssThreads[i] = new GetHttpTextThread(httpClient, httpget, i + 1,rssChannels.get(i));
		//		}
		//
		//		// start the threads
		//		for (int j = 0; j < rssThreads.length; j++) {
		//			rssThreads[j].start();
		//		}
		//
		//		// join the threads
		//		for (int j = 0; j < rssThreads.length; j++) {
		//			try {
		//				rssThreads[j].join();
		//			} catch (InterruptedException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		}

		// When HttpClient instance is no longer needed, 
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources

		//httpClient.getConnectionManager().shutdown();  		

		//*** - DOWNLOAD RSS END


		if (getLastFetch() + 60 < (System.currentTimeMillis()/1000)){
			mRssHtmlDownloaderTasks = new ArrayList<RssHtmlDownloaderTask>();
			for (RssChannel theChan : mRssChannels){
				if (theChan.isEnabled()){
					RssHtmlDownloaderTask task = new RssHtmlDownloaderTask(theChan);
					//task.execute(theChan.getUrl());
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,theChan.getUrl());
					mRssHtmlDownloaderTasks.add(task);
					Log.i(TAG,"task d/l linkg" + theChan.getUrl());
				}
			}
			updateLastFetch();
			System.out.println("FINISHED ON CREATE");
		} else {
			System.out.println("GOT HHHHHHHHHHHHHHHHHERE");
			loadChannels();
			buildUI();
			updateThumbnails();
		}

	}



	private void buildUI(){
		//*** - PARSE XML START		
		for (RssChannel theChan : mRssChannels){
			if(!(theChan.getHtml() == null)){
				try {
					Document doc = NaftemporikiParsers.parseXML(theChan.getHtml());
					theChan.setArticles(NaftemporikiParsers.populateArticles(doc));
					if(!(theChan.getArticles()== null)){
						System.out.println(theChan.getArticles().size());
						for (Article theArticle: theChan.getArticles()){
							//theArticle.setParentRssChan(theChan);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				if (!(theChan.getArticles() == null))
					Collections.reverse((ArrayList<Article>) theChan.getArticles());
			}
		}
		//*** - PARSE XML END

		//*** - BUILD HORIZONTALLISTVIEWS START
		myLinList =(LinearLayout) findViewById(R.id.LinearLayout1);

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
				channTitleTV.setText(theChan.getTitle());
				channTitleTV.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				channTitleTV.setTextSize(15);
				channTitleTV.setTypeface(null,Typeface.BOLD);

				TextView spacerTV = new TextView(this);

				myLinList.addView(channTitleTV);
				myLinList.addView(hListView);
				myLinList.addView(spacerTV);


				//theChan.setHlist(hListView);

				hListView.setOnItemClickListener(new OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						// TODO Auto-generated method stub
						v.playSoundEffect(SoundEffectConstants.CLICK);
						Article clickedArticle = (Article)parent.getItemAtPosition(position);
						Intent intent = new Intent(MainActivity.this, ArticlesViewActivity.class);
						intent.putExtra("parcel", myChan);
						intent.putExtra("position", position);
						startActivity(intent);



					}}
						);
			}
		}
		//*** - BUILD HORIZONTALLISTVIEWS END

		//prefetchArticleHtml();




		saveChannels();
		//		Gson gson = new Gson();
		//		String json = gson.toJson(mRssChannels); 



	}

	//	@SuppressLint("NewApi")
	//	private void prefetchArticleHtml(){
	//		mArticleHtmlDownloaderTasks = new ArrayList<ArticleHtmlDownloaderTask>();
	//		int cC = 0;
	//		for (RssChannel theChan : mRssChannels){
	//			if (!(theChan.getArticles() == null)){
	//				int aC = 0;
	//				for (Article theArticle: theChan.getArticles()){
	//					ArticleHtmlDownloaderTask task = new ArticleHtmlDownloaderTask(theArticle);
	//					//ArticleHtmlDownloaderTask task = new ArticleHtmlDownloaderTask();
	//
	//
	//					//task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,theArticle.getLink());
	//					task.execute(theArticle.getLink());
	//					mArticleHtmlDownloaderTasks.add(task);
	//					Log.i(TAG,"task d/l Article html" + theArticle.getLink());
	//
	//					aC++;
	//					//if(aC==3) break;
	//				}
	//			}
	//			cC++;
	//			//if (cC == 3) break;
	//		}
	//	}

	private void updateThumbnails(){

		int childcount = myLinList.getChildCount();
		for (int i=0; i < childcount; i++){
			View v = myLinList.getChildAt(i);
			if(v instanceof HorizontalListView){	
				ArticleAdapter theAdapter = (ArticleAdapter)((HorizontalListView) v).getAdapter();
				theAdapter.notifyDataSetChanged();
				System.out.println("FOUND A LISTVIEW! at"+ i);
			}
		}
	}

	private class ArticleAdapter extends ArrayAdapter<Article> {

		private ArrayList<Article> articles;

		public ArticleAdapter(Context context, int textViewResourceId, ArrayList<Article> objects) {
			super(context, textViewResourceId, objects);
			this.articles = objects;
		}

		@Override  
		public View getView(int position, View convertView, ViewGroup parent) {  
			View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_list_item, null);  
			TextView title = (TextView) retval.findViewById(R.id.title);  
			ImageView thumb = (ImageView) retval.findViewById(R.id.image);

			Article article = articles.get(position);

			title.setText(article.getTitle());  

			if(!(article.getThumbUrl() == null)){
				thumb.setBackgroundColor(Color.BLACK);
				imageDownloader.download(article.getThumbUrl(), (ImageView) thumb);
				int pxX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
				int pxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());				
				thumb.getLayoutParams().height = pxX;
				thumb.getLayoutParams().width = pxY;
				//System.out.println(pxX);

			}

			return retval;  
		}  		

	}




	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case R.id.menu_rss:
			System.out.println("RSS");

			Intent myIntent = new Intent(MainActivity.this, MultipleChoiceListView.class);
			MainActivity.this.startActivity(myIntent);

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
				System.out.println("LOADED HTML COINTENT");
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
				System.out.println("ALLLLLLLLLLLLLLL DONE");
				buildUI();
				updateThumbnails();
				mProgressDialog.dismiss();
				//prefetchArticleHtml();

			}			
		}
	}

	//	class ArticleHtmlDownloaderTask extends AsyncTask<String, Void, String> {
	//		private String url;
	//		//private Article article;
	//		//private final WeakReference<ImageView> imageViewReference;
	//		private WeakReference<Article> wArticle;
	//
	//		public ArticleHtmlDownloaderTask(Article article) {
	//
	//			wArticle = new WeakReference<Article>(article);
	//		}
	//
	//		/**
	//		 * Actual download method.
	//		 */
	//		@Override
	//		protected String doInBackground(String... params) {
	//			url = params[0];
	//			return downloadHtml(url);
	//		}
	//
	//		/**
	//		 * Once the image is downloaded, associates it to the imageView
	//		 */
	//		@Override
	//		protected void onPostExecute(String theHtml) {
	//			Article article = wArticle.get();
	//			if (article != null){
	//				if (isCancelled()) {
	//					article.setHtml(null);
	//				}
	//
	//				if (theHtml != null) {
	//					article.setHtml(theHtml);
	//					System.out.println("LOADED HTML COINTENT");	
	//					article.setArticleHtml(NaftemporikiParsers.parseArticleHtml(theHtml));			
	//					article.setImgUrl(NaftemporikiParsers.parseArticleImgLink(theHtml));
	//					
	//					updateThumbnails();
	//				}
	//			}
	//
	//		}
	//	}	



	String downloadHtml(String url) {
		final HttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(url);
		Log.w(TAG, "starte d d/ling" + url); 
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
					Log.w(TAG, "finished d/ling" + url); 
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

	public static void serializeChannels(){

	}


	//	static class GetHttpTextThread extends Thread {
	//
	//		private final HttpClient httpClient;
	//		private final HttpContext context;
	//		private final HttpGet httpget;
	//		private final int id;
	//		private final RssChannel rssChannel;
	//
	//		public GetHttpTextThread(HttpClient httpClient, HttpGet httpget, int id, RssChannel theChan) {
	//			this.httpClient = httpClient;
	//			this.context = new BasicHttpContext();
	//			this.httpget = httpget;
	//			this.id = id;
	//			this.rssChannel = theChan;
	//		}
	//
	//		/**
	//		 * Executes the GetMethod and prints some status information.
	//		 */
	//		@Override
	//		public void run() {
	//
	//			System.out.println(id + " - about to get something from " + httpget.getURI());
	//
	//			try {
	//
	//				// execute the method
	//				HttpResponse response = httpClient.execute(httpget, context);
	//
	//				System.out.println(id + " - get executed");
	//				// get the response body as an array of bytes
	//				HttpEntity entity = response.getEntity();
	//				if (entity != null) {
	//					//System.out.println(entity.toString());
	//					//byte[] bytes = EntityUtils.toByteArray(entity);
	//					//System.out.println(id + " - " + bytes.length + " bytes read");
	//
	//					InputStream instream = entity.getContent();
	//					String result= convertStreamToString(instream);
	//					// now you have the string representation of the HTML request
	//					instream.close();
	//					//System.out.println(result);
	//					rssChannel.setHtml(result);
	//
	//				}
	//
	//			} catch (Exception e) {
	//				httpget.abort();
	//				e.printStackTrace();
	//				System.out.println(id + " - error: " + e);
	//			}
	//		}
	//
	//	}


	//	static class GetArticleHttpThread extends Thread {
	//
	//		private final HttpClient httpClient;
	//		private final HttpContext context;
	//		private final HttpGet httpget;
	//		private final int id;
	//		private final Article article;
	//
	//		public GetArticleHttpThread(HttpClient httpClient, HttpGet httpget, int id, Article theArt) {
	//			this.httpClient = httpClient;
	//			this.context = new BasicHttpContext();
	//			this.httpget = httpget;
	//			this.id = id;
	//			this.article = theArt;
	//		}
	//
	//		/**
	//		 * Executes the GetMethod and prints some status information.
	//		 */
	//		@Override
	//		public void run() {
	//
	//			System.out.println(id + " - about to get something from " + httpget.getURI());
	//
	//			try {
	//
	//				// execute the method
	//				HttpResponse response = httpClient.execute(httpget, context);
	//
	//				System.out.println(id + " - get executed");
	//				// get the response body as an array of bytes
	//				HttpEntity entity = response.getEntity();
	//				if (entity != null) {
	//					//System.out.println(entity.toString());
	//					//byte[] bytes = EntityUtils.toByteArray(entity);
	//					//System.out.println(id + " - " + bytes.length + " bytes read");
	//
	//					InputStream instream = entity.getContent();
	//					String result= convertStreamToString(instream);
	//					// now you have the string representation of the HTML request
	//					instream.close();
	//					System.out.println(result);
	//					article.setHtml(result);
	//
	//				}
	//
	//			} catch (Exception e) {
	//				httpget.abort();
	//				System.out.println(id + " - error: " + e);
	//			}
	//		}
	//
	//	}	
}
