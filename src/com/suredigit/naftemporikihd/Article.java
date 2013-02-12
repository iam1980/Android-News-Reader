package com.suredigit.naftemporikihd;

import android.os.Parcel;
import android.os.Parcelable;

public class Article implements Parcelable{
	
	private String title;
	private String link;
	private String date;
	private String dateGr;
	private String imgUrl;
	private String thumbUrl;
	private String text;
	private boolean isDummy;
	//private RssChannel parentRssChan;
	
	public Article(String title, String link, String date, String dateGR, String text, String thumbUrl, String imgUrl) {
		super();
		this.title = title;
		this.link = link;
		this.date = date;
		this.text = text;
		this.thumbUrl = thumbUrl;
		this.imgUrl = imgUrl;
		this.isDummy = false;
		this.dateGr = dateGR;
	}
	
	public Article(String dummy){
		title = "DUMMYARTICLE";
		this.isDummy = true;
	}

	private Article(Parcel in) {
		title = in.readString();
		link = in.readString();
		date = in.readString();
		imgUrl = in.readString();
		text = in.readString();
		dateGr = in.readString();
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDate() {
		return date;
	}

	public String getImgUrl() {
		return imgUrl;
	}

//	public Bitmap getImg() {
//		return img;
//	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

//	public void setImg(Bitmap img) {
//		this.img = img;
//	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}	

//	public RssChannel getParentRssChan() {
//		return parentRssChan;
//	}

//	public void setParentRssChan(RssChannel parentRssChan) {
//		this.parentRssChan = parentRssChan;
//	}
	

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public boolean isDummy() {
		return isDummy;
	}
	
	public String getDateGr() {
		return dateGr;
	}

	public void setDateGr(String dateGr) {
		this.dateGr = dateGr;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(link);
		dest.writeString(date);
		dest.writeString(imgUrl);
		dest.writeString(text);
		dest.writeString(dateGr);
		
	}
	
	public static final Parcelable.Creator<Article> CREATOR
	= new Parcelable.Creator<Article>() {
		public Article createFromParcel(Parcel in) {
			return new Article(in);
		}

		public Article[] newArray(int size) {
			return new Article[size];
		}
	};	
	

}
