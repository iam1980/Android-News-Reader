package com.suredigit.naftemporikihd;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

public class ArticlesViewActivity extends SherlockFragmentActivity {

	//TestFragmentAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;
	private MyAdapter mAdapter;
	RssChannel mChannel;
	String mCategoryTitle;
	public int mFontSize = 18;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(MainActivity.pNightMode) {
			setTheme(R.style.Theme_Sherlock);	
		} else 
			setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		//super.onCreate(savedInstanceState);
		super.onCreate(null);
		getSupportActionBar().setIcon(R.drawable.ic_naftemporiki);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		setContentView(R.layout.activity_articles_view);
		// Show the Up button in the action bar.
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().setHomeButtonEnabled(true);

		Bundle b = getIntent().getExtras();
		RssChannel theChan = b.getParcelable("parcel");
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

}
