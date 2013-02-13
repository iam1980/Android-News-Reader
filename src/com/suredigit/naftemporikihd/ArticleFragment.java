package com.suredigit.naftemporikihd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public final class ArticleFragment extends Fragment {
	private static final String TAG = "NaftermporikiHD Afragment";
	private static final String KEY_CONTENT = "ArticleFragment:Content";
	private Article mArticle;
	private String mCategoryTitle;
	private ImageView ivPhoto;

	//private ImageDownloader imageDownloader = new ImageDownloader();
	private TextView tvContent;	

	public static ArticleFragment newInstance(Article article,String category) {
		ArticleFragment fragment = new ArticleFragment();

		fragment.mArticle = article;
		fragment.mCategoryTitle = category;

		return fragment;
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("GOTE HER");
		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mArticle = savedInstanceState.getParcelable(KEY_CONTENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//        TextView text = new TextView(getActivity());
		//        text.setGravity(Gravity.CENTER);
		//        text.setText(mContent);
		//        text.setTextSize(20 * getResources().getDisplayMetrics().density);
		//        text.setPadding(20, 20, 20, 20);
		//
		//        LinearLayout layout = new LinearLayout(getActivity());
		//        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		//        layout.setGravity(Gravity.CENTER);
		//        layout.addView(text);
		View v = inflater.inflate(R.layout.fragment_article, container, false);
		final TextView tvCategory = (TextView)v.findViewById(R.id.textViewCategory);
		TextView tvDate = (TextView)v.findViewById(R.id.textViewDate);
		TextView tvTitle = (TextView)v.findViewById(R.id.textViewTitle);
		tvContent = (TextView)v.findViewById(R.id.textViewArticleContent);

		ArticlesViewActivity pActivity = (ArticlesViewActivity) getActivity();

		tvContent.setTextSize(MainActivity.pFontSize);

		ivPhoto = (ImageView)v.findViewById(R.id.imageViewPhoto);

		tvCategory.setText(mCategoryTitle);
		tvTitle.setText(mArticle.getTitle());

		tvDate.setText(mArticle.getDateGr());
		//

		if(!(mArticle.getImgUrl() == null)){
			ArticlesViewActivity.imageDownloader.download(mArticle.getImgUrl(), (ImageView) ivPhoto);
			ivPhoto.setOnClickListener(new View.OnClickListener() {
				//@Override
				public void onClick(View v) {
					Log.v(TAG, " click"); 

					if ((ivPhoto.getDrawable() instanceof BitmapDrawable) &&
							(((BitmapDrawable)ivPhoto.getDrawable()).getBitmap() != null)
							){
						//if (((BitmapDrawable)ivPhoto.getDrawable()).getBitmap() == null) System.out.println("HELLO");
//						Bitmap bitmap = ((BitmapDrawable)ivPhoto.getDrawable()).getBitmap();
//						System.out.println(bitmap.getRowBytes());
//
//
//						FileOutputStream fos;
//						try {
//							fos = getActivity().openFileOutput("TMPIMG", Context.MODE_PRIVATE);
//							bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//							fos.close();
//
//						} catch (FileNotFoundException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						File cacheDir = getActivity().getBaseContext().getCacheDir();
						File f = new File(cacheDir, "pic");

                        try {
                            FileOutputStream out = new FileOutputStream(f);
                            Bitmap bitmap = ((BitmapDrawable)ivPhoto.getDrawable()).getBitmap();
                            bitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100, out);
                            out.flush();
                            out.close();
                            System.out.println("FILE SAVED");

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }						
						
						Intent intent = new Intent(getActivity(), FullscreenPhotoViewerActivity.class);
						intent.putExtra("title", mArticle.getTitle());
						startActivity(intent);
					}
				}        
			});


		} else {
			ivPhoto.setVisibility(View.GONE);
		}

		if(!(mArticle.getText() == null))
			tvContent.setText(Html.fromHtml(mArticle.getText()));
		//tvContent.setText((mArticle.getText()));
		//Log.i(TAG,mArticle.getText());


		ScrollView theScroll = (ScrollView)v.findViewById(R.id.scrollViewContainer);
		theScroll.fullScroll(ScrollView.FOCUS_UP);
		theScroll.smoothScrollTo(0, 0);



		return v;
	}



	public float getCurrentFontSize(){
		float sizePix = tvContent.getTextSize();
		float sizeSp = pixelsToSp(getActivity(),sizePix);
		return sizeSp;
	}

	public void toggleTextSize(){
		System.out.println("Hello");
		//float sizePix = tvContent.getTextSize();
		//float sizeSp = pixelsToSp(getActivity(),sizePix);
		//System.out.println(sizeSp);

		//if(sizeSp == 18)
		if (tvContent!=null)
			tvContent.setTextSize(24);

		//if(sizeSp == 22)
		//	tvContent.setTextSize(18);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putString(KEY_CONTENT, mContent);
		outState.putParcelable("KEY_CONTENT", mArticle);
		System.out.println("SAVING");
	}

	public static float pixelsToSp(Context context, Float px) {
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		return px/scaledDensity;
	}



}
