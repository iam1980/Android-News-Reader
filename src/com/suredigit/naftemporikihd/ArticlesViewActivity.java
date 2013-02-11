package com.suredigit.naftemporikihd;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class ArticlesViewActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener{

	//TestFragmentAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;
	private MyAdapter mAdapter;
	RssChannel mChannel;
	String mCategoryTitle;
	public int mFontSize = 18;

	String[] mRssTitles;
	ArrayList<RssChannel> mRssChannels = new ArrayList<RssChannel>();

	private int mNaviCounter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(MainActivity.pNightMode) {
			setTheme(R.style.Theme_Sherlock);	
		} else 
			setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		//super.onCreate(savedInstanceState);
		super.onCreate(null);
		//getSupportActionBar().setIcon(R.drawable.ic_naftemporiki);
		getSupportActionBar().setDisplayShowTitleEnabled(false);



		SharedPreferences preferences = Singleton.getInstance().prefs;
		Gson gson = new Gson();
		String json = preferences.getString("channels","");
		Type listType = new TypeToken<ArrayList<RssChannel>>() {}.getType();
		mRssChannels = gson.fromJson(json,listType);		

		ArrayList<String> rssChannels = new ArrayList<String>();
		for (RssChannel myChan : mRssChannels){
			if(myChan.isEnabled())
				rssChannels.add(myChan.getTitle());
		}
		mRssTitles = new String[rssChannels.size()];
		rssChannels.toArray(mRssTitles);

		/** Create an array adapter to populate dropdownlist */
		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.sherlock_spinner_item, mRssTitles);
		adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);


		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(adapter, this);
		getSupportActionBar().setSelectedNavigationItem(2);




		setContentView(R.layout.activity_articles_view);
		// Show the Up button in the action bar.
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().setHomeButtonEnabled(true);

		Bundle b = getIntent().getExtras();
		RssChannel theChan = b.getParcelable("parcel");
		
		System.out.println("INDEX O FFFFF"+mRssChannels.indexOf(theChan));
		
		int position = b.getInt("position");
		System.out.println(theChan.getTitle());
		System.out.println(theChan.getArticles().get(position));
		mChannel = theChan;
		mCategoryTitle = theChan.getTitle();


		mAdapter = new MyAdapter(getSupportFragmentManager());
		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		//mPager.setOffscreenPageLimit(mChannel.getArticles().size()-1);

		UnderlinePageIndicator indicator = (UnderlinePageIndicator)findViewById(R.id.indicator);
		//CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
		indicator.setFades(false);
		indicator.setCurrentItem(position);
		mIndicator = indicator;

	}

	public class MyAdapter extends FragmentPagerAdapter {
		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mChannel.getArticles().size();
		}

		@Override
		public Fragment getItem(int position) {
			return ArticleFragment.newInstance(mChannel.getArticles().get(position),mCategoryTitle);
		}
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_articles_view, menu);	
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.menu_night).setChecked(MainActivity.pNightMode);
		return true;
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
			finish();
			//NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_browser:
			Article theArticleB = mChannel.getArticles().get(mPager.getCurrentItem());
			String shareTextB = theArticleB.getLink();
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shareTextB));
			startActivity(browserIntent);

			return true;
		case R.id.menu_share:
			String shareText = "";
			String shareTitle = "";

			Article theArticle = mChannel.getArticles().get(mPager.getCurrentItem());
			shareText = theArticle.getLink();
			shareTitle = theArticle.getTitle();

			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("text/plain");
			share.putExtra(Intent.EXTRA_TEXT, shareText);
			share.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
			startActivity(Intent.createChooser(share, "Μοιράσου το άρθρο"));
			return true;
		case R.id.menu_night:
			if(item.isChecked()){
				item.setChecked(false);
				MainActivity.pNightMode = false;
				MainActivity.savePreferences();
			} else {
				item.setChecked(true);
				MainActivity.pNightMode = true;
				MainActivity.savePreferences();
			}
			finish();
			overridePendingTransition(0, 0);
			int position =  mPager.getCurrentItem();

			Intent intent = new Intent(this, ArticlesViewActivity.class);
			intent.putExtra("parcel", mChannel);
			intent.putExtra("position", position);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);

			return true;			

		case R.id.menu_inc_font:

			if (MainActivity.pFontSize < 22){
				MainActivity.pFontSize = MainActivity.pFontSize + 2;
				MainActivity.savePreferences();
				finish();
				overridePendingTransition(0, 0);
				int positionInc =  mPager.getCurrentItem();

				Intent intentInc = new Intent(this, ArticlesViewActivity.class);
				intentInc.putExtra("parcel", mChannel);
				intentInc.putExtra("position", positionInc);
				intentInc.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intentInc);

			}



			return true;
		case R.id.menu_dec_font:

			if (MainActivity.pFontSize > 14){
				MainActivity.pFontSize = MainActivity.pFontSize - 2;
				MainActivity.savePreferences();
				finish();
				overridePendingTransition(0, 0);
				int positionDec =  mPager.getCurrentItem();

				Intent intentDec = new Intent(this, ArticlesViewActivity.class);
				intentDec.putExtra("parcel", mChannel);
				intentDec.putExtra("position", positionDec);
				intentDec.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intentDec);

			}



			return true;			
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (mNaviCounter != 0){
			System.out.println("Selected: " + mRssTitles[itemPosition]);
			//		Intent intent = new Intent(ArticlesViewActivity.this, ArticlesViewActivity.class);
			//		intent.putExtra("parcel", mRssChannels.get(1));
			//		intent.putExtra("position", 0);
			//		startActivity(intent);
			//		return false;
		}
		mNaviCounter++;
		return false;
	}

}
