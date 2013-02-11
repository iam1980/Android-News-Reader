package com.suredigit.naftemporikihd;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NaftemporikiParsers {

	public static Document parseXML(String xml) throws IOException, ParserConfigurationException, SAXException{
		Document doc = null;

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		doc = builder.parse(is);

		return doc;
	}

	public static ArrayList<Article> populateArticles (Document doc) throws XPathExpressionException {

		ArrayList<Article> articles = null;

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//item");

		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		if (nodes.getLength() > 0 ) {
			articles = new ArrayList<Article>();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node currentItem = nodes.item(i);

				NodeList nodeList = currentItem.getChildNodes();
				String title = null;
				String link = null;
				String text = null;
				String publDate = "";
				String thumbURL = null;
				String imgUrl = null;
				for (int j = 0; j < nodeList.getLength(); j++) {
					Node childNode = nodeList.item(j);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						if(childNode.getNodeName().equals("title")) title = childNode.getTextContent();
						if(childNode.getNodeName().equals("siteurl")) link = childNode.getTextContent();
						if(childNode.getNodeName().equals("text")) text = childNode.getTextContent();
						if(childNode.getNodeName().equals("pubDate")) publDate = childNode.getTextContent();
						if(childNode.getNodeName().equals("thumbnail")) thumbURL = childNode.getTextContent();
						if(thumbURL !=null)
							if (thumbURL.equalsIgnoreCase("")) thumbURL = null;

					}
				}
				if (title != null && link != null){
					//System.out.println("XXX"+thumbURL+"XXX");
					if (thumbURL != null){
						int x=getParamValue(thumbURL,"width");
						int y=getParamValue(thumbURL,"height");

						thumbURL = thumbURL.replace("&width="+x, "&width=150");
						thumbURL = thumbURL.replace("&height="+y,"&height=150");
						
						imgUrl = new String(thumbURL);
						
						int xF = getParamValue(imgUrl,"width");
						int yF = getParamValue(imgUrl,"height");
						
						imgUrl = imgUrl.replace("&width="+xF,"&width=768");
						imgUrl = imgUrl.replace("&height="+yF,"&height=330");
						
						//text = text.replaceAll("&nbsp;", " ");
						//text = text.replaceAll("\u00a0","");
						text = text.replaceAll("(\r\n|\n)", "<br />");
						
						//System.out.println(imgUrl);
					}

					articles.add(new Article(title,link,publDate,text,thumbURL,imgUrl));
					
				}

			}
		}

		return articles;

	}	

	public static String parseArticleHtml (String html){

		String articleContent = null;

		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		org.jsoup.nodes.Element storyDiv = null;

		storyDiv = doc.select("div#leftPHArea_sBody").first();

		if(storyDiv == null)
			storyDiv = doc.select("div#CPMain_sBody").first();	


		if(!(storyDiv == null)){
			storyDiv.select("div.storyAssets").remove();
			articleContent = storyDiv.html();	
		}


		return articleContent;

	}

	public static String parseArticleImgLink (String html){

		String imgSrc = null;

		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		org.jsoup.nodes.Element imgDiv = null;

		imgDiv = doc.select("div.storyMediaContainer").first();
		if (!(imgDiv == null)){
			imgDiv = imgDiv.select("div.storyMediaContent").first();
			imgDiv = imgDiv.select("img").first();
		}
		if(!(imgDiv == null)){
			imgSrc = MainActivity.BASEURL + imgDiv.attr("src");
			//System.out.println(imgSrc);
			int x=getParamValue(imgSrc,"width");
			int y=getParamValue(imgSrc,"height");
			//System.out.println(x+"+,"+y);
			imgSrc = imgSrc.replace("&width="+x, "&width=150");
			imgSrc = imgSrc.replace("&height="+y,"&height=150");
			//System.out.println(imgSrc);
		}
		return imgSrc;

	}

	public static Integer getParamValue(String urlStr,String param) {
		Pattern p = Pattern.compile(param+"=(\\d+)");
		Matcher m = p.matcher(urlStr);
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		}
		return null;
	}	

}
