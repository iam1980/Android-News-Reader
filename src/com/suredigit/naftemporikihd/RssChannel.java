package com.suredigit.naftemporikihd;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class RssChannel implements Parcelable{

	private String title;
	private String url;
	private String html;
	private boolean enabled = true;
	private ArrayList<Article> articles;
	//	private HorizontalListView hlist = null;

	public RssChannel(String title, String url) {
		super();
		this.title = title;
		this.url = url;
	}	

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getHtml (){
		return html;
	}	

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setHtml(String html){
		this.html = html;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ArrayList<Article> getArticles() {
		return articles;
	}

	public void setArticles(ArrayList<Article> articles) {
		this.articles = articles;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub	
		dest.writeString(title);
		dest.writeString(url);
		dest.writeString(html);
		dest.writeTypedList(articles);
	}

	public static final Parcelable.Creator<RssChannel> CREATOR
	= new Parcelable.Creator<RssChannel>() {
		public RssChannel createFromParcel(Parcel in) {
			return new RssChannel(in);
		}

		public RssChannel[] newArray(int size) {
			return new RssChannel[size];
		}
	};	

	RssChannel() {
		// initialization
		articles = new ArrayList<Article>();
	}

	private RssChannel(Parcel in) {
		this();
		title = in.readString();
		url = in.readString();
		html = in.readString();
		in.readTypedList(articles, Article.CREATOR);
	}	
	//	public HorizontalListView getHlist() {
	//		return hlist;
	//	}
	//
	//	public void setHlist(HorizontalListView hlist) {
	//		this.hlist = hlist;
	//	}

	@Override
	public String toString() {
		return "RssChannel [title=" + title + ", url=" + url + ", html=" + html + ", enabled=" + enabled + ", articles=" + articles + "]";
	}


	

}
